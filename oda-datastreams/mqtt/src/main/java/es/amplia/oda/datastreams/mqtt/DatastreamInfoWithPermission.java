package es.amplia.oda.datastreams.mqtt;

import lombok.Value;

@Value
public class DatastreamInfoWithPermission {
    private String deviceId;
    private String datastreamId;
    private MqttDatastreamPermission permission;
}
