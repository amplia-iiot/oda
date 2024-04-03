package es.amplia.oda.datastreams.mqtt.configuration;

import es.amplia.oda.comms.mqtt.api.MqttConnectOptions.KeyStoreType;
import es.amplia.oda.comms.mqtt.api.MqttConnectOptions.SslOptions;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.mqtt.MqttDatastreamsOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MqttDatastreamsConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsConfigurationUpdateHandler.class);

    static final String SERVER_URI_PROPERTY_NAME = "brokerURI";
    static final String CLIENT_ID_PROPERTY_NAME = "clientId";
    static final String PASSWORD_PROPERTY_NAME = "password";
    static final String EVENT_TOPIC_PROPERTY_NAME = "eventTopic";
    static final String REQUEST_TOPIC_PROPERTY_NAME = "requestTopic";
    static final String RESPONSE_TOPIC_PROPERTY_NAME = "responseTopic";
    static final String MESSAGE_QOS_PROPERTY_NAME = "message.qos";
    static final String MESSAGE_RETAINED_PROPERTY_NAME = "message.retained";
    static final String KEY_STORE_PATH_PROPERTY_NAME = "keyStore.path";
    static final String KEY_STORE_TYPE_PROPERTY_NAME = "keyStore.type";
    static final String KEY_STORE_PASS_PROPERTY_NAME = "keyStore.password";
    static final String TRUST_STORE_PATH_PROPERTY_NAME = "trustStore.path";
    static final String TRUST_STORE_TYPE_PROPERTY_NAME = "trustStore.type";
    static final String TRUST_STORE_PASS_PROPERTY_NAME = "trustStore.password";
    static final String NEXT_LEVEL_ODA_LIST_PROPERTY_NAME = "nextLevel.odaIds";

    static final int DEFAULT_QOS = 1;
    static final boolean DEFAULT_RETAINED = false;

    private final MqttDatastreamsOrchestrator mqttDatastreamsOrchestrator;

    private MqttDatastreamsConfiguration currentConfiguration;

    public MqttDatastreamsConfigurationUpdateHandler(MqttDatastreamsOrchestrator mqttDatastreamsOrchestrator) {
        this.mqttDatastreamsOrchestrator = mqttDatastreamsOrchestrator;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new configuration");
        String brokerURI = getRequiredConfigurationParameter(props, SERVER_URI_PROPERTY_NAME);
        String clientId =
                Optional.ofNullable((String) props.get(CLIENT_ID_PROPERTY_NAME)).orElse(UUID.randomUUID().toString());
        String password = getRequiredConfigurationParameter(props, PASSWORD_PROPERTY_NAME);
        String eventTopic = getRequiredConfigurationParameter(props, EVENT_TOPIC_PROPERTY_NAME);
        String requestTopic = getRequiredConfigurationParameter(props, REQUEST_TOPIC_PROPERTY_NAME);
        String responseTopic = getRequiredConfigurationParameter(props, RESPONSE_TOPIC_PROPERTY_NAME);
        int qos = Optional.ofNullable((String) props.get(MESSAGE_QOS_PROPERTY_NAME))
                    .map(Integer::parseInt)
                    .orElse(DEFAULT_QOS);
        boolean retained = Optional.ofNullable((String) props.get(MESSAGE_RETAINED_PROPERTY_NAME))
                    .map(Boolean::parseBoolean)
                    .orElse(DEFAULT_RETAINED);

        // SSL Configuration
        Optional<String> keyStore = Optional.ofNullable((String) props.get(KEY_STORE_PATH_PROPERTY_NAME));
        Optional<KeyStoreType> keyStoreType =
                Optional.ofNullable((String) props.get(KEY_STORE_TYPE_PROPERTY_NAME))
                        .map(KeyStoreType::valueOf);
        Optional<char[]> keyStorePwd =
                Optional.ofNullable((String) props.get(KEY_STORE_PASS_PROPERTY_NAME))
                        .map(String::toCharArray);
        Optional<String> trustStore = Optional.ofNullable((String) props.get(TRUST_STORE_PATH_PROPERTY_NAME));
        Optional<KeyStoreType> trustStoreType =
                Optional.ofNullable((String) props.get(TRUST_STORE_TYPE_PROPERTY_NAME))
                        .map(KeyStoreType::valueOf);
        Optional<char[]> trustStorePwd =
                Optional.ofNullable((String) props.get(TRUST_STORE_PASS_PROPERTY_NAME))
                        .map(String::toCharArray);

        currentConfiguration = new MqttDatastreamsConfiguration(brokerURI, clientId, password, eventTopic, requestTopic, responseTopic, qos, retained);

        if (keyStore.isPresent() && keyStorePwd.isPresent() && trustStore.isPresent() && trustStorePwd.isPresent()) {
            currentConfiguration.setKeyStore(keyStore.get());
            currentConfiguration.setKeyStorePassword(keyStorePwd.get());
            currentConfiguration.setKeyStoreType(keyStoreType.orElse(SslOptions.DEFAULT_KEY_STORE_TYPE));
            currentConfiguration.setTrustStore(trustStore.get());
            currentConfiguration.setTrustStorePassword(trustStorePwd.get());
            currentConfiguration.setTrustStoreType(trustStoreType.orElse(SslOptions.DEFAULT_KEY_STORE_TYPE));
        }

        // List of next level ODA identifiers
        Optional<String> odaList = Optional.ofNullable((String) props.get(NEXT_LEVEL_ODA_LIST_PROPERTY_NAME));
        odaList.ifPresent(list -> {
            String[] array = list.split(",");
            currentConfiguration.setNextLevelOdaIds(Arrays.asList(array));});

        LOGGER.info("New configuration loaded");
    }

    private String getRequiredConfigurationParameter(Dictionary<String, ?> props, String parameterName) {
        return Optional.ofNullable((String) props.get(parameterName))
                .orElseThrow(() -> new ConfigurationException("Missing required parameter: " + parameterName));
    }

    @Override
    public void applyConfiguration() {
        mqttDatastreamsOrchestrator.loadConfiguration(currentConfiguration);
    }
}
