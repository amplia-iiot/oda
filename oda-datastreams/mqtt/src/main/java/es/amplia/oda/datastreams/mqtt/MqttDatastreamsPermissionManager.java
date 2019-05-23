package es.amplia.oda.datastreams.mqtt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class MqttDatastreamsPermissionManager {

    private final Map<DatastreamInfo, MqttDatastreamPermission> permissions = new ConcurrentHashMap<>();

    void addPermission(String deviceId, String datastreamId, MqttDatastreamPermission permission) {
        permissions.put(new DatastreamInfo(deviceId, datastreamId), permission);
    }

    void removePermission(String deviceId, String datastreamId) {
        permissions.remove(new DatastreamInfo(deviceId, datastreamId));
    }

    void removeDevicePermissions(String deviceId) {
        permissions.keySet().stream()
                .filter(key -> deviceId.equals(key.getDeviceId()))
                .forEach(key -> removePermission(deviceId, key.getDatastreamId()));
    }

    boolean hasReadPermission(String deviceId, String datastreamId) {
        return permissions.getOrDefault(new DatastreamInfo(deviceId, datastreamId), MqttDatastreamPermission.NONE).isReadable();
    }

    boolean hasWritePermission(String deviceId, String datastreamId) {
        return permissions.getOrDefault(new DatastreamInfo(deviceId, datastreamId), MqttDatastreamPermission.NONE).isWritable();
    }
}
