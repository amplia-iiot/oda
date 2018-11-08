package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;


public class ConnectionConfiguration implements MqttConfiguration {

    private final int mqttVersion;
    private final boolean automaticReconnect;
    private final int connectionTimeout;
    private final int keepAliveInterval;
    private final int maxInFlight;
    private final boolean cleanSession;
    private final String userName;
    private final String password;

    ConnectionConfiguration(int mqttVersion, boolean automaticReconnect, int connectionTimeout, int keepAliveInterval,
                            int maxInFlight, boolean cleanSession, String userName, String password) {
        this.mqttVersion = mqttVersion;
        this.automaticReconnect = automaticReconnect;
        this.connectionTimeout = connectionTimeout;
        this.keepAliveInterval = keepAliveInterval;
        this.maxInFlight = maxInFlight;
        this.cleanSession = cleanSession;
        this.userName = userName;
        this.password = password;
    }

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
