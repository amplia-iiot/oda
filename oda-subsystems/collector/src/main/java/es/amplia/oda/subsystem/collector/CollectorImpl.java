package es.amplia.oda.subsystem.collector;

import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import es.amplia.oda.statemanager.api.StateManager;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class CollectorImpl implements Collector {

    private final StateManager stateManager;
    private final EventDispatcher eventDispatcher;


    CollectorImpl(StateManager stateManager, EventDispatcher eventDispatcher) {
        this.stateManager = stateManager;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void collect(DevicePattern devicePattern, Set<String> datastreams) {
        stateManager.getDatastreamsInformation(devicePattern, datastreams)
                .thenApply(this::mapToEvents)
                .thenAccept(this::publishEvents);
    }

    private List<Event> mapToEvents(Set<DatastreamValue> datastreamValues) {
        return datastreamValues.stream()
                .filter(this::isOkValue)
                .map(this::mapToEvent)
                .collect(Collectors.toList());
    }

    private boolean isOkValue(DatastreamValue datastreamValue) {
        return Status.OK.equals(datastreamValue.getStatus());
    }

    private Event mapToEvent(DatastreamValue datastreamValue) {
        return new Event(datastreamValue.getDatastreamId(), datastreamValue.getDeviceId(), null,
                datastreamValue.getAt(), datastreamValue.getValue());
    }

    private void publishEvents(List<Event> events) {
        eventDispatcher.publish(events);
    }
}
