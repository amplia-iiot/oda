package es.amplia.oda.connector.mqtt.configuration;

import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.connector.mqtt.MqttConnector;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Dictionary;
import java.util.Optional;
import java.util.function.Supplier;

import static es.amplia.oda.comms.mqtt.api.MqttConnectOptions.*;

public class ConfigurationUpdateHandlerImpl implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUpdateHandlerImpl.class);

    static final String HOST_PROPERTY_NAME = "host";
    static final String PORT_PROPERTY_NAME = "port";
    static final String SECURE_PORT_PROPERTY_NAME = "securePort";
    static final String SECURE_CONNECTION_PROPERTY_NAME = "secure";
    static final String MQTT_VERSION_PROPERTY_NAME = "mqttVersion";
    static final String KEEP_ALIVE_INTERVAL_PROPERTY_NAME = "keepAliveInterval";
    static final String MAX_IN_FLIGHT_PROPERTY_NAME = "maxInFlight";
    static final String CLEAN_SESSION_PROPERTY_NAME = "cleanSession";
    static final String CONNECTION_TIMEOUT_PROPERTY = "connectionTimeout";
    static final String AUTOMATIC_RECONNECT_PROPERTY_NAME = "automaticReconnect";
    static final String LWT_TOPIC_PROPERTY_NAME = "lwt.topic";
    static final String LWT_PAYLOAD_PROPERTY_NAME = "lwt.payload";
    static final String LWT_QOS_PROPERTY_NAME = "lwt.qos";
    static final String LWT_RETAINED_PROPERTY_NAME = "lwt.retained";
    static final String KEY_STORE_PATH_PROPERTY_NAME = "keyStore.path";
    static final String KEY_STORE_TYPE_PROPERTY_NAME = "keyStore.type";
    static final String KEY_STORE_PASSWORD_PROPERTY_NAME = "keyStore.password";
    static final String KEY_MANAGER_ALGORITHM_PROPERTY_NAME = "keyManager.algorithm";
    static final String TRUST_STORE_PATH_PROPERTY_NAME = "trustStore.path";
    static final String TRUST_STORE_TYPE_PROPERTY_NAME = "trustStore.type";
    static final String TRUST_STORE_PASSWORD_PROPERTY_NAME = "trustStore.password";
    static final String TRUST_MANAGER_ALGORITHM_PROPERTY_NAME = "trustManager.algorithm";
    static final String IOT_TOPIC_PROPERTY_NAME = "topic.iot";
    static final String REQUEST_TOPIC_PROPERTY_NAME = "topic.request";
    static final String RESPONSE_TOPIC_PROPERTY_NAME = "topic.response";
    static final String MESSAGE_QOS_PROPERTY_NAME = "message.qos";
    static final String MESSAGE_RETAINED_PROPERTY = "message.retained";

    static final int DEFAULT_PORT = 1883;
    static final int DEFAULT_SECURE_PORT = 8883;
    static final int DEFAULT_QOS = 0;
    static final boolean DEFAULT_RETAINED = false;

    private static final String TCP_URL_PROTOCOL_HEADER = "tcp://";
    private static final String SSL_URL_PROTOCOL_HEADER = "ssl://";
    private static final String MQTT_VERSION_3_1 = "3.1";
    private static final String MQTT_VERSION_3_1_1 = "3.1.1";


    private final MqttConnector connector;
    private final DeviceInfoProvider deviceInfoProvider;

    private Dictionary<String, ?> lastProperties;
    private ConnectorConfiguration currentConfiguration;

    public ConfigurationUpdateHandlerImpl(MqttConnector connector, DeviceInfoProvider deviceInfoProvider) {
        this.connector = connector;
        this.deviceInfoProvider = deviceInfoProvider;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading MQTT connector configuration");

        lastProperties = props;

        String deviceId = deviceInfoProvider.getDeviceId();
        String apiKey = deviceInfoProvider.getApiKey();
        if (deviceId == null || apiKey == null) {
            throw new ConfigurationException("Can not find device identifier and API Key");
        }

        try {
            // Get broker URL
            String host = Optional.ofNullable((String) props.get(HOST_PROPERTY_NAME))
                            .orElseThrow(() -> missingRequireFieldExceptionSupplier(HOST_PROPERTY_NAME).get());
            int port = Optional.ofNullable((String) props.get(PORT_PROPERTY_NAME))
                            .map(Integer::parseInt)
                            .orElse(DEFAULT_PORT);
            int securePort =
                    Optional.ofNullable((String) props.get(SECURE_PORT_PROPERTY_NAME))
                            .map(Integer::parseInt)
                            .orElse(DEFAULT_SECURE_PORT);
            boolean secureConnection =
                    Optional.ofNullable((String) props.get(SECURE_CONNECTION_PROPERTY_NAME))
                            .map(Boolean::parseBoolean)
                            .orElse(false);
            String brokerUrl = getBrokerUrl(host, port, securePort, secureConnection);

            // Get MQTT options
            MqttConnectOptions mqttConnectOptions = getMqttConnectOptionsConfiguration(props, deviceId, apiKey);

            // Topics and messages configuration
            String iotTopic = getTopicFromProperties(props, IOT_TOPIC_PROPERTY_NAME, deviceId);
            String requestTopic = getTopicFromProperties(props, REQUEST_TOPIC_PROPERTY_NAME, deviceId);
            String responseTopic = getTopicFromProperties(props, RESPONSE_TOPIC_PROPERTY_NAME, deviceId);
            int qos = Optional.ofNullable((String) props.get(MESSAGE_QOS_PROPERTY_NAME))
                    .map(Integer::parseInt)
                    .orElse(DEFAULT_QOS);
            boolean retained = Optional.ofNullable((String) props.get(MESSAGE_RETAINED_PROPERTY))
                    .map(Boolean::parseBoolean)
                    .orElse(DEFAULT_RETAINED);

            currentConfiguration = new ConnectorConfiguration(brokerUrl, deviceId, mqttConnectOptions, iotTopic,
                    requestTopic, responseTopic, qos, retained);
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Error parsing configuration properties: " + e);
        }

        LOGGER.info("MQTT connector configuration loaded");
    }

    private MqttConnectOptions getMqttConnectOptionsConfiguration(Dictionary<String, ?> props, String deviceId,
                                                                  String apiKey) {
        MqttConnectOptionsBuilder optionsBuilder = MqttConnectOptions.builder(deviceId, apiKey.toCharArray());
        getConnectionConfiguration(props, optionsBuilder);
        getWillOptionalConfiguration(props, deviceId, optionsBuilder);
        getSslOptionalConfiguration(props, optionsBuilder);
        return optionsBuilder.build();
    }

    private void getConnectionConfiguration(Dictionary<String, ?> props, MqttConnectOptionsBuilder optionsBuilder) {
        Optional.ofNullable((String) props.get(MQTT_VERSION_PROPERTY_NAME))
                .map(this::parseMqttVersion)
                .ifPresent(optionsBuilder::mqttVersion);
        Optional.ofNullable((String) props.get(KEEP_ALIVE_INTERVAL_PROPERTY_NAME))
                .map(Integer::parseInt)
                .ifPresent(optionsBuilder::keepAliveInterval);
        Optional.ofNullable((String) props.get(MAX_IN_FLIGHT_PROPERTY_NAME))
                .map(Integer::parseInt)
                .ifPresent(optionsBuilder::maxInFlight);
        Optional.ofNullable((String) props.get(CLEAN_SESSION_PROPERTY_NAME))
                .map(Boolean::parseBoolean)
                .ifPresent(optionsBuilder::cleanSession);
        Optional.ofNullable((String) props.get(AUTOMATIC_RECONNECT_PROPERTY_NAME))
                .map(Boolean::parseBoolean)
                .ifPresent(optionsBuilder::automaticReconnect);
        Optional.ofNullable((String) props.get(CONNECTION_TIMEOUT_PROPERTY))
                .map(Integer::parseInt)
                .ifPresent(optionsBuilder::connectionTimeout);
    }

    private void getWillOptionalConfiguration(Dictionary<String, ?> props, String deviceId,
                                              MqttConnectOptionsBuilder optionsBuilder) {
        Optional<String> willTopic =
                Optional.ofNullable((String) props.get(LWT_TOPIC_PROPERTY_NAME))
                        .map(value -> getTopic(value, deviceId));
        Optional<byte[]>  willPayload =
                Optional.ofNullable((String) props.get(LWT_PAYLOAD_PROPERTY_NAME))
                        .map(value -> value.getBytes(StandardCharsets.UTF_8));
        Optional<Integer> willQos =
                Optional.ofNullable((String) props.get(LWT_QOS_PROPERTY_NAME))
                        .map(Integer::parseInt);
        Optional<Boolean> willRetained =
                Optional.ofNullable((String) props.get(LWT_RETAINED_PROPERTY_NAME))
                        .map(Boolean::parseBoolean);

        if (willTopic.isPresent() && willPayload.isPresent()) {
            if (willQos.isPresent() && willRetained.isPresent()) {
                optionsBuilder.will(willTopic.get(), willPayload.get(), willQos.get(), willRetained.get());
            } else {
                optionsBuilder.will(willTopic.get(), willPayload.get());
            }
        }
    }

    private void getSslOptionalConfiguration(Dictionary<String, ?> props, MqttConnectOptionsBuilder optionsBuilder) {
        Optional<String> keyStore = Optional.ofNullable((String) props.get(KEY_STORE_PATH_PROPERTY_NAME));
        Optional<KeyStoreType> keyStoreType =
                Optional.ofNullable((String) props.get(KEY_STORE_TYPE_PROPERTY_NAME))
                        .map(KeyStoreType::valueOf);
        Optional<char[]> keyStorePwd =
                Optional.ofNullable((String) props.get(KEY_STORE_PASSWORD_PROPERTY_NAME))
                        .map(String::toCharArray);
        Optional<KeyManagerAlgorithm> keyManagerAlgorithm =
                Optional.ofNullable((String) props.get(KEY_MANAGER_ALGORITHM_PROPERTY_NAME))
                        .map(KeyManagerAlgorithm::valueOf);
        Optional<String> trustStore = Optional.ofNullable((String) props.get(TRUST_STORE_PATH_PROPERTY_NAME));
        Optional<KeyStoreType> trustStoreType =
                Optional.ofNullable((String) props.get(TRUST_STORE_TYPE_PROPERTY_NAME))
                        .map(KeyStoreType::valueOf);
        Optional<char[]> trustStorePwd =
                Optional.ofNullable((String) props.get(TRUST_STORE_PASSWORD_PROPERTY_NAME))
                        .map(String::toCharArray);
        Optional<KeyManagerAlgorithm> trustManagerAlgorithm =
                Optional.ofNullable((String) props.get(TRUST_MANAGER_ALGORITHM_PROPERTY_NAME))
                        .map(KeyManagerAlgorithm::valueOf);

        if (keyStore.isPresent() && keyStorePwd.isPresent() && trustStore.isPresent() && trustStorePwd.isPresent()) {
            optionsBuilder.ssl(keyStore.get(), keyStoreType.orElse(SslOptions.DEFAULT_KEY_STORE_TYPE),
                    keyStorePwd.get(),
                    keyManagerAlgorithm.orElse(KeyManagerAlgorithm.from(KeyManagerFactory.getDefaultAlgorithm())),
                    trustStore.get(), trustStoreType.orElse(SslOptions.DEFAULT_KEY_STORE_TYPE),
                    trustStorePwd.get(),
                    trustManagerAlgorithm.orElse(KeyManagerAlgorithm.from(KeyManagerFactory.getDefaultAlgorithm())));
        }
    }

    private Supplier<ConfigurationException> missingRequireFieldExceptionSupplier(String missingField) {
        return () -> new ConfigurationException("Missing require field \"" + missingField  + "\"");
    }

    private String getTopicFromProperties(Dictionary<String, ?> props, String propertyName, String deviceId) {
        return Optional.ofNullable((String) props.get(propertyName))
                .map(value -> getTopic(value, deviceId))
                .orElseThrow(() -> missingRequireFieldExceptionSupplier(propertyName).get());
    }

    private String getBrokerUrl(String host, int port, int securePort, boolean secureConnection) {
        String brokerUrl = secureConnection ? SSL_URL_PROTOCOL_HEADER : TCP_URL_PROTOCOL_HEADER;
        brokerUrl += host;
        brokerUrl += ":";
        brokerUrl += secureConnection ? securePort : port;
        return brokerUrl;
    }

    private MqttVersion parseMqttVersion(String value) {
        if (MQTT_VERSION_3_1.equals(value)) {
            return MqttVersion.MQTT_3_1;
        } else if (MQTT_VERSION_3_1_1.equals(value)) {
            return MqttVersion.MQTT_3_1_1;
        } else {
            return MqttVersion.valueOf(value);
        }
    }

    private String getTopic(String baseTopic, String deviceId) {
        return baseTopic + "/" + deviceId;
    }

    @Override
    public void applyConfiguration() throws MqttException {
        LOGGER.info("Apply MQTT connector configuration");
        connector.loadConfigurationAndInit(currentConfiguration);
    }

    public void reapplyConfiguration() throws MqttException {
        if (lastProperties != null) {
            loadConfiguration(lastProperties);
            applyConfiguration();
        }
    }
}
