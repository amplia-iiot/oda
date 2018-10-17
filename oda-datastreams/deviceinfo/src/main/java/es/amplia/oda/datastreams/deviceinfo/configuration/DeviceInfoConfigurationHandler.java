package es.amplia.oda.datastreams.deviceinfo.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter;

import java.util.Dictionary;

/**
 * Device Info configuration handler.
 */
public class DeviceInfoConfigurationHandler implements ConfigurationUpdateHandler {

    /**
     * Configured property names.
     */
    static final String DEVICE_ID_PROPERTY_NAME = "deviceId";
    static final String API_KEY_PROPERTY_NAME = "apiKey";
    static final String SERIAL_NUMBER_COMMAND_PROPERTY_NAME = "serialNumberCommand";

    /**
     * Device info datastreams getter to configure.
     */
    private final DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter;

    /**
     * Current configuration.
     */
    private DeviceInfoConfiguration currentConfiguration;

    /**
     * Constructor.
     * @param deviceInfoDatastreamsGetter Device info datastreams getter to configure
     */
    public DeviceInfoConfigurationHandler(DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter) {
        this.deviceInfoDatastreamsGetter = deviceInfoDatastreamsGetter;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        String deviceId = (String) props.get(DEVICE_ID_PROPERTY_NAME);
        String apiKey = (String) props.get(API_KEY_PROPERTY_NAME);
        String serialNumberCommand = (String) props.get(SERIAL_NUMBER_COMMAND_PROPERTY_NAME);

        if (apiKey == null) {
            throw new ConfigurationException("Missing required field \"apiKey\"");
        }
        if (serialNumberCommand == null) {
            throw new ConfigurationException("Missing required field \"serialNumberCommand\"");
        }

        currentConfiguration = new DeviceInfoConfiguration(deviceId, apiKey, serialNumberCommand);
    }

    @Override
    public void applyConfiguration() throws Exception {
        deviceInfoDatastreamsGetter.loadConfiguration(currentConfiguration);
    }
}
