package es.amplia.oda.subsystem.countermanager;

import es.amplia.oda.core.commons.countermanager.CounterManager;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.subsystem.countermanager.configuration.CounterManagerConfigurationUpdateHandler;
import es.amplia.oda.subsystem.countermanager.internal.CounterEngine;
import es.amplia.oda.subsystem.countermanager.internal.CounterManagerImpl;
import es.amplia.oda.subsystem.countermanager.internal.PrinterOutputManager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ConfigurationUpdateHandler configHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceRegistration<CounterManager> registration;
    private CounterEngine engine;
    private PrinterOutputManager printerManager;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Counter Manager bundle");

        engine = new CounterEngine();
        printerManager = new PrinterOutputManager();
        engine.setPrinterOutputManager(printerManager);
        printerManager.setCounterEngine(engine);
        configHandler = new CounterManagerConfigurationUpdateHandler(engine, printerManager);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        registration = bundleContext.registerService(CounterManager.class, new CounterManagerImpl(engine), null);

        LOGGER.info("Counter Manager bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Counter Manager bundle");

        engine.terminate();
        configurableBundle.close();
        registration.unregister();

        LOGGER.info("CounterManagerr bundle stopped");
    }
}
