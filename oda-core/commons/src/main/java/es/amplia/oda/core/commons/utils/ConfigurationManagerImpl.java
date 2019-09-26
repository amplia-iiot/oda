package es.amplia.oda.core.commons.utils;

import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ConfigurationManagerImpl implements ConfigurationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManagerImpl.class);

    static final String FILENAME_KEY = "felix.fileinstall.filename";
    static final String ODA_BUNDLES_CONFIGURATION_FOLDER_PROPERTY_NAME = "oda.bundles.configuration.folder";


    private final String configurationFolder;
    private final ConfigurationAdmin configAdmin;


    public ConfigurationManagerImpl(ConfigurationAdmin configAdmin) {
        this.configurationFolder = System.getProperty(ODA_BUNDLES_CONFIGURATION_FOLDER_PROPERTY_NAME);
        this.configAdmin = configAdmin;
    }

    @Override
    public Optional<String> getConfiguration(String pid, String property) throws IOException {
        // configuration is never null. Config Admin returns the current configuration or a new one
        Configuration configuration = configAdmin.getConfiguration(pid);
        Optional<Dictionary<String, Object>> properties = Optional.ofNullable(configuration.getProperties());
        return properties.flatMap(value -> Optional.ofNullable((String) value.get(property)));
    }

    @Override
    public void updateConfiguration(String pid, String property, Object value) throws IOException {
        // configuration is never null. Config Admin returns the current configuration or a new one
        Configuration configuration = configAdmin.getConfiguration(pid);
        Dictionary<String, Object> properties = getServiceProperties(pid, configuration);

        properties.put(property, value.toString());
        configuration.update(properties);

        LOGGER.info("Property {} from service {} updated with value {}", property, pid, value);
    }

    private Dictionary<String, Object> getServiceProperties(String pid, Configuration configuration) {
        Dictionary<String, Object> properties = configuration.getProperties();

        if (properties == null) {
            LOGGER.info("No properties found for service {}. Creating properties", pid);
            properties = new MapBasedDictionary<>(String.class);
            properties.put(Constants.SERVICE_PID, pid);
            properties.put(FILENAME_KEY, getConfigLocation(configuration, pid));
        }
        return properties;
    }

    private String getConfigLocation(Configuration configuration, String pid) {
        String configFilesLocation = configurationFolder;
        if (configFilesLocation == null) {
            String bundleLocation = configuration.getBundleLocation();
            String defaultOdaBundlesLocation = bundleLocation.substring(0, bundleLocation.lastIndexOf('/'));
            configFilesLocation = defaultOdaBundlesLocation + "/../configuration";
        }
        return configFilesLocation + "/" + pid + ".cfg";
    }

    @Override
    public void updateConfiguration(String pid, Map<String, Object> newProperties) throws IOException {
        // configuration is never null. Config Admin returns the current configuration or a new one
        Configuration configuration = configAdmin.getConfiguration(pid);
        Dictionary<String, Object> properties = getServiceProperties(pid, configuration);

        addNewProperties(newProperties, properties);
        configuration.update(properties);

        LOGGER.info("Updated properties of service {} with {}", pid, newProperties);
    }

    private void addNewProperties(Map<String, Object> newProperties, Dictionary<String, Object> properties) {
        for (Map.Entry<String, Object> entry : newProperties.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void replaceConfiguration(String pid, Map<String,Object> newProperties) throws IOException {
        // configuration is never null. Config Admin returns the current configuration or a new one
        Configuration configuration = configAdmin.getConfiguration(pid);

        Dictionary<String, Object> properties = getServiceProperties(pid, configuration);
        clearServiceProperties(properties);
        addNewProperties(newProperties, properties);
        configuration.update(properties);

        LOGGER.info("Replaced properties of service {} with {}", pid, newProperties);
    }

    private void clearServiceProperties(Dictionary<String, Object> properties) {
        if (properties != null) {
            List<String> keysToRemove = new ArrayList<>();
            Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (!key.equals(Constants.SERVICE_PID) && !key.equals(FILENAME_KEY)) {
                    keysToRemove.add(key);
                }
            }
            keysToRemove.forEach(properties::remove);
        }
    }

    @Override
    public void clearConfiguration(String pid) throws IOException {
        // configuration is never null. Config Admin returns the current configuration or a new one
        Configuration configuration = configAdmin.getConfiguration(pid);
        Dictionary<String, Object> properties = configuration.getProperties();
        clearServiceProperties(properties);
        configuration.update(properties);

        LOGGER.info("Clear properties of service {}", pid);
    }
}
