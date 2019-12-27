package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.utils.*;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private static final int NUM_THREADS = 10;

    private DatastreamsGettersFinderImpl datastreamsGettersFinder;
    private EventPublisherProxy eventPublisher;
    private Scheduler scheduler;
    private ConfigurableBundle configurableBundle;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Poller Subsystem");

        ServiceLocator<DatastreamsGetter> datastreamsGettersLocator =
                new ServiceLocatorOsgi<>(bundleContext, DatastreamsGetter.class);
        datastreamsGettersFinder = new DatastreamsGettersFinderImpl(datastreamsGettersLocator);
        eventPublisher = new EventPublisherProxy(bundleContext);
        DatastreamsEvent datastreamsEvent = new PollerDatastreamsEvent(eventPublisher);
        Poller poller = new PollerImpl(datastreamsGettersFinder, datastreamsEvent);
        scheduler = new SchedulerImpl(Executors.newScheduledThreadPool(NUM_THREADS));
        PollerConfigurationUpdateHandler configHandler = new PollerConfigurationUpdateHandler(poller, scheduler);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        
        LOGGER.info("Poller Subsystem started");
    }

    @Override
    public void stop(BundleContext context) {
        LOGGER.info("Stopping Poller Subsystem");

        configurableBundle.close();
        scheduler.close();
        datastreamsGettersFinder.close();
        eventPublisher.close();

        LOGGER.info("Poller Subsystem stopped");
    }
}
