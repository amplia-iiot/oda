package es.amplia.oda.service.scadatables.configuration;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.service.scadatables.internal.ScadaTableInfoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SCADA Tables update configuration handler.
 */
@SuppressWarnings("restriction")
public class ScadaTablesConfigurationHandler implements ConfigurationUpdateHandler {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ScadaTablesConfigurationHandler.class);

    public static final String REVERSE_ENDIAN_FUNCTION = "var reverseEndian = function(x) {\n\tvar sHex = " +
            "'00000000' + parseInt(x, 10).toString(16);\n\tsHex = sHex.slice(-8);\n\treturn " +
            "parseInt(sHex.replace(/^(.(..)*)$/, '0$1').match(/../g).reverse().join(''), 16);\n\t};";
    private static final String DATASTREAM_PROPERTY_NAME = "datastream";
    private static final String DEVICE_PROPERTY_NAME = "device";
    private static final String FEED_PROPERTY_NAME = "feed";
    private static final String TRANSFORMATION_PROPERTY_NAME = "transformation";
    private static final String NASHORN_ENGINE_NAME = "nashorn";

    /**
     * SCADA table information service.
     */
    private final ScadaTableInfoService scadaTableInfoService;

    /**
     * Current configuration.
     */
    // map is <<address,asdu>, config>
    private final Map< Map<Integer,String>, ScadaTableEntryConfiguration> currentScadaTableConfig = new HashMap<>();


    /**
     * Constructor.
     *
     * @param scadaTableInfoService SCADA table information service.
     */
    public ScadaTablesConfigurationHandler(ScadaTableInfoService scadaTableInfoService) {
        this.scadaTableInfoService = scadaTableInfoService;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        clearLastConfiguration();

        Map<String, ?> propertiesMap = Collections.dictionaryToMap(props);

        for (Map.Entry<String, ?> entry : propertiesMap.entrySet()) {
            try {
                String[] keys = entry.getKey().split(",");
                String dataType = keys[0];
                int index = Integer.parseInt(keys[1]);

                String[] propertyTokens = getTokensFromProperty((String) entry.getValue());

                String datastreamId = getValueByToken(DATASTREAM_PROPERTY_NAME, propertyTokens)
                        .orElseThrow(throwMissingRequiredPropertyConfigurationException(DATASTREAM_PROPERTY_NAME));

                String deviceId = getValueByToken(DEVICE_PROPERTY_NAME, propertyTokens).orElse(null);

                String feed = getValueByToken(FEED_PROPERTY_NAME, propertyTokens).orElse(null);

                // create scada table config entry
                ScadaTableEntryConfiguration newConfig = new BoxEntryConfiguration(dataType, datastreamId, deviceId, feed);

                Optional<String> script = getValueByToken(TRANSFORMATION_PROPERTY_NAME, propertyTokens);
                if (script.isPresent()) {
                    registerScript(newConfig, script.get(), datastreamId, index);
                } else newConfig.setScript(null);

                logger.info("Adding translate info: {}, {}, {}, {}, {}", dataType, index, datastreamId, deviceId, feed);
                Map<Integer,String> pairAsduAddress = java.util.Collections.singletonMap(index, dataType);
                currentScadaTableConfig.put(pairAsduAddress, newConfig);

            } catch (IndexOutOfBoundsException | NumberFormatException exception) {
                logInvalidConfigurationWarning(entry.getKey(), "The entry format is not valid");
            } catch (IllegalArgumentException exception) {
                logInvalidConfigurationWarning(entry.getKey(), "Unrecognized property value");
            } catch (ConfigurationException exception) {
                logInvalidConfigurationWarning(entry.getKey(), exception.getMessage());
            }
        }
    }

    private void registerScript(ScadaTableEntryConfiguration newConfig, String script, String datastreamId, int index) {
        try {
            final ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName(NASHORN_ENGINE_NAME);
            engine.eval(REVERSE_ENDIAN_FUNCTION + "\r\n function run(x) { return " + script + "; }");
            newConfig.setScript((Invocable) engine);
        } catch (ScriptException e) {
            logger.error("Error loading script", e);
            throw new ConfigurationException("Invalid script supplied for Datastream \"" + datastreamId + "\" and index " + index);
        }
    }

    /**
     * Create a supplier to throw a missing required property configuration exception.
     *
     * @param propertyName Property missing.
     * @return Supplier throwing the correspondent configuration exception.
     */
    private Supplier<RuntimeException> throwMissingRequiredPropertyConfigurationException(String propertyName) {
        return () -> new ConfigurationException("Missing required property \"" + propertyName + "\"");
    }

    /**
     * Log a warning of an invalid configuration entry.
     *
     * @param entry   Invalid entry.
     * @param message Message describing the error.
     */
    private void logInvalidConfigurationWarning(String entry, String message) {
        logger.warn("Invalid configuration entry  \"{}\": {}", entry, message);
    }

    /**
     * Clear the last configuration.
     */
    private void clearLastConfiguration() {
        currentScadaTableConfig.clear();
    }

    @Override
    public void applyConfiguration() {
        scadaTableInfoService.loadConfiguration(currentScadaTableConfig);
    }
}
