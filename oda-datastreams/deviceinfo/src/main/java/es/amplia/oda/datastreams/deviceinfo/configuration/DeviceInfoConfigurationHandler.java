package es.amplia.oda.datastreams.deviceinfo.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;

public class DeviceInfoConfigurationHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceInfoConfigurationHandler.class);

    static final String DEVICE_ID_PROPERTY_NAME = "deviceId";
    static final String API_KEY_PROPERTY_NAME = "apiKey";
    static final String SERIAL_NUMBER_COMMAND_PROPERTY_NAME = "serialNumberCommand";
    static final String PATH_PROPERTY_NAME = "path";

    private final DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter;
    private final ScriptsLoader scriptsLoader;

    private DeviceInfoConfiguration currentConfiguration;

    public DeviceInfoConfigurationHandler(DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter, ScriptsLoader scriptsLoader) {
        this.deviceInfoDatastreamsGetter = deviceInfoDatastreamsGetter;
        this.scriptsLoader = scriptsLoader;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        String deviceId = (String) props.get(DEVICE_ID_PROPERTY_NAME);
        String apiKey = (String) props.get(API_KEY_PROPERTY_NAME);
        String serialNumberCommand = (String) props.get(SERIAL_NUMBER_COMMAND_PROPERTY_NAME);
        String path = (String) props.get(PATH_PROPERTY_NAME);

        if (apiKey == null) {
            throw new ConfigurationException("Missing required field \"apiKey\"");
        }
        if (serialNumberCommand == null) {
            throw new ConfigurationException("Missing required field \"serialNumberCommand\"");
        }
        if (path == null) {
            throw new ConfigurationException("Missing required field \"path\"");
        }

        currentConfiguration = new DeviceInfoConfiguration(deviceId, apiKey, serialNumberCommand, path);
    }

    @Override
    public void applyConfiguration() {

        try {
            scriptsLoader.load(currentConfiguration.getPath());
            deviceInfoDatastreamsGetter.loadConfiguration(currentConfiguration);
        } catch (CommandExecutionException | IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
