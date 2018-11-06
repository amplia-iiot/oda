package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.osgi.proxies.OpenGateConnectorProxy;
import es.amplia.oda.core.commons.utils.DatastreamSetterTypeMapperImpl;
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
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Activator implements BundleActivator, ManagedService {
    private static final Logger logger = LoggerFactory.getLogger(Activator.class);
    private static final int SCHEDULERS_THREAD_POOL_SIZE = 10;
    private static final int STOP_PENDING_OPERATIONS_TIMEOUT = 10;

    private ServiceRegistration<?> registration;
    private DatastreamSetterTypeMapperImpl datastreamsTypeMapper;
    private OpenGateConnectorProxy connector;
    private ServiceRegistration<EventDispatcher> eventDispatcherServiceRegistration;
    private OgJsonEventDispatcher eventDispatcher;
    private ScheduledExecutorService executor;
    private Scheduler scheduler;


    @Override
    public void start(BundleContext context) {
        logger.info("Starting OpenGate Json Dispatcher");
        datastreamsTypeMapper = new DatastreamSetterTypeMapperImpl(context);
        JsonParser jsonParser = new JsonParserImpl(datastreamsTypeMapper);
        JsonWriter jsonWriter = new JsonWriterImpl();
        OperationGetDeviceParameters operationGetDeviceParameters = new OperationGetDeviceParametersProxy(context);
        OperationSetDeviceParameters operationSetDeviceParameters = new OperationSetDeviceParametersProxy(context);
        OperationRefreshInfo operationRefreshInfo = new OperationRefreshInfoProxy(context);
        OperationUpdate operationUpdate = new OperationUpdateProxy(context);
        DeviceInfoProvider deviceInfoProvider = new DeviceInfoProviderProxy(context);
        Dispatcher dispatcher = new OgJsonDispatcher(jsonParser, jsonWriter, deviceInfoProvider, operationGetDeviceParameters, operationSetDeviceParameters, operationRefreshInfo, operationUpdate);
        registration = context.registerService(Dispatcher.class.getName(), dispatcher, null);
        connector = new OpenGateConnectorProxy(context);
        eventDispatcher = new OgJsonEventDispatcher(deviceInfoProvider, jsonWriter, connector);
        eventDispatcherServiceRegistration = context.registerService(EventDispatcher.class, eventDispatcher, null);
        scheduler = new Scheduler(deviceInfoProvider, eventDispatcher, connector, jsonWriter);
        
        Dictionary<String, String> props = new Hashtable<>();
        props.put("service.pid", "es.amplia.opengate.oda.dispatchers.og_json");
        context.registerService(ManagedService.class.getName(), this, props);
        
        logger.info("OpenGate Json Dispatcher started");
    }

    @Override
    public void stop(BundleContext context) {
        logger.info("Stopping OpenGate Json Dispatcher");
        if (registration != null) registration.unregister();
        if (datastreamsTypeMapper != null) datastreamsTypeMapper.close();
        if (eventDispatcherServiceRegistration != null) eventDispatcherServiceRegistration.unregister();
        if (connector != null) connector.close();
        registration = null;
        datastreamsTypeMapper = null;
        if(executor!=null) {
            stopPendingOperations();
        }
        executor = null;
        scheduler = null;
    }
    
    @Override
    public void updated(Dictionary<String, ?> properties) {
        if(properties==null) {
            logger.info("Collection subsystem updated with null properties");
            return;
        }
        
        logger.info("Dispatcher OG_JSON updated with {} properties", properties.size());
        if(executor!=null) {
            stopPendingOperations();
        }
        executor = new ScheduledThreadPoolExecutor(SCHEDULERS_THREAD_POOL_SIZE);

        eventDispatcher.setReduceBandwidthMode(ConfigurationParser.getReduceBandwidthMode(properties));

        Map<ConfigurationParser.Key, Set<String>> schedules  = new HashMap<>();
        ConfigurationParser.parse(properties, schedules);
        
        eventDispatcher.setDatastreamIdsConfigured(schedules.values());
        
        schedules.forEach((key,ids)->{
            logger.debug("Scheduling dispatch of ids={} strating in {} seconds, for every {} seconds", ids,
                    key.getSecondsFirstDispatch(), key.getSecondsBetweenDispatches());
            executor.scheduleAtFixedRate(() -> scheduler.runFor(ids), key.getSecondsFirstDispatch(),
                    key.getSecondsBetweenDispatches(), TimeUnit.SECONDS);
        });
    }
    
    private void stopPendingOperations() {
        long timeout = STOP_PENDING_OPERATIONS_TIMEOUT;
        
        executor.shutdown();
        try {
            executor.awaitTermination(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("The shutdown of the pool of threads its taking more than {} seconds. Will not wait longer.",
                    timeout);
            Thread.currentThread().interrupt();
        }
    }

}
