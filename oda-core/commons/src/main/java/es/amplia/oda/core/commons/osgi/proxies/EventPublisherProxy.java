package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.EventPublisher;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;

import java.util.HashMap;
import java.util.Map;

import static es.amplia.oda.core.commons.utils.Events.*;

public class EventPublisherProxy implements EventPublisher {

    private final EventAdminProxy eventAdmin;


    public EventPublisherProxy(BundleContext bundleContext) {
        this.eventAdmin = new EventAdminProxy(bundleContext);
    }

    @Override
    public void publishEvent(String deviceId, String datastreamId, String[] path, Long at, Object value) {
        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(DEVICE_ID_PROPERTY_NAME, deviceId);
        eventProperties.put(DATASTREAM_ID_PROPERTY_NAME, datastreamId);
        eventProperties.put(PATH_PROPERTY_NAME, path);
        eventProperties.put(AT_PROPERTY_NAME, at);
        eventProperties.put(VALUE_PROPERTY_NAME, value);
        Event event = new Event(EVENT_TOPIC, eventProperties);

        eventAdmin.sendEvent(event);
    }

    @Override
    public void close() {
        eventAdmin.close();
    }
}
