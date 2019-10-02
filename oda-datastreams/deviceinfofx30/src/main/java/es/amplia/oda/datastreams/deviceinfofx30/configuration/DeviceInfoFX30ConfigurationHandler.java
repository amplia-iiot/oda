package es.amplia.oda.datastreams.deviceinfofx30.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.deviceinfofx30.DeviceInfoFX30;
import es.amplia.oda.datastreams.deviceinfofx30.ScriptsLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Optional;

public class DeviceInfoFX30ConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceInfoFX30ConfigurationHandler.class);

	static final String DEVICE_ID_PROPERTY_NAME = "deviceId";
	static final String API_KEY_PROPERTY_NAME = "apiKey";
	static final String SOURCE_PROPERTY_NAME = "source";
	static final String PATH_PROPERTY_NAME = "path";

	private final ScriptsLoader scriptsLoader;
	private final DeviceInfoFX30 deviceInfoFX30;

	private DeviceInfoFX30Configuration currentConfiguration;

	public DeviceInfoFX30ConfigurationHandler(ScriptsLoader scriptsLoader, DeviceInfoFX30 deviceInfoFX30) {
		this.scriptsLoader = scriptsLoader;
		this.deviceInfoFX30 = deviceInfoFX30;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		String deviceId = (String) props.get(DEVICE_ID_PROPERTY_NAME);
		String apiKey = Optional.ofNullable((String) props.get(API_KEY_PROPERTY_NAME))
				.orElseThrow(() -> missingRequiredField(API_KEY_PROPERTY_NAME));
		String source = Optional.ofNullable((String) props.get(SOURCE_PROPERTY_NAME))
				.orElseThrow(() -> missingRequiredField(SOURCE_PROPERTY_NAME));
		String path = Optional.ofNullable((String) props.get(PATH_PROPERTY_NAME))
				.orElseThrow(() -> missingRequiredField(PATH_PROPERTY_NAME));

		currentConfiguration = new DeviceInfoFX30Configuration(deviceId, apiKey, source, path);
	}

	private ConfigurationException missingRequiredField(String field) {
		return new ConfigurationException("Missing required field " + field);
	}

	@Override
	public void applyConfiguration() {
		try {
			scriptsLoader.loadScripts(currentConfiguration.getSource(), currentConfiguration.getPath());
			deviceInfoFX30.loadConfiguration(currentConfiguration);
		} catch (CommandExecutionException | IOException e) {
			LOGGER.error(e.getMessage());
		}
	}
}
