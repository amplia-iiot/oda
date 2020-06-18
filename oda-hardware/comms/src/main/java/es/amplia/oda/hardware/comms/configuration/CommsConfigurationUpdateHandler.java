package es.amplia.oda.hardware.comms.configuration;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.comms.CommsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

public class CommsConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommsConfigurationUpdateHandler.class);

    static final String PIN_PROPERTY_NAME = "pin";
    static final String APN_PROPERTY_NAME = "apn";
    static final String USERNAME_PROPERTY_NAME = "username";
    static final String PASS_PROPERTY_NAME = "password";
    static final String CONNECTION_TIMEOUT_PROPERTY_NAME = "connectionTimeout";
    static final int DEFAULT_CONNECTION_TIMEOUT = 60;
    static final String RETRY_CONNECTION_TIMER_PROPERTY_NAME = "retryConnectionTimer";
    static final long DEFAULT_RETRY_CONNECTION_TIMER = 60;



    private final CommsManager commsManager;
    private CommsConfiguration currentConfiguration;


    public CommsConfigurationUpdateHandler(CommsManager commsManager) {
        this.commsManager = commsManager;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new Comms Hardware configuration");
        String pin = Optional.ofNullable(props.get(PIN_PROPERTY_NAME)).map(String::valueOf).orElse("");
        String apn = Optional.ofNullable(props.get(APN_PROPERTY_NAME)).map(String::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Missing required property " + APN_PROPERTY_NAME));
        LOGGER.info("Configured bundle for APN {} with '{}' as PIN", apn, pin);
        String username = Optional.ofNullable(props.get(USERNAME_PROPERTY_NAME)).map(String::valueOf).orElse("");
        String password = Optional.ofNullable(props.get(PASS_PROPERTY_NAME)).map(String::valueOf).orElse("");
        int connectionTimeout = Optional.ofNullable(props.get(CONNECTION_TIMEOUT_PROPERTY_NAME)).map(String::valueOf)
                .map(Integer::parseInt).orElse(DEFAULT_CONNECTION_TIMEOUT);
        long retryConnectionTimer = Optional.ofNullable(props.get(RETRY_CONNECTION_TIMER_PROPERTY_NAME))
                .map(String::valueOf).map(Long::parseLong).orElse(DEFAULT_RETRY_CONNECTION_TIMER);

        currentConfiguration =
                new CommsConfiguration(pin, apn, username, password, connectionTimeout, retryConnectionTimer);
        LOGGER.info("New Comms Hardware configuration loaded");
    }

    @Override
    public void applyConfiguration() {
        LOGGER.info("Applying comms hardware configuration");
        commsManager.connect(currentConfiguration.getPin(), currentConfiguration.getApn(),
                currentConfiguration.getUsername(), currentConfiguration.getPassword(),
                currentConfiguration.getConnectionTimeout(), currentConfiguration.getRetryConnectionTimer());
    }
}
