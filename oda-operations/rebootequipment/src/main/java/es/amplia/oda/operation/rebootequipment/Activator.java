package es.amplia.oda.operation.rebootequipment;

import es.amplia.oda.core.commons.osgi.proxies.OpenGateConnectorProxy;
import es.amplia.oda.core.commons.utils.MapBasedDictionary;
import es.amplia.oda.event.api.ResponseDispatcherProxy;
import es.amplia.oda.operation.api.CustomOperation;
import java.util.Dictionary;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<CustomOperation> registration;
    private ServiceRegistration<EventHandler> eventHandlerServiceRegistration;
    private ResponseDispatcherProxy responseDispatcher;
    private OpenGateConnectorProxy ogConnector;

    @Override
    public void start(BundleContext context) {
        LOGGER.info("Starting Operation RebootEquipment Activator");
        responseDispatcher = new ResponseDispatcherProxy(context);
        ogConnector = new OpenGateConnectorProxy(context);
        RebootEquipmentImpl rebootEquipment = new RebootEquipmentImpl(context, responseDispatcher, ogConnector);
        String[] updateTopics =
                new String[] { RebootEquipmentImpl.STARTED_BUNDLE_EVENT };
        Dictionary<String, Object> props = new MapBasedDictionary<>(String.class);
        props.put(EventConstants.EVENT_TOPIC, updateTopics);
        eventHandlerServiceRegistration =
                context.registerService(EventHandler.class, rebootEquipment, props);
        registration = context.registerService(CustomOperation.class, rebootEquipment, null);
        LOGGER.info("Operation RebootEquipment started");
    }

    @Override
    public void stop(BundleContext context) {
        LOGGER.info("Stopping Operation RebootEquipment Activator");
        registration.unregister();
        eventHandlerServiceRegistration.unregister();
        responseDispatcher.close();
        ogConnector.close();
        LOGGER.info("Operation RebootEquipment stopped");
    }
}
