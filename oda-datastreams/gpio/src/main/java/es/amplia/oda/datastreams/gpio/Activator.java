package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.osgi.proxies.GpioServiceProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.gpio.configuration.DatastreamsGpioConfigurationHandler;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private GpioServiceProxy gpioService;
    private EventPublisherProxy eventPublisher;
    private GpioDatastreamsManager manager;
    private ConfigurableBundle configurableBundle;
    private ServiceListenerBundle<GpioService> gpioServiceListener;
    private ServiceListenerBundle<DeviceInfoProvider> deviceInfoProviderServiceListener;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting GPIO datastreams bundle");

        gpioService = new GpioServiceProxy(bundleContext);
        eventPublisher = new EventPublisherProxy(bundleContext);
        GpioDatastreamsFactory factory = new GpioDatastreamsFactoryImpl(gpioService, eventPublisher);
        ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsGetter.class);
        ServiceRegistrationManager<DatastreamsSetter> datastreamsSetterRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, DatastreamsSetter.class);
        manager = new GpioDatastreamsManager(factory, datastreamsGetterRegistrationManager,
                datastreamsSetterRegistrationManager);

        ConfigurationUpdateHandler configurationHandler =
                new DatastreamsGpioConfigurationHandler(manager, gpioService);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configurationHandler);
        gpioServiceListener = new ServiceListenerBundle<>(bundleContext, GpioService.class,
                () -> onServiceChanged(configurationHandler));
        deviceInfoProviderServiceListener = new ServiceListenerBundle<>(bundleContext, DeviceInfoProvider.class,
                () -> onServiceChanged(configurationHandler));

        LOGGER.info("GPIO datastreams bundle started");
    }

    void onServiceChanged(ConfigurationUpdateHandler configHandler) {
        try {
            configHandler.applyConfiguration();
        }catch (Exception exception) {
            LOGGER.error("Exception applying configuration: ", exception);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping GPIO datastreams bundle");

        deviceInfoProviderServiceListener.close();
        gpioServiceListener.close();
        configurableBundle.close();
        manager.close();
        gpioService.close();
        eventPublisher.close();

        LOGGER.info("GPIO datastreams bundle stopped");
    }
}
