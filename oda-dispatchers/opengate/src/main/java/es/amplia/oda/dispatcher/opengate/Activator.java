package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.OpenGateConnectorProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.Serializers;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.operation.api.OperationRefreshInfo;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;
import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.api.osgi.proxies.OperationGetDeviceParametersProxy;
import es.amplia.oda.operation.api.osgi.proxies.OperationRefreshInfoProxy;
import es.amplia.oda.operation.api.osgi.proxies.OperationSetDeviceParametersProxy;
import es.amplia.oda.operation.api.osgi.proxies.OperationUpdateProxy;

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
    private static final int STOP_PENDING_OPERATIONS_TIMEOUT = 10;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(NUM_THREADS);

    private OpenGateConnectorProxy connector;
    private ConfigurableBundle configurableBundle;

    private ServiceRegistration<Dispatcher> operationDispatcherRegistration;
    private ServiceRegistration<EventDispatcher> eventDispatcherRegistration;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting OpenGate Dispatcher");

        Serializer serializer = new SerializerProxy(bundleContext, Serializers.SerializerType.JSON);
        OperationGetDeviceParameters operationGetDeviceParameters = new OperationGetDeviceParametersProxy(bundleContext);
        OperationSetDeviceParameters operationSetDeviceParameters = new OperationSetDeviceParametersProxy(bundleContext);
        OperationRefreshInfo operationRefreshInfo = new OperationRefreshInfoProxy(bundleContext);
        OperationUpdate operationUpdate = new OperationUpdateProxy(bundleContext);
        DeviceInfoProvider deviceInfoProvider = new DeviceInfoProviderProxy(bundleContext);
        Dispatcher dispatcher =
                new OpenGateOperationDispatcher(serializer, deviceInfoProvider,
                        operationGetDeviceParameters, operationSetDeviceParameters, operationRefreshInfo,
                        operationUpdate);
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
        operationDispatcherRegistration.unregister();
        configurableBundle.close();
        stopPendingOperations();
        connector.close();

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
