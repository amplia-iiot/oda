package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.dispatcher.opengate.EventCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EventCollectorImpl implements EventCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventCollectorImpl.class);


    private final EventDispatcherImpl eventDispatcher;
    private final List<String> datastreamIdsToCollect = new ArrayList<>();
    private final Map<String, List<Event>> collectedEvents = new HashMap<>();


    EventCollectorImpl(EventDispatcherImpl eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void loadDatastreamIdsToCollect(Collection<String> datastreamIds) {
        datastreamIdsToCollect.clear();
        datastreamIdsToCollect.addAll(datastreamIds);
    }

    @Override
    public void publish(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        List<Event> eventsToPublish = new ArrayList<>();
        for (Event event : events) {
            if (isEventFromCollectedDatastream(event)) {
                LOGGER.info("Collected event {} of datastream {}", event, event.getDatastreamId());
                collect(event);
            } else {
                eventsToPublish.add(event);
            }
        }
        eventDispatcher.publish(eventsToPublish);
    }

    @Override
    public void publishImmediately(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        eventDispatcher.publishImmediately(events);
    }

    @Override
    public void publishSameThreadNoQos(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        eventDispatcher.publishSameThreadNoQos(events);
    }


    private boolean isEventFromCollectedDatastream(Event event) {
        return datastreamIdsToCollect.contains(event.getDatastreamId());
    }

    private void collect(Event event) {
        collectedEvents.merge(event.getDatastreamId(), Collections.singletonList(event), this::joinLists);
    }

    private List<Event> joinLists(List<Event> list1, List<Event> list2) {
        ArrayList<Event> jointList = new ArrayList<>(list1);
        jointList.addAll(list2);
        return jointList;
    }

    @Override
    public void publishCollectedEvents(Collection<String> datastreamIds) {

        for (String datastreamId : datastreamIds) {
            List<Event> events = collectedEvents.remove(datastreamId);
            if (events != null) {
                eventDispatcher.publish(events);
            } else {
                LOGGER.info("No events collected for {}", datastreamId);
            }
        }
    }
}
