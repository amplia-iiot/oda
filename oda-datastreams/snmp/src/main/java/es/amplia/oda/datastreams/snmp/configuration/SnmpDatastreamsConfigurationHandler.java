package es.amplia.oda.datastreams.snmp.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.snmp.SnmpEntry;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.snmp.internal.SnmpDatastreamsManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class SnmpDatastreamsConfigurationHandler implements ConfigurationUpdateHandler {

    private static final String OID_PROPERTY_NAME = "oid";
    private static final String DEVICE_PROPERTY_NAME = "device";
    private static final String DATA_TYPE_PROPERTY_NAME = "dataType";
    private static final String DATASTREAM_PROPERTY_NAME = "datastream";
    private static final String FEED_PROPERTY_NAME = "feed";

    // Current configuration.
    List<SnmpEntry> currentSnmpRecollection = new ArrayList<>();

    private final SnmpDatastreamsManager datastreamsManager;

    public SnmpDatastreamsConfigurationHandler(SnmpDatastreamsManager datastreamsManager) {
        this.datastreamsManager = datastreamsManager;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        clearLastConfiguration();

        Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);
        for (Map.Entry<String, ?> entry : mappedProperties.entrySet()) {
            try {
               // key is oid,deviceId
                String[] keyProperties = getTokensFromProperty(entry.getKey());
                String oid = keyProperties[0].trim();
                String deviceId = keyProperties[1].trim();

                // properties are dataType, datastreamId, feed
                String[] propertyTokens = getTokensFromProperty((String) entry.getValue());
                String dataType = getValueByToken(DATA_TYPE_PROPERTY_NAME, propertyTokens)
                        .orElseThrow(throwMissingRequiredPropertyConfigurationException(DATA_TYPE_PROPERTY_NAME));
                String datastreamId = getValueByToken(DATASTREAM_PROPERTY_NAME, propertyTokens)
                        .orElseThrow(throwMissingRequiredPropertyConfigurationException(DATASTREAM_PROPERTY_NAME));
                String feed = getValueByToken(FEED_PROPERTY_NAME, propertyTokens).orElse(null);

                // create entry
                // TODO: check that doesn't already exist an entry for that OID and device
                SnmpEntry newEntry = new SnmpEntry(oid, dataType, datastreamId, deviceId, feed);
                log.info("Adding snmp entry info: {}", newEntry);

                // adding to list
                currentSnmpRecollection.add( newEntry);

            } catch (Exception e) {
                logInvalidConfigurationWarning(entry, e.getMessage());
            }
        }
    }

    private void clearLastConfiguration() {
        currentSnmpRecollection.clear();
    }

    @Override
    public void applyConfiguration() {
        datastreamsManager.loadConfiguration(currentSnmpRecollection);
    }

    private Supplier<RuntimeException> throwMissingRequiredPropertyConfigurationException(String propertyName) {
        return () -> new ConfigurationException("Missing required property \"" + propertyName + "\"");
    }

    private void logInvalidConfigurationWarning(Map.Entry<String, ?> entry, String message) {
        log.warn("Invalid configuration entry  \"{}\": {}", entry, message);
    }
}
