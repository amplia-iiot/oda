package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.OpenGateConnectorProxy;
import es.amplia.oda.core.commons.osgi.proxies.OperationSenderProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.dispatcher.opengate.event.EventDispatcherFactoryImpl;
import es.amplia.oda.dispatcher.opengate.event.ResponseDispatcherImpl;
import es.amplia.oda.dispatcher.opengate.operation.processor.OpenGateOperationProcessorFactoryImpl;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.event.api.ResponseDispatcher;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private static final int NUM_THREADS = 10;

    private SerializerProviderOsgi serializerProvider;
    private DeviceInfoProviderProxy deviceInfoProvider;
    private OpenGateOperationProcessorFactory factory;

    private OpenGateConnectorProxy connector;
    private Scheduler scheduler;
    private ServiceRegistrationManager<EventDispatcher> eventDispatcherServiceRegistrationManager;
    private ConfigurableBundle configurableBundle;

    private ServiceRegistration<Dispatcher> operationDispatcherRegistration;
    private ServiceRegistration<ResponseDispatcher> responseDispatcherRegistration;
    
    private OperationSenderProxy opSender;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting OpenGate Dispatcher");

        serializerProvider = new SerializerProviderOsgi(bundleContext);
        deviceInfoProvider = new DeviceInfoProviderProxy(bundleContext);
        factory = new OpenGateOperationProcessorFactoryImpl(bundleContext);
        opSender = new OperationSenderProxy(bundleContext);
        Dispatcher dispatcher = new OpenGateOperationDispatcher(serializerProvider, deviceInfoProvider,
                factory.createOperationProcessor(), opSender);
        operationDispatcherRegistration = bundleContext.registerService(Dispatcher.class, dispatcher, null);

        scheduler = new SchedulerImpl(Executors.newScheduledThreadPool(NUM_THREADS));
        connector = new OpenGateConnectorProxy(bundleContext);

        ResponseDispatcherImpl respDispatcher = new ResponseDispatcherImpl(serializerProvider.getSerializer(ContentType.JSON), ContentType.JSON, connector);
        responseDispatcherRegistration = bundleContext.registerService(ResponseDispatcher.class, respDispatcher, null);

        EventDispatcherFactory eventDispatcherFactory =
                new EventDispatcherFactoryImpl(deviceInfoProvider, serializerProvider, connector, scheduler);
        eventDispatcherServiceRegistrationManager =
                new ServiceRegistrationManagerOsgi<>(bundleContext, EventDispatcher.class);
        DispatcherConfigurationUpdateHandler configHandler =
                new DispatcherConfigurationUpdateHandler(eventDispatcherFactory, scheduler,
                        eventDispatcherServiceRegistrationManager);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        
        LOGGER.info("OpenGate Dispatcher started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping OpenGate Json Dispatcher");

        configurableBundle.close();
        eventDispatcherServiceRegistrationManager.unregister();
        scheduler.close();
        connector.close();

        operationDispatcherRegistration.unregister();
        serializerProvider.close();
        deviceInfoProvider.close();
        factory.close();
        opSender.close();

        responseDispatcherRegistration.unregister();

        LOGGER.info("OpenGate Dispatcher stopped");
    }
}
