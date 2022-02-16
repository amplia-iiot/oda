package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.EventPublisher;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static es.amplia.oda.core.commons.utils.Events.*;

public class EventPublisherProxy implements EventPublisher {

    private final EventAdminProxy eventAdmin;


    public EventPublisherProxy(BundleContext bundleContext) {
        this.eventAdmin = new EventAdminProxy(bundleContext);
    }

    @Override
    public void publishEvents(String deviceId, String[] path, Map<String, Map<Long,Object>> events) {
        List<Map<String, Object>> eventList = new ArrayList<>();
        events.entrySet().stream()
                        .forEach(event -> {
                            Map<String, Object> eventProperties = new HashMap<>();
                            for(Map.Entry entry : event.getValue().entrySet()) {
                                eventProperties.put(DEVICE_ID_PROPERTY_NAME, deviceId);
                                eventProperties.put(DATASTREAM_ID_PROPERTY_NAME, event.getKey());
                                eventProperties.put(PATH_PROPERTY_NAME, path);
                                eventProperties.put(AT_PROPERTY_NAME, entry.getKey());
                                eventProperties.put(VALUE_PROPERTY_NAME, entry.getValue());
                                eventList.add(eventProperties);
                            }
                        });
        Map<String, List> eventsObject = new HashMap<>();
        eventsObject.put("events", eventList);

        Event event = new Event(EVENT_TOPIC, eventsObject);

        eventAdmin.sendEvent(event);
    }

    @Override
    public void close() {
        eventAdmin.close();
    }
}
