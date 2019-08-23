package es.amplia.oda.datastreams.deviceinfofx30.configuration;

import lombok.Value;

@Value
public class DeviceInfoFX30Configuration {
	private String deviceId;
	private String apiKey;

	private String path;
}
