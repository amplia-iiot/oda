package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * Configuration of the MQTT connection
 */
public class ConnectionConfiguration implements MqttConfiguration {

    /**
     * MQTT version.
     */
    private final int mqttVersion;

    /**
     * Automatic reconnect connection.
     */
    private final boolean automaticReconnect;

    /**
     * Timeout trying to establish connection.
     */
    private final int connectionTimeout;

    /**
     * Keep-alive interval to check if the connection is alive.
     */
    private final int keepAliveInterval;

    /**
     * Max number of in-flight messages (Messages with QoS &gt; 0 without acknowledgment).
     */
    private final int maxInFlight;

    /**
     * Client session to ignore last persistent session (if exists) or to use it.
     */
    private final boolean cleanSession;

    /**
     * User name to use for the connection.
     */
    private final String userName;

    /**
     * Password to use for the connection.
     */
    private final String password;

    /**
     * Constructor.
     *
     * @param mqttVersion        MQTT version.
     * @param automaticReconnect Automatic reconnect connection
     * @param connectionTimeout  Timeout trying to establish connection.
     * @param keepAliveInterval  Keep-alive interval.
     * @param maxInFlight        Max number of in-flight messages
     * @param cleanSession       Use clean session.
     * @param userName           User name for the connection.
     * @param password           Password for the connection.
     */
    ConnectionConfiguration(int mqttVersion, boolean automaticReconnect, int connectionTimeout,
                                   int keepAliveInterval, int maxInFlight, boolean cleanSession, String userName,
                                   String password) {
        this.mqttVersion = mqttVersion;
        this.automaticReconnect = automaticReconnect;
        this.connectionTimeout = connectionTimeout;
        this.keepAliveInterval = keepAliveInterval;
        this.maxInFlight = maxInFlight;
        this.cleanSession = cleanSession;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Configure MQTT connect options with the current connection configuration.
     *
     * @param options MQTT connect options to configure.
     */
    public void configure(MqttConnectOptions options) {
        options.setMqttVersion(mqttVersion);
        options.setAutomaticReconnect(automaticReconnect);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setMaxInflight(maxInFlight);
        options.setCleanSession(cleanSession);
        options.setUserName(userName);
        options.setPassword(password.toCharArray());
    }
}
