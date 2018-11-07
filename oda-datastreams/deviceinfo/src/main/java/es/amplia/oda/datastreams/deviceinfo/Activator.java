package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import es.amplia.oda.core.commons.utils.CommandProcessorImpl;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfigurationHandler;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ConfigurableBundle configurableBundle;

    private ServiceRegistration<DeviceInfoProvider> deviceIdProviderRegistration;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForSerialNumber;
    private ServiceRegistration<DatastreamsGetter> datastreamsGetterRegistrationForDeviceId;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Datastreams Getter Device Info");

        CommandProcessor commandProcessor = new CommandProcessorImpl();
        DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter = new DeviceInfoDatastreamsGetter(commandProcessor);
        ConfigurationUpdateHandler configHandler = new DeviceInfoConfigurationHandler(deviceInfoDatastreamsGetter);
        configurableBundle = new ConfigurableBundle(bundleContext, configHandler);

        deviceIdProviderRegistration =
                bundleContext.registerService(DeviceInfoProvider.class, deviceInfoDatastreamsGetter, null);
        datastreamsGetterRegistrationForDeviceId =
                bundleContext.registerService(DatastreamsGetter.class,
                        deviceInfoDatastreamsGetter.getDatastreamsGetterForDeviceId(), null);
        datastreamsGetterRegistrationForSerialNumber =
                bundleContext.registerService(DatastreamsGetter.class,
                        deviceInfoDatastreamsGetter.getDatastreamsGetterForSerialNumber(), null);

        LOGGER.info("Datastreams Getter Device Info started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Datastreams Getter Device");

        datastreamsGetterRegistrationForSerialNumber.unregister();
        datastreamsGetterRegistrationForDeviceId.unregister();
        deviceIdProviderRegistration.unregister();
        configurableBundle.close();

        LOGGER.info("Datastreams Getter Device stopped");
    }
}
