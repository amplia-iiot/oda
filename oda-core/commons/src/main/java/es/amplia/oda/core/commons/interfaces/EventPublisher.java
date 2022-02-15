package es.amplia.oda.core.commons.interfaces;

import java.util.Map;

public interface EventPublisher extends AutoCloseable {

    void publishEvent(String deviceId, String datastreamId, String[] path, Long at, Object value);
    void publishGroupEvents(String deviceId, String[] path, Map<String, Map<Long,Object>> events);
    @Override
    void close();
}
