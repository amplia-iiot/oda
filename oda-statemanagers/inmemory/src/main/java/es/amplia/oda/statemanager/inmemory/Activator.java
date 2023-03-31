package es.amplia.oda.statemanager.inmemory;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.event.api.EventDispatcherProxy;
import es.amplia.oda.ruleengine.api.RuleEngineProxy;
import es.amplia.oda.statemanager.inmemory.configuration.StateManagerInMemoryConfigurationHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private DatastreamsSettersFinder datastreamsSettersFinder;
    private RuleEngineProxy ruleEngine;
    private EventDispatcherProxy eventDispatcher;
    private ServiceRegistration<StateManager> stateManagerRegistration;
    private ConfigurableBundle configurableBundle;
    private static final int NUM_THREADS = 10;
    private static final int MAX_SIZE_THREADS_QUEUE = 1000;

    private Scheduler scheduler;
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("In Memory State Manager is starting");

        // create threads pool to process events received
        ExecutorService executor = new ThreadPoolExecutor(NUM_THREADS, NUM_THREADS,
                0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(MAX_SIZE_THREADS_QUEUE));
        // create scheduler to manage periodic tasks
        scheduler = new SchedulerImpl(executorService);
        ServiceLocator<DatastreamsSetter> datastreamsSettersLocator =
                new ServiceLocatorOsgi<>(bundleContext, DatastreamsSetter.class);
        datastreamsSettersFinder = new DatastreamsSettersFinderImpl(datastreamsSettersLocator);
        eventDispatcher = new EventDispatcherProxy(bundleContext);
        ruleEngine = new RuleEngineProxy(bundleContext);
        SerializerProxy serializer = new SerializerProxy(bundleContext, ContentType.JSON);
        InMemoryStateManager inMemoryStateManager =
                new InMemoryStateManager(datastreamsSettersFinder, eventDispatcher, ruleEngine, serializer, executor, scheduler);
        ConfigurationUpdateHandler configurationUpdateHandler = new StateManagerInMemoryConfigurationHandler(inMemoryStateManager);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configurationUpdateHandler);
        stateManagerRegistration = bundleContext.registerService(StateManager.class, inMemoryStateManager, null);

        LOGGER.info("In Memory State Manager started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("In Memory State Manager is stopping");

        configurableBundle.close();
        stateManagerRegistration.unregister();
        datastreamsSettersFinder.close();
        ruleEngine.close();
        eventDispatcher.close();
        scheduler.close();

        LOGGER.info("In Memory State Manager is stopped");
    }
}
