package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConnectionConfigurationTest {

    private static final int MQTT_VERSION = MqttConnectOptions.MQTT_VERSION_3_1_1;
    private static final boolean AUTOMATIC_RECONNECT = true;
    private static final int CONNECTION_TIMEOUT = 60;
    private static final int KEEP_ALIVE_INTERVAL = 120;
    private static final int MAX_IN_FLIGHT = 20;
    private static final boolean CLEAN_SESSION = false;
    private static final String USER_NAME = "user";
    private static final String PASSWORD = "PASSWORD";

    @Test
    public void testConstructor() {
        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(MQTT_VERSION, AUTOMATIC_RECONNECT,
                CONNECTION_TIMEOUT, KEEP_ALIVE_INTERVAL, MAX_IN_FLIGHT, CLEAN_SESSION, USER_NAME, PASSWORD);
        assertNotNull(connectionConfiguration);
    }

    @Test
    public void testConfigure() {
        ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(MQTT_VERSION, AUTOMATIC_RECONNECT,
                CONNECTION_TIMEOUT, KEEP_ALIVE_INTERVAL, MAX_IN_FLIGHT, CLEAN_SESSION, USER_NAME, PASSWORD);
        MqttConnectOptions options = new MqttConnectOptions();

        connectionConfiguration.configure(options);

        assertEquals(MQTT_VERSION, options.getMqttVersion());
        assertEquals(AUTOMATIC_RECONNECT, options.isAutomaticReconnect());
        assertEquals(CONNECTION_TIMEOUT, options.getConnectionTimeout());
        assertEquals(KEEP_ALIVE_INTERVAL, options.getKeepAliveInterval());
        assertEquals(MAX_IN_FLIGHT, options.getMaxInflight());
        assertEquals(CLEAN_SESSION, options.isCleanSession());
        assertEquals(USER_NAME, options.getUserName());
        assertEquals(PASSWORD, new String(options.getPassword()));
    }
}