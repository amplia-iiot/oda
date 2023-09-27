package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.utils.Event;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class EventPublisherProxy implements EventPublisher {


    private final StateManagerProxy stateManager;


    public EventPublisherProxy(BundleContext bundleContext) {
        this.stateManager = new StateManagerProxy(bundleContext);
    }

    @Override
    public void publishEvents(String deviceId, String[] path, Map<String, Map<String, Map<Long, Object>>> events) {
        // parse events
        List<Event> eventsToPublish = new ArrayList<>();

        for (Map.Entry<String, Map<String, Map<Long, Object>>> eventsByDatastreamId : events.entrySet()) {
            for (Map.Entry<String, Map<Long, Object>> eventsFyFeed : eventsByDatastreamId.getValue().entrySet()) {
                for (Map.Entry<Long, Object> entry : eventsFyFeed.getValue().entrySet()) {
                    String datastreamId = eventsByDatastreamId.getKey();
                    String feed = eventsFyFeed.getKey();
                    Long at = entry.getKey();
                    Object value = entry.getValue();

                    // create new event and add to list
                    Event eventToAdd = new Event(datastreamId, deviceId, path, feed, at, value);
                    eventsToPublish.add(eventToAdd);
                }
            }
        }
        stateManager.onReceivedEvents(eventsToPublish);
    }

    @Override
    public void close() {
        stateManager.close();
    }
}
