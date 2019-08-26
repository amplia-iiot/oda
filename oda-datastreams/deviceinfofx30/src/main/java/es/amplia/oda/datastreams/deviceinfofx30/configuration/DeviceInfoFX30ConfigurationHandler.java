package es.amplia.oda.datastreams.deviceinfofx30.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.deviceinfofx30.DeviceInfoFX30;

import java.util.Dictionary;

public class DeviceInfoFX30ConfigurationHandler implements ConfigurationUpdateHandler {

	private static final String DEVICE_ID_PROPERTY_NAME = "deviceId";
	private static final String API_KEY_PROPERTY_NAME = "apiKey";
	private static final String PATH_PROPERTY_NAME = "path";

	private final DeviceInfoFX30 deviceInfoFX30;

	private DeviceInfoFX30Configuration currentConfiguration;

	public DeviceInfoFX30ConfigurationHandler(DeviceInfoFX30 deviceInfoFX30) {
		this.deviceInfoFX30 = deviceInfoFX30;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		String deviceId = (String) props.get(DEVICE_ID_PROPERTY_NAME);
		String apiKey = (String) props.get(API_KEY_PROPERTY_NAME);

		String path = (String) props.get(PATH_PROPERTY_NAME);

		if (apiKey == null) {
			throw new ConfigurationException("Missing required field \"apiKey\"");
		}
		if (path == null) {
			throw new ConfigurationException("Missing required field \"path\"");
		}

		currentConfiguration = new DeviceInfoFX30Configuration(deviceId, apiKey, path);
	}

	@Override
	public void applyConfiguration() {
		deviceInfoFX30.loadConfiguration(currentConfiguration);
	}
}
