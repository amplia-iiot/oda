package es.amplia.oda.core.commons.interfaces;

import java.util.List;
import java.util.Map;

public interface DatastreamsEvent {
    void registerToEventSource();
    void unregisterFromEventSource();
    void publish(String deviceId, String datastreamId, List<String> path, Long at, Object value);
    void publishGroup(String deviceId, List<String> path, Map<String, Map<Long, Object>> events);
}
