package es.amplia.oda.statemanager.api;

import es.amplia.oda.core.commons.utils.MapBasedDictionary;
import es.amplia.oda.event.api.Event;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import static es.amplia.oda.core.commons.utils.Events.*;

public class OsgiEventHandler implements EventHandler {

    private final ServiceRegistration<org.osgi.service.event.EventHandler> registration;

    private StateManager stateManager;

    public OsgiEventHandler(BundleContext bundleContext) {
        Dictionary<String, Object> props = new MapBasedDictionary<>(String.class);
        props.put(EventConstants.EVENT_TOPIC, new String[] { EVENT_TOPIC });
        registration =
                bundleContext.registerService(org.osgi.service.event.EventHandler.class, new EventHandlerImpl(), props);
    }

    @Override
    public void registerStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public void unregisterStateManager() {
        this.stateManager = null;
    }

    class EventHandlerImpl implements org.osgi.service.event.EventHandler {
        @Override
        public void handleEvent(org.osgi.service.event.Event osgiEvent) {
            if(osgiEvent.containsProperty("events")) {
                // List of events
                List<Event> events = new ArrayList<>();
                List<HashMap<String, Object>> parameters = (List<HashMap<String, Object>>) osgiEvent.getProperty("events");
                parameters.forEach(event -> {
                    String datastreamId = (String) event.get(DATASTREAM_ID_PROPERTY_NAME);
                    String deviceId = (String) event.get(DEVICE_ID_PROPERTY_NAME);
                    String[] path = (String[]) event.get(PATH_PROPERTY_NAME);
                    Long at = (Long) event.get(AT_PROPERTY_NAME);
                    Object value = event.get(VALUE_PROPERTY_NAME);
                    Event eventToAdd = new Event(datastreamId, deviceId, path, at, value);
                    events.add(eventToAdd);
                });

                notifyStateManager(events);
            } else {
                // Single event
                String datastreamId = (String) osgiEvent.getProperty(DATASTREAM_ID_PROPERTY_NAME);
                String deviceId = (String) osgiEvent.getProperty(DEVICE_ID_PROPERTY_NAME);
                String[] path = (String[]) osgiEvent.getProperty(PATH_PROPERTY_NAME);
                Long at = (Long) osgiEvent.getProperty(AT_PROPERTY_NAME);
                Object value = osgiEvent.getProperty(VALUE_PROPERTY_NAME);
                Event event = new Event(datastreamId, deviceId, path, at, value);

                notifyStateManager(event);
            }
        }

        private void notifyStateManager(es.amplia.oda.event.api.Event event) {
            if (stateManager != null) {
                stateManager.onReceivedEvent(event);
            }
        }

        private void notifyStateManager(List<es.amplia.oda.event.api.Event> events) {
            if (stateManager != null) {
                stateManager.onReceivedEvents(events);
            }
        }
    }

    @Override
    public void close() {
        registration.unregister();
    }
}
