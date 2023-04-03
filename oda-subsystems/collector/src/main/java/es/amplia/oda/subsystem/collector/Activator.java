package es.amplia.oda.subsystem.collector;

import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import es.amplia.oda.subsystem.collector.configuration.CollectorConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.event.api.EventDispatcherProxy;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private static final int NUM_THREADS = 10;
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUM_THREADS);


    private StateManagerProxy stateManager;
    private EventDispatcherProxy eventDispatcher;
    private Scheduler scheduler;
    private ConfigurableBundle configurableBundle;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Collector subsystem bundle");
        stateManager = new StateManagerProxy(bundleContext);
        eventDispatcher = new EventDispatcherProxy(bundleContext);
        Collector collector = new CollectorImpl(stateManager, eventDispatcher);
        scheduler = new SchedulerImpl(executorService);
        ConfigurationUpdateHandler configHandler = new CollectorConfigurationUpdateHandler(collector, scheduler);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        LOGGER.info("Collector subsystem bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Collector subsystem bundle");
        configurableBundle.close();
        scheduler.close();
        stateManager.close();
        eventDispatcher.close();
        LOGGER.info("Collector subsystem bundle stopped");
    }
}
