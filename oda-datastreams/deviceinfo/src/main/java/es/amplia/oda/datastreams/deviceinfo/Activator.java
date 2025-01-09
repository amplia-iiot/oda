package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfigurationHandler;

import es.amplia.oda.datastreams.deviceinfo.configuration.ScriptsLoader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ConfigurableBundle configurableBundle;
    private ScriptsLoader scriptsLoader;
    private DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter;

    private ServiceRegistration<DeviceInfoProvider> deviceIdProviderRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Datastreams Getter Device Info");

        CommandProcessor commandProcessor = new CommandProcessorImpl();
        deviceInfoDatastreamsGetter = new DeviceInfoDatastreamsGetter(commandProcessor, bundleContext);
        scriptsLoader = new ScriptsLoader(commandProcessor);
        ConfigurationUpdateHandler configHandler = new DeviceInfoConfigurationHandler(deviceInfoDatastreamsGetter, scriptsLoader);
        deviceIdProviderRegistration =
                bundleContext.registerService(DeviceInfoProvider.class, deviceInfoDatastreamsGetter, null);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler,
                Collections.singletonList(deviceIdProviderRegistration));

        LOGGER.info("Datastreams Getter Device Info started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Datastreams Getter Device");

        deviceInfoDatastreamsGetter.unregister();
        deviceIdProviderRegistration.unregister();
        configurableBundle.close();
        try {
            scriptsLoader.close();
        } catch (Exception e) {
            LOGGER.error("Error trying to close scripts loader", e);
        }

        LOGGER.info("Datastreams Getter Device stopped");
    }
}
