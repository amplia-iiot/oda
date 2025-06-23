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
    private static final String INITIAL_POLLING_TIME_PROPERTY_NAME = "initialPollingTime";
    private static final String CONNECTION_INITIAL_DELAY_PROPERTY_NAME = "connectionInitialDelay";
    private static final String CONNECTION_RETRY_DELAY_PROPERTY_NAME = "connectionRetryDelay";
    private static final String QUALITY_BITS_MASK_PROPERTY_NAME = "qualityBitsMask";
    private static final String QUALITY_BITS_NOTIFICATION_PROPERTY_NAME = "qualityBitsNotify";

    private static final int DEFAULT_INITIAL_DELAY = 10;
    private static final int DEFAULT_RETRY_DELAY = 300;
    private static final boolean DEFAULT_QUALITY_BITS_NOTIFICATION = false;

    private static final int KEY_FIELDS_SIZE = 3;
    private static final String KEY_FIELDS_DELIMITER = ";";
    private static final char [] DEFAULT_QUALITY_BITS_MASK = {'*','*','*','*'};

    private final Iec104DatastreamsManager iec104DatastreamsManager;
    private final List<Iec104DatastreamsConfiguration> currentIec104DatastreamsConfigurations = new ArrayList<>();
    private int iec104Polling;
    private int iec104PollingInitialDelay;
    private int initialDelay;
    private int retryDelay;
    private char[] qualityBitsMask = new char[4];
    private boolean qualityBitsNotify;


    public Iec104DatastreamsConfigurationUpdateHandler(Iec104DatastreamsManager iec104DatastreamsManager) {
        this.iec104DatastreamsManager = iec104DatastreamsManager;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        currentIec104DatastreamsConfigurations.clear();

        LOGGER.info("IEC104 datastreams loading configuration");
        Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);
        this.iec104PollingInitialDelay = Integer.parseInt((String)mappedProperties.get(INITIAL_POLLING_TIME_PROPERTY_NAME));
        this.iec104Polling = Integer.parseInt((String)mappedProperties.get(POLLING_TIME_PROPERTY_NAME));
        initialDelay = Optional.ofNullable((String) props.get(CONNECTION_INITIAL_DELAY_PROPERTY_NAME))
                    .map(Integer::parseInt)
                    .orElse(DEFAULT_INITIAL_DELAY);
        retryDelay = Optional.ofNullable((String) props.get(CONNECTION_RETRY_DELAY_PROPERTY_NAME))
                    .map(Integer::parseInt)
                    .orElse(DEFAULT_RETRY_DELAY);

        LOGGER.debug("Initial polling time of {} milliseconds, after this polling time of {} milliseconds",
                this.iec104PollingInitialDelay, this.iec104Polling);

        if (this.iec104PollingInitialDelay <= 0 ||  this.iec104Polling <= 0) {
            LOGGER.error("Initial delay or polling times are not bigger than zero");
            return;
        }

        // parse quality bits notification
        this.qualityBitsNotify = Optional.ofNullable((String) props.get(QUALITY_BITS_NOTIFICATION_PROPERTY_NAME))
                .map(Boolean::parseBoolean)
                .orElse(DEFAULT_QUALITY_BITS_NOTIFICATION);

        // parse quality bits mask
        if (!parseQualityBits((String) props.get(QUALITY_BITS_MASK_PROPERTY_NAME))) {
            LOGGER.warn("Invalid quality bits mask. Using default mask {}", DEFAULT_QUALITY_BITS_MASK);
            this.qualityBitsMask = DEFAULT_QUALITY_BITS_MASK.clone();
        } else {
            LOGGER.debug("Quality bits mask to apply '{}', quality notify '{}'", this.qualityBitsMask, this.qualityBitsNotify);
        }

        // remove parsed properties
        mappedProperties.remove(POLLING_TIME_PROPERTY_NAME);
        mappedProperties.remove(INITIAL_POLLING_TIME_PROPERTY_NAME);
        mappedProperties.remove(CONNECTION_INITIAL_DELAY_PROPERTY_NAME);
        mappedProperties.remove(CONNECTION_RETRY_DELAY_PROPERTY_NAME);
        mappedProperties.remove(QUALITY_BITS_MASK_PROPERTY_NAME);
        mappedProperties.remove(QUALITY_BITS_NOTIFICATION_PROPERTY_NAME);

        // parse devices configuration
        for (Map.Entry<String, ?> entry : mappedProperties.entrySet()) {
            String deviceId = entry.getKey();
            String[] keys = ((String) entry.getValue()).split(KEY_FIELDS_DELIMITER);
            if (keys.length == KEY_FIELDS_SIZE) {
                Iec104DatastreamsConfiguration.Iec104DatastreamsConfigurationBuilder builder =
                        Iec104DatastreamsConfiguration.builder().
                                deviceId(deviceId).
                                ipAddress(keys[0]).
                                ipPort(Integer.parseInt(keys[1])).
                                commonAddress(Integer.parseInt(keys[2]));

                LOGGER.debug("Adding configuration for device {}", deviceId);
                currentIec104DatastreamsConfigurations.add(builder.build());
            } else {
                LOGGER.error("Invalid configuration for device {}, found {}, expected {}", deviceId, keys.length, KEY_FIELDS_SIZE);
            }
        }
    }

    @Override
    public void loadDefaultConfiguration() {
        currentIec104DatastreamsConfigurations.clear();
    }

    @Override
    public void applyConfiguration() {
        LOGGER.info("IEC104 datastreams applying configuration");
        iec104DatastreamsManager.loadConfiguration(currentIec104DatastreamsConfigurations, iec104PollingInitialDelay,
                iec104Polling, initialDelay, retryDelay, qualityBitsMask, qualityBitsNotify);
    }

    private boolean parseQualityBits(String qualityBitsMaskString) {
        if (qualityBitsMaskString == null) {
            return false;
        }

        if (qualityBitsMaskString.length() != qualityBitsMask.length) {
            LOGGER.error("Parameter qualityBitsMask must be {} characters", qualityBitsMask.length);
            return false;
        }

        for (int i = 0; i < qualityBitsMaskString.length(); i++) {
            char qualityBit = qualityBitsMaskString.charAt(i);
            if (qualityBit == '1' || qualityBit == '0' || qualityBit == '*') {
                this.qualityBitsMask[i] = qualityBit;
            } else {
                LOGGER.error("Invalid character in qualityBitsMask parameter. Valid characters are '0', '1' or '*'");
                return false;
            }
        }

        return true;
    }
}
