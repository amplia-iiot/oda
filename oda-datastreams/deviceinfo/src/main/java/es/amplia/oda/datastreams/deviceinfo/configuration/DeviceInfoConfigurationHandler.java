package es.amplia.oda.datastreams.deviceinfo.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter;

import java.util.Dictionary;

public class DeviceInfoConfigurationHandler implements ConfigurationUpdateHandler {

    static final String DEVICE_ID_PROPERTY_NAME = "deviceId";
    static final String API_KEY_PROPERTY_NAME = "apiKey";
    static final String SERIAL_NUMBER_COMMAND_PROPERTY_NAME = "serialNumberCommand";

    private final DeviceInfoDatastreamsGetter deviceInfoDatastreamsGetter;

    private DeviceInfoConfiguration currentConfiguration;

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
    public void applyConfiguration() {
        deviceInfoDatastreamsGetter.loadConfiguration(currentConfiguration);
    }
}
