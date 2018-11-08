package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.GpioServiceProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.datastreams.gpio.configuration.DatastreamsGpioConfigurationHandler;
import es.amplia.oda.event.api.EventDispatcherProxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private GpioServiceProxy gpioService;

    private EventDispatcherProxy eventDispatcher;

    private GpioDatastreamsRegistry registry;

    private ConfigurableBundle configurableBundle;

    private ServiceListenerBundle<GpioService> gpioServiceListener;
    private ServiceListenerBundle<DeviceInfoProvider> deviceInfoProviderServiceListener;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting GPIO datastreams bundle");

        gpioService = new GpioServiceProxy(bundleContext);
        eventDispatcher = new EventDispatcherProxy(bundleContext);
        registry = new GpioDatastreamsRegistry(bundleContext, gpioService, eventDispatcher);

        ConfigurationUpdateHandler configurationHandler =
                new DatastreamsGpioConfigurationHandler(registry, gpioService);
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
            LOGGER.warn("Exception applying configuration: {}", exception);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping GPIO datastreams bundle");

        deviceInfoProviderServiceListener.close();
        gpioServiceListener.close();
        configurableBundle.close();
        registry.close();
        gpioService.close();
        eventDispatcher.close();

        LOGGER.info("GPIO datastreams bundle stopped");
    }
}
