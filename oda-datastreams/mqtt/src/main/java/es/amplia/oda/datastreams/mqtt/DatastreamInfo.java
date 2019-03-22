package es.amplia.oda.datastreams.mqtt;

import lombok.Value;

@Value
public class DatastreamInfo {
    private String deviceId;
    private String datastreamId;
}
