package es.amplia.oda.connector.mqtt.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConnectorConfigurationBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorConfigurationBuilder.class);

    private static final int DEFAULT_PORT = 1883;
    private static final int DEFAULT_SECURE_PORT = 8883;
    private static final boolean DEFAULT_SECURE_CONNECTION = false;
    private static final int DEFAULT_MQTT_VERSION = MqttConnectOptions.MQTT_VERSION_DEFAULT;
    private static final boolean DEFAULT_AUTOMATIC_RECONNECT = true;
    private static final int DEFAULT_CONNECTION_TIMEOUT = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
    private static final int DEFAULT_KEEP_ALIVE_INTERVAL = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;
    private static final int DEFAULT_MAX_IN_FLIGHT = MqttConnectOptions.MAX_INFLIGHT_DEFAULT;
    private static final boolean DEFAULT_CLEAN_SESSION = MqttConnectOptions.CLEAN_SESSION_DEFAULT;
    private static final int DEFAULT_LWT_QUALITY_OF_SERVICE = 1;
    private static final boolean DEFAULT_LWT_RETAINED = false;
    private static final int DEFAULT_QUALITY_OF_SERVICE = 1;
    private static final boolean DEFAULT_RETAINED = false;

    private String host;
    private int port = DEFAULT_PORT;
    private int securePort = DEFAULT_SECURE_PORT;
    private String clientId;
    private boolean secureConnection = DEFAULT_SECURE_CONNECTION;
    private int mqttVersion = DEFAULT_MQTT_VERSION;
    private boolean automaticReconnect = DEFAULT_AUTOMATIC_RECONNECT;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL;
    private int maxInFlight = DEFAULT_MAX_IN_FLIGHT;
    private boolean cleanSession = DEFAULT_CLEAN_SESSION;
    private String userName;
    private String password;
    private String lwtTopic;
    private String lwtPayload;
    private int lwtQualityOfService = DEFAULT_LWT_QUALITY_OF_SERVICE;
    private boolean lwtRetained = DEFAULT_LWT_RETAINED;
    private String keyStorePath;
    private String keyStoreType;
    private String keyStorePassword;
    private String requestQueue;
    private String responseQueue;
    private String iotQueue;
    private int qualityOfService = DEFAULT_QUALITY_OF_SERVICE;
    private boolean retained = DEFAULT_RETAINED;

    // Hide default public constructor
    private ConnectorConfigurationBuilder() {}

    static ConnectorConfigurationBuilder newBuilder() {
        return new ConnectorConfigurationBuilder();
    }

    void setHost(String host) {
        this.host = host;
    }

    void setPort(int port) {
        this.port = port;
    }

    void setSecurePort(int securePort) {
        this.securePort = securePort;
    }

    void setClientId(String clientId) {
        this.clientId = clientId;
    }

    void setSecureConnection(boolean secureConnection) {
        this.secureConnection = secureConnection;
    }

    void setMqttVersion(int mqttVersion) {
        this.mqttVersion = mqttVersion;
    }

    void setAutomaticReconnect(boolean automaticReconnect) {
        this.automaticReconnect = automaticReconnect;
    }

    void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    void setMaxInFlight(int maxInFlight) {
        this.maxInFlight = maxInFlight;
    }

    void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    void setUserName(String userName) {
        this.userName = userName;
    }

    void setPassword(String password) {
        this.password = password;
    }

    void setLwtTopic(String lwtTopic) {
        this.lwtTopic = lwtTopic;
    }

    void setLwtPayload(String lwtPayload) {
        this.lwtPayload = lwtPayload;
    }

    void setLwtQualityOfService(int lwtQualityOfService) {
        this.lwtQualityOfService = lwtQualityOfService;
    }

    void setLwtRetained(boolean lwtRetained) {
        this.lwtRetained = lwtRetained;
    }

    void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    void setRequestQueue(String requestQueue) {
        this.requestQueue = requestQueue;
    }

    void setResponseQueue(String responseQueue) {
        this.responseQueue = responseQueue;
    }

    void setIotQueue(String iotQueue) {
        this.iotQueue = iotQueue;
    }

    void setQualityOfService(int qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    void setRetained(boolean retained) {
        this.retained = retained;
    }

    ConnectorConfiguration build() throws ConfigurationException {
        if (!isConnectorConfigured()) {
            throw new ConfigurationException("MQTT connector is not configured.");
        }

        MqttConfiguration connectionConfiguration = buildConnectionConfiguration();
        MqttConfiguration lwtConfiguration = buildLwtConfiguration();
        MqttConfiguration sslConfiguration = buildSslConfiguration();
        QueuesConfiguration queuesConfiguration = buildQueuesConfiguration();

        return new ConnectorConfiguration(host, port, securePort, clientId, secureConnection, connectionConfiguration,
                lwtConfiguration, sslConfiguration, queuesConfiguration);
    }

    private boolean isConnectorConfigured() {
        return host != null && clientId != null;
    }

    private MqttConfiguration buildConnectionConfiguration() throws ConfigurationException {
        if (!isConnectionConfigured()) {
            LOGGER.error("Connection is not configured. Check connection required fields are provided.");
            throw new ConfigurationException("MQTT connection is not configured");
        }
        LOGGER.info("Connection configured.");
        return new ConnectionConfiguration(mqttVersion, automaticReconnect, connectionTimeout, keepAliveInterval,
                maxInFlight, cleanSession, userName, password);
    }

    private boolean isConnectionConfigured() {
        return userName != null && password != null;
    }

    private MqttConfiguration buildLwtConfiguration() {
        if (!isLwtConfigured()) {
            LOGGER.info("LWT is not configured. Ignored.");
            return new NullConfiguration();
        }
        LOGGER.info("LWT is configured.");
        return new LwtConfiguration(lwtTopic, lwtPayload, lwtQualityOfService, lwtRetained);
    }

    private boolean isLwtConfigured() {
        return lwtTopic != null && lwtPayload != null;
    }

    private MqttConfiguration buildSslConfiguration() {
        if (!isSslConfigured()) {
            LOGGER.info("SSL is not configured. Ignored.");
            return new NullConfiguration();
        }
        LOGGER.info("SSL is configured.");
        return new SslConfiguration(keyStorePath, keyStoreType, keyStorePassword);
    }

    private boolean isSslConfigured() {
        return keyStorePath != null && keyStoreType != null && keyStorePassword != null;
    }

    private QueuesConfiguration buildQueuesConfiguration() throws ConfigurationException {
        if (!areQueuesConfigured()) {
            LOGGER.error("Queues are not configured. Check queues required fields are provided");
            throw new ConfigurationException("MQTT Queues are not configured.");
        }
        return new QueuesConfiguration(requestQueue, responseQueue, iotQueue, qualityOfService, retained);
    }

    private boolean areQueuesConfigured() {
        return requestQueue != null && responseQueue != null && iotQueue != null;
    }
}
