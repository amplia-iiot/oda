package es.amplia.oda.statemanager.api;

import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.event.api.Event;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface StateManager extends AutoCloseable {
    CompletableFuture<DatastreamValue> getDatastreamInformation(String deviceId, String datastreamId);
    CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(String deviceId, Set<String> datastreamIds);
    CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(DevicePattern devicePattern, String datastreamId);
    CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(DevicePattern devicePattern, Set<String> datastreamId);
    CompletableFuture<Set<DatastreamValue>> getDeviceInformation(String deviceId);
    CompletableFuture<DatastreamValue> setDatastreamValue(String deviceId, String datastreamId, Object value);
    CompletableFuture<Set<DatastreamValue>> setDatastreamValues(String deviceId, Map<String, Object> datastreamValues);
    void registerToEvents(EventHandler eventHandler);
    void unregisterToEvents(EventHandler eventHandler);
    void onReceivedEvent(Event event);
    void close();
}
