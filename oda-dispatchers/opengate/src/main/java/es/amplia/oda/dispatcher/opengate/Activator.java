package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.OpenGateConnectorProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.Serializers;
import es.amplia.oda.dispatcher.opengate.operation.processor.OpenGateOperationProcessorFactoryImpl;
import es.amplia.oda.event.api.EventDispatcher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private static final int NUM_THREADS = 10;
    static final long STOP_PENDING_OPERATIONS_TIMEOUT = 10;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(NUM_THREADS);

    private SerializerProxy serializer;
    private DeviceInfoProviderProxy deviceInfoProvider;
    private OpenGateOperationProcessorFactory factory;

    private OpenGateConnectorProxy connector;
    private ConfigurableBundle configurableBundle;

    private ServiceRegistration<Dispatcher> operationDispatcherRegistration;
    private ServiceRegistration<EventDispatcher> eventDispatcherRegistration;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting OpenGate Dispatcher");

        serializer = new SerializerProxy(bundleContext, Serializers.SerializerType.JSON);
        deviceInfoProvider = new DeviceInfoProviderProxy(bundleContext);
        factory = new OpenGateOperationProcessorFactoryImpl(bundleContext, serializer);
        Dispatcher dispatcher =
                new OpenGateOperationDispatcher(serializer, deviceInfoProvider, factory.createOperationProcessor());
        operationDispatcherRegistration = bundleContext.registerService(Dispatcher.class, dispatcher, null);

        connector = new OpenGateConnectorProxy(bundleContext);
        OpenGateEventDispatcher eventDispatcher = new OpenGateEventDispatcher(deviceInfoProvider, serializer, connector);
        eventDispatcherRegistration = bundleContext.registerService(EventDispatcher.class, eventDispatcher, null);

        Scheduler scheduler = new SchedulerImpl(deviceInfoProvider, eventDispatcher, connector, serializer);
        DispatcherConfigurationUpdateHandler configHandler =
                new DispatcherConfigurationUpdateHandler(executor, eventDispatcher, scheduler);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        
        LOGGER.info("OpenGate Dispatcher started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping OpenGate Json Dispatcher");

        eventDispatcherRegistration.unregister();
        configurableBundle.close();
        stopPendingOperations();
        connector.close();

        operationDispatcherRegistration.unregister();
        serializer.close();
        deviceInfoProvider.close();
        factory.close();

        LOGGER.info("OpenGate Dispatcher stopped");
    }
    
    private void stopPendingOperations() {
        executor.shutdown();
        try {
            executor.awaitTermination(STOP_PENDING_OPERATIONS_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("The shutdown of the pool of threads took longer than {} seconds: {}",
                    STOP_PENDING_OPERATIONS_TIMEOUT, e);
            Thread.currentThread().interrupt();
        }
    }
}
