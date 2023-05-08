package es.amplia.oda.datastreams.iec104.configuration;

import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.iec104.Iec104DatastreamsManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Iec104DatastreamsConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Iec104DatastreamsConfigurationUpdateHandler.class);

    private static final String POLLING_TIME_PROPERTY_NAME = "pollingTime";

    private static final int KEY_FIELDS_SIZE = 3;
    private static final String KEY_FIELDS_DELIMITER = ";";


    private final Iec104DatastreamsManager iec104DatastreamsManager;
    private final List<Iec104DatastreamsConfiguration> currentIec104DatastreamsConfigurations = new ArrayList<>();
    private int iec104Polling;

    public Iec104DatastreamsConfigurationUpdateHandler(Iec104DatastreamsManager iec104DatastreamsManager) {
        this.iec104DatastreamsManager = iec104DatastreamsManager;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        currentIec104DatastreamsConfigurations.clear();

        LOGGER.info("IEC104 datastreams loading configuration");
        Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);
        this.iec104Polling = Integer.parseInt((String)mappedProperties.get(POLLING_TIME_PROPERTY_NAME));
        LOGGER.debug("Setting polling time to {}", this.iec104Polling);
        mappedProperties.remove(POLLING_TIME_PROPERTY_NAME);
        for (Map.Entry<String, ?> entry : mappedProperties.entrySet()) {
            String deviceId = entry.getKey();
            String[] keys = ((String)entry.getValue()).split(KEY_FIELDS_DELIMITER);
            if (keys.length == KEY_FIELDS_SIZE) {
                Iec104DatastreamsConfiguration.Iec104DatastreamsConfigurationBuilder builder =
                    Iec104DatastreamsConfiguration.builder().
                        deviceId(deviceId).
                        ipAddress(keys[0]).
                        ipPort(Integer.parseInt(keys[1])).
                        commonAddress(Integer.parseInt(keys[2]));

                LOGGER.debug("Adding configuration for device {}", deviceId);
                currentIec104DatastreamsConfigurations.add(builder.build());
            } else
                LOGGER.error("Invalid configuration for device {}, found {}, expected {}", deviceId, keys.length, KEY_FIELDS_SIZE);
        }
    }

    @Override
    public void loadDefaultConfiguration() {
        currentIec104DatastreamsConfigurations.clear();
    }

    @Override
    public void applyConfiguration() {
        iec104DatastreamsManager.loadConfiguration(currentIec104DatastreamsConfigurations, iec104Polling);
    }
}
