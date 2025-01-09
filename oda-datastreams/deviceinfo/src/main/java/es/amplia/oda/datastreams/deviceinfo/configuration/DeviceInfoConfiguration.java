package es.amplia.oda.datastreams.deviceinfo.configuration;

import java.util.Map;

import lombok.Value;

@Value
public class DeviceInfoConfiguration {
    private String deviceId;
    private String apiKey;
    private String source;
    private String path;
    private Map<String,String> dsScript;
}
