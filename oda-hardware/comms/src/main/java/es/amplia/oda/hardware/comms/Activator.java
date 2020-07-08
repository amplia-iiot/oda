package es.amplia.oda.hardware.comms;

import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.hardware.comms.configuration.CommsConfigurationUpdateHandler;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
    static final int NUM_THREADS = 1;

    private ScriptsLoader scriptsLoader;
    private CommsManager commsManager;
    private ConfigurableBundle configurableBundle;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Comms bundle");
        CommandProcessor commandProcessor = new CommandProcessorImpl();
        scriptsLoader = new ScriptsLoaderImpl(commandProcessor);
        commsManager = new CommsManagerImpl(commandProcessor);
        ConfigurationUpdateHandler configHandler = new CommsConfigurationUpdateHandler(scriptsLoader, commsManager);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        LOGGER.info("Comms bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Comms bundle");
        configurableBundle.close();
        commsManager.close();
        scriptsLoader.close();
        LOGGER.info("Comms bundle stopped");
    }
}
