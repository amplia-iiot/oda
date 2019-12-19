package es.amplia.oda.subsystem.collector;

import es.amplia.oda.subsystem.collector.configuration.CollectorConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.event.api.EventDispatcherProxy;
import es.amplia.oda.statemanager.api.StateManagerProxy;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class Activator implements BundleActivator {

    private static final int NUM_THREADS = 10;
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(NUM_THREADS);


    private StateManagerProxy stateManager;
    private EventDispatcherProxy eventDispatcher;
    private Scheduler scheduler;
    private ConfigurableBundle configurableBundle;


    @Override
    public void start(BundleContext bundleContext) {
        stateManager = new StateManagerProxy(bundleContext);
        eventDispatcher = new EventDispatcherProxy(bundleContext);
        Collector collector = new CollectorImpl(stateManager, eventDispatcher);
        scheduler = new SchedulerImpl(executorService);
        ConfigurationUpdateHandler configHandler = new CollectorConfigurationUpdateHandler(collector, scheduler);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
    }

    @Override
    public void stop(BundleContext bundleContext) {
        configurableBundle.close();
        scheduler.close();
        stateManager.close();
        eventDispatcher.close();
    }
}
