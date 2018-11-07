package es.amplia.oda.datastreams.deviceinfo.configuration;

import lombok.Value;

@Value
public class DeviceInfoConfiguration {
    private String deviceId;
    private String apiKey;
    private String serialNumberCommand;
}
