package es.amplia.oda.core.commons.interfaces;

import java.util.List;

public interface DatastreamsEvent {
    void registerToEventSource();
    void unregisterFromEventSource();
    void publish(String deviceId, String datastreamId, List<String> path, Long at, Object value);
}
