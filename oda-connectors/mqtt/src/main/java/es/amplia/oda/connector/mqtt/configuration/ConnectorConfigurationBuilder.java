package es.amplia.oda.connector.mqtt.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder pattern for creating MQTT connector configurations.
 */
class ConnectorConfigurationBuilder {

    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorConfigurationBuilder.class);

    /**
     * Connector configuration build default values.
     */
    private static final int DEFAULT_PORT = 1883;
    private static final int DEFAULT_SECURE_PORT = 8883;
    private static final boolean DEFAULT_SECURE_CONNECTION = false;
    private static final int DEFAULT_MQTT_VERSION = MqttConnectOptions.MQTT_VERSION_DEFAULT;
    private static final boolean DEFAULT_AUTOMATIC_RECONNECT = true;
    private static final int DEFAULT_CONNECTION_TIMEOUT = MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;
    private static final int DEFAULT_KEEP_ALIVE_INTERVAL = MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT;
    private static final int DEFAULT_MAX_INFLIGHT = MqttConnectOptions.MAX_INFLIGHT_DEFAULT;
    private static final boolean DEFAULT_CLEAN_SESSION = MqttConnectOptions.CLEAN_SESSION_DEFAULT;
    private static final int DEFAULT_LWT_QUALITY_OF_SERVICE = 1;
    private static final boolean DEFAULT_LWT_RETAINED = false;
    private static final int DEFAULT_QUALITY_OF_SERVICE = 1;
    private static final boolean DEFAULT_RETAINED = false;

    /**
     * Connector options.
     */
    private String host;
    private int port = DEFAULT_PORT;
    private int securePort = DEFAULT_SECURE_PORT;
    private String clientId;
    private boolean secureConnection = DEFAULT_SECURE_CONNECTION;

    /**
     * Connection options.
     */
    private int mqttVersion = DEFAULT_MQTT_VERSION;
    private boolean automaticReconnect = DEFAULT_AUTOMATIC_RECONNECT;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    private int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL;
    private int maxInflight = DEFAULT_MAX_INFLIGHT;
    private boolean cleanSession = DEFAULT_CLEAN_SESSION;
    private String userName;
    private String password;

    /**
     * LWT options.
     */
    private String lwtTopic;
    private String lwtPayload;
    private int lwtQualityOfService = DEFAULT_LWT_QUALITY_OF_SERVICE;
    private boolean lwtRetained = DEFAULT_LWT_RETAINED;

    private String keyStorePath;
    private String keyStoreType;
    private String keyStorePassword;

    /**
     * Queues options.
     */
    private String requestQueue;
    private String responseQueue;
    private String iotQueue;
    private int qualityOfService = DEFAULT_QUALITY_OF_SERVICE;
    private boolean retained = DEFAULT_RETAINED;

    /**
     * Private constructor.
     */
    private ConnectorConfigurationBuilder() {
    }

    /**
     * Factory method to return a new connector configuration builder.
     *
     * @return New connector configuration builder.
     */
    static ConnectorConfigurationBuilder newBuilder() {
        return new ConnectorConfigurationBuilder();
    }

    /**
     * Set the broker host.
     *
     * @param host Broker host.
     */
    void setHost(String host) {
        this.host = host;
    }

    /**
     * Set the broker port.
     *
     * @param port Broker port.
     */
    void setPort(int port) {
        this.port = port;
    }

    /**
     * Set the broker secure port.
     *
     * @param securePort Secure port..
     */
    void setSecurePort(int securePort) {
        this.securePort = securePort;
    }

    /**
     * Set the connection client identifier.
     *
     * @param clientId Client identifier.
     */
    void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Set secure connection.
     *
     * @param secureConnection Secure connection.
     */
    void setSecureConnection(boolean secureConnection) {
        this.secureConnection = secureConnection;
    }

    /**
     * Set MQTT version.
     *
     * @param mqttVersion MQTT version.
     */
    void setMqttVersion(int mqttVersion) {
        this.mqttVersion = mqttVersion;
    }

    /**
     * Set automatic reconnect.
     *
     * @param automaticReconnect Automatic reconnect.
     */
    void setAutomaticReconnect(boolean automaticReconnect) {
        this.automaticReconnect = automaticReconnect;
    }

    /**
     * Set connection timeout.
     *
     * @param connectionTimeout Connection timeout.
     */
    void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Set the connection keep-alive interval.
     *
     * @param keepAliveInterval Keep-alive interval.
     */
    void setKeepAliveInterval(int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    /**
     * Set the connection max in-flight.
     *
     * @param maxInflight Max in-flight.
     */
    void setMaxInflight(int maxInflight) {
        this.maxInflight = maxInflight;
    }

    /**
     * Set this connection starts with clean session.
     *
     * @param cleanSession Connection clean session.
     */
    void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    /**
     * Set connection user name.
     *
     * @param userName connection user name.
     */
    void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Set connection password.
     *
     * @param password Connection password.
     */
    void setPassword(String password) {
        this.password = password;
    }

    /**
     * Set LWT topic.
     *
     * @param lwtTopic LWT topic.
     */
    void setLwtTopic(String lwtTopic) {
        this.lwtTopic = lwtTopic;
    }

    /**
     * Set LWT payload.
     *
     * @param lwtPayload LWT payload.
     */
    void setLwtPayload(String lwtPayload) {
        this.lwtPayload = lwtPayload;
    }

    /**
     * Set LWT Quality of Service.
     *
     * @param lwtQualityOfService LWT Quality of Service.
     */
    void setLwtQualityOfService(int lwtQualityOfService) {
        this.lwtQualityOfService = lwtQualityOfService;
    }

    /**
     * Set LWT retained policy.
     *
     * @param lwtRetained LWT retained policy.
     */
    void setLwtRetained(boolean lwtRetained) {
        this.lwtRetained = lwtRetained;
    }

    /**
     * Set the Key store path to use a secure connection.
     *
     * @param keyStorePath Key store path.
     */
    void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    /**
     * Set the key store type to use a secure connection.
     *
     * @param keyStoreType Key store type.
     */
    void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * Set the key store password to use a secure connection.
     *
     * @param keyStorePassword Key store password.
     */
    void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Set the queue request to subscribe.
     *
     * @param requestQueue Queue request to subscribe.
     */
    void setRequestQueue(String requestQueue) {
        this.requestQueue = requestQueue;
    }

    /**
     * Set the Response queue to publish.
     *
     * @param responseQueue Response queue to publish.
     */
    void setResponseQueue(String responseQueue) {
        this.responseQueue = responseQueue;
    }

    /**
     * Set the IOT queue to publish.
     *
     * @param iotQueue IOT queue to publish.
     */
    void setIotQueue(String iotQueue) {
        this.iotQueue = iotQueue;
    }

    /**
     * Set the MQTT messages Quality of Service.
     *
     * @param qualityOfService MQTT messages Quality of Service.
     */
    void setQualityOfService(int qualityOfService) {
        this.qualityOfService = qualityOfService;
    }

    /**
     * Set the MQTT messages retained policy.
     *
     * @param retained MQTT messages retained policy.
     */
    void setRetained(boolean retained) {
        this.retained = retained;
    }

    /**
     * Build a connector configuration with the parameters given to the builder.
     *
     * @return New connector configuration.
     * @throws ConfigurationException Exception configuring the MQTT connector.
     */
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

    /**
     * Check if the connector is configured. Required fields are provided.
     *
     * @return True if the connector is configured.
     */
    private boolean isConnectorConfigured() {
        return host != null && clientId != null;
    }

    /**
     * Build the connection configuration.
     * If the connection is not configured throw an exception as this configuration is required.
     *
     * @return Connection configuration.
     * @throws ConfigurationException Exception configuring the connection.
     */
    private MqttConfiguration buildConnectionConfiguration() throws ConfigurationException {
        if (!isConnectionConfigured()) {
            LOGGER.error("Connection is not configured. Check connection required fields are provided.");
            throw new ConfigurationException("MQTT connection is not configured");
        }
        LOGGER.info("Connection configured.");
        return new ConnectionConfiguration(mqttVersion, automaticReconnect, connectionTimeout, keepAliveInterval,
                maxInflight, cleanSession, userName, password);
    }

    /**
     * Check if the connection is configured. User name and password required fields are set.
     *
     * @return True if the connection is configured.
     */
    private boolean isConnectionConfigured() {
        return userName != null && password != null;
    }

    /**
     * Build the LWT configuration (optional).
     * If LWT fields are not provided return a Null Configuration (Null Object pattern).
     *
     * @return LWT configuration or Null configuration.
     */
    private MqttConfiguration buildLwtConfiguration() {
        if (!isLwtConfigured()) {
            LOGGER.info("LWT is not configured. Ignored.");
            return new NullConfiguration();
        }
        LOGGER.info("LWT is configured.");
        return new LwtConfiguration(lwtTopic, lwtPayload, lwtQualityOfService, lwtRetained);
    }

    /**
     * Check if the LWT is configured. LWT required params are set.
     *
     * @return True if LWT is configured.
     */
    private boolean isLwtConfigured() {
        return lwtTopic != null && lwtPayload != null;
    }

    /**
     * Build the SSL configuration (optional).
     * If SSL fields are not provided return a Null Configuration (Null Object pattern).
     *
     * @return SSL configuration or Null configuration.
     */
    private MqttConfiguration buildSslConfiguration() {
        if (!isSslConfigured()) {
            LOGGER.info("SSL is not configured. Ignored.");
            return new NullConfiguration();
        }
        LOGGER.info("SSL is configured.");
        return new SslConfiguration(keyStorePath, keyStoreType, keyStorePassword);
    }

    /**
     * Check if SSL is configured. All SSL fields are set.
     *
     * @return True if SSL is configured.
     */
    private boolean isSslConfigured() {
        return keyStorePath != null && keyStoreType != null && keyStorePassword != null;
    }

    /**
     * Build the Queues configuration.
     * If the queues are not configured throw an exception as this configuration is required.
     *
     * @return Queues configuration.
     * @throws ConfigurationException Exception configuring the queues.
     */
    private QueuesConfiguration buildQueuesConfiguration() throws ConfigurationException {
        if (!areQueuesConfigured()) {
            LOGGER.error("Queues are not configured. Check queues required fields are provided");
            throw new ConfigurationException("MQTT Queues are not configured.");
        }
        return new QueuesConfiguration(requestQueue, responseQueue, iotQueue, qualityOfService, retained);
    }

    /**
     * Check if the queues are configured. Required fields are set.
     *
     * @return True if queues are configured.
     */
    private boolean areQueuesConfigured() {
        return requestQueue != null && responseQueue != null && iotQueue != null;
    }
}
