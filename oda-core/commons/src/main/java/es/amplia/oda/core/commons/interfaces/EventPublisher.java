package es.amplia.oda.core.commons.interfaces;

import java.util.Map;

public interface EventPublisher extends AutoCloseable {

    void publishEvents(String deviceId, String[] path, Map<String, Map<String, Map<Long,Object>>> events);
    @Override
    void close();
}
