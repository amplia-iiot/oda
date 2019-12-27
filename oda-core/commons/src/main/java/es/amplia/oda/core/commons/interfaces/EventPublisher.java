package es.amplia.oda.core.commons.interfaces;

public interface EventPublisher extends AutoCloseable {

    void publishEvent(String deviceId, String datastreamId, String[] path, Long at, Object value);
    @Override
    void close();
}
