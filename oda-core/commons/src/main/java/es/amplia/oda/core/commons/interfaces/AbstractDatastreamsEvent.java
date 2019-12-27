package es.amplia.oda.core.commons.interfaces;

import java.util.List;
import java.util.Optional;

public abstract class AbstractDatastreamsEvent implements DatastreamsEvent {

    private final EventPublisher eventPublisher;

    public AbstractDatastreamsEvent(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(String deviceId, String datastreamId, List<String> path, Long at, Object value) {
        String[] pathArray = Optional.ofNullable(path).map(list -> list.toArray(new String[0])).orElse(null);
        eventPublisher.publishEvent(deviceId, datastreamId, pathArray, at, value);
    }
}
