package es.amplia.oda.datastreams.deviceinfoowa450.configuration;

import lombok.Value;

@Value
public class DeviceInfoConfiguration {
    private String deviceId;
    private String apiKey;
    private String source;
    private String path;
}
