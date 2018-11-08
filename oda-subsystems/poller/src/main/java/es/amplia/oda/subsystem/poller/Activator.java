package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocatorOsgi;
import es.amplia.oda.event.api.EventDispatcherProxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private static final int NUM_THREADS = 10;
    private static final int STOP_OPERATIONS_TIMEOUT = 10;


    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(NUM_THREADS);

    private EventDispatcherProxy eventDispatcher;
    private ConfigurableBundle configurableBundle;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Poller Subsystem");

        DatastreamsGettersLocator datastreamsGettersLocator = new DatastreamsGettersLocatorOsgi(bundleContext);
        DatastreamsGetterFinder datastreamsGetterFinder = new DatastreamsGetterFinderImpl(datastreamsGettersLocator);
        eventDispatcher = new EventDispatcherProxy(bundleContext);
        Poller poller = new PollerImpl(datastreamsGetterFinder, eventDispatcher);
        PollerConfigurationUpdateHandler configHandler = new PollerConfigurationUpdateHandler(executor, poller);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        
        LOGGER.info("Poller Subsystem started");
    }

    @Override
    public void stop(BundleContext context) {
        LOGGER.info("Stopping Poller Subsystem");

        configurableBundle.close();
        stopPendingOperations();
        eventDispatcher.close();

        LOGGER.info("Poller Subsystem stopped");
    }

    private void stopPendingOperations() {
        executor.shutdown();
        try {
            executor.awaitTermination(STOP_OPERATIONS_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("The shutdown of the pool of threads its taking more than {} seconds. Will not wait longer.",
                    STOP_OPERATIONS_TIMEOUT);
            Thread.currentThread().interrupt();
        }
    }
}
