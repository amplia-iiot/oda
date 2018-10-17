package es.amplia.oda.datastreams.deviceinfo.configuration;

import lombok.Data;

/**
 * Device information configuration.
 */
@Data
public class DeviceInfoConfiguration {
    /**
     * Device identifier preconfigured.
     */
    private final String deviceId;
    /**
     * API Key.
     */
    private final String apiKey;
    /**
     * Platform command to get the serial number.
     */
    private final String serialNumberCommand;
}
