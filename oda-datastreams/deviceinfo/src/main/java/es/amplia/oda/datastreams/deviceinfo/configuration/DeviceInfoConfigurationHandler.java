package es.amplia.oda.datastreams.deviceinfo.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.deviceinfo.DeviceInfoDatastreamsGetter;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

public class DeviceInfoConfigurationHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceInfoConfigurationHandler.class);

    static final String DEVICE_ID_PROPERTY_NAME = "deviceId";
    static final String API_KEY_PROPERTY_NAME = "apiKey";
    static final String SOURCE_PROPERTY_NAME = "source";
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
        props.remove(DEVICE_ID_PROPERTY_NAME);
        String apiKey = (String) props.get(API_KEY_PROPERTY_NAME);
        props.remove(API_KEY_PROPERTY_NAME);
        String source = (String) props.get(SOURCE_PROPERTY_NAME);
        props.remove(SOURCE_PROPERTY_NAME);
        String path = (String) props.get(PATH_PROPERTY_NAME);
        props.remove(PATH_PROPERTY_NAME);

        if (apiKey == null) {
            throw new ConfigurationException("Missing required field \"apiKey\"");
        }
        if (source == null) {
            throw new ConfigurationException("Missing required field \"serialNumberCommand\"");
        }
        if (path == null) {
            throw new ConfigurationException("Missing required field \"path\"");
        }

        Enumeration<String> keys = props.keys();
        HashMap<String, String> scripts = new HashMap<>();
        while (keys.hasMoreElements()) {
            String datastremId = keys.nextElement();
            scripts.put(datastremId, (String) props.get(datastremId));
        }

        currentConfiguration = new DeviceInfoConfiguration(deviceId, apiKey, source, path, scripts);
    }

    @Override
    public void notifyConfigurationFilePath(String path) {
        deviceInfoDatastreamsGetter.setConfigFilePath(path);
    }

    @Override
    public void applyConfiguration() {
            scriptsLoader.load(currentConfiguration.getSource(), currentConfiguration.getPath());
            deviceInfoDatastreamsGetter.loadConfiguration(currentConfiguration);
    }
}
