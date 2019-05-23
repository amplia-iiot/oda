package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;

import org.junit.Test;

import java.util.Properties;
import java.util.UUID;

import static es.amplia.oda.comms.mqtt.api.MqttConnectOptions.*;

import static org.junit.Assert.*;

public class MqttPahoConnectOptionsMapperTest {

    private static final String TEST_USERNAME = "testUser";
    private static final char[] TEST_API_KEY = UUID.randomUUID().toString().toCharArray();
    private static final MqttVersion TEST_MQTT_VERSION = MqttVersion.MQTT_3_1;
    private static final int CORRESPONDENT_MQTT_VERSION =
            org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1;
    private static final int TEST_KEEP_ALIVE_INTERVAL = 300;
    private static final int TEST_MAX_IN_FLIGHT= 5;
    private static final boolean TEST_CLEAN_SESSION = false;
    private static final int TEST_CONNECTION_TIMEOUT = 10;
    private static final boolean TEST_AUTOMATIC_RECONNECT = false;
    private static final String TEST_WILL_TOPIC = "test/will/topic";
    private static final byte[] TEST_WILL_PAYLOAD = "Test will message".getBytes();
    private static final int TEST_WILL_QOS = 2;
    private static final boolean TEST_WILL_RETAINED = true;
    private static final String TEST_KEYSTORE = "test/keystore";
    private static final KeyStoreType TEST_KEYSTORE_TYPE = KeyStoreType.PKCS12;
    private static final char[] TEST_KEYSTORE_P = "123456".toCharArray();
    private static final KeyManagerAlgorithm TEST_KEY_MANAGER_ALGORITHM = KeyManagerAlgorithm.PKIX;
    private static final String TEST_TRUSTSTORE = "test/truststore";
    private static final KeyStoreType TEST_TRUSTSTORE_TYPE = KeyStoreType.PKCS12;
    private static final char[] TEST_TRUSTSTORE_P = "7890123".toCharArray();
    private static final KeyManagerAlgorithm TEST_TRUST_MANAGER_ALGORITHM = KeyManagerAlgorithm.PKIX;

    @Test
    public void testFrom() {
        MqttConnectOptions testOptions =
                MqttConnectOptions.builder(TEST_USERNAME, TEST_API_KEY)
                        .mqttVersion(TEST_MQTT_VERSION)
                        .keepAliveInterval(TEST_KEEP_ALIVE_INTERVAL)
                        .maxInFlight(TEST_MAX_IN_FLIGHT)
                        .cleanSession(TEST_CLEAN_SESSION)
                        .connectionTimeout(TEST_CONNECTION_TIMEOUT)
                        .automaticReconnect(TEST_AUTOMATIC_RECONNECT)
                        .will(TEST_WILL_TOPIC, TEST_WILL_PAYLOAD, TEST_WILL_QOS, TEST_WILL_RETAINED)
                        .ssl(TEST_KEYSTORE, TEST_KEYSTORE_TYPE, TEST_KEYSTORE_P, TEST_KEY_MANAGER_ALGORITHM,
                                TEST_TRUSTSTORE, TEST_TRUSTSTORE_TYPE, TEST_TRUSTSTORE_P, TEST_TRUST_MANAGER_ALGORITHM)
                        .build();

        org.eclipse.paho.client.mqttv3.MqttConnectOptions pahoOptions = MqttPahoConnectOptionsMapper.from(testOptions);

        assertEquals(TEST_USERNAME, pahoOptions.getUserName());
        assertArrayEquals(TEST_API_KEY, pahoOptions.getPassword());
        assertEquals(CORRESPONDENT_MQTT_VERSION, pahoOptions.getMqttVersion());
        assertEquals(TEST_KEEP_ALIVE_INTERVAL, pahoOptions.getKeepAliveInterval());
        assertEquals(TEST_MAX_IN_FLIGHT, pahoOptions.getMaxInflight());
        assertEquals(TEST_CLEAN_SESSION, pahoOptions.isCleanSession());
        assertEquals(TEST_CONNECTION_TIMEOUT, pahoOptions.getConnectionTimeout());
        assertEquals(TEST_WILL_TOPIC, pahoOptions.getWillDestination());
        assertArrayEquals(TEST_WILL_PAYLOAD, pahoOptions.getWillMessage().getPayload());
        assertEquals(TEST_WILL_QOS, pahoOptions.getWillMessage().getQos());
        assertEquals(TEST_WILL_RETAINED, pahoOptions.getWillMessage().isRetained());
        assertSslProperties(pahoOptions);
    }

    private void assertSslProperties(org.eclipse.paho.client.mqttv3.MqttConnectOptions pahoOptions) {
        Properties sslProperties = pahoOptions.getSSLProperties();

        assertEquals(TEST_KEYSTORE, sslProperties.get(MqttPahoConnectOptionsMapper.SYSKEYSTORE));
        assertEquals(TEST_KEYSTORE_TYPE, sslProperties.get(MqttPahoConnectOptionsMapper.SYSKEYSTORETYPE));
        assertArrayEquals(TEST_KEYSTORE_P, (char[]) sslProperties.get(MqttPahoConnectOptionsMapper.SYSKEYSTOREPWD));
        assertEquals(TEST_KEY_MANAGER_ALGORITHM, sslProperties.get(MqttPahoConnectOptionsMapper.SYSKEYMGRALGO));
        assertEquals(TEST_TRUSTSTORE, sslProperties.get(MqttPahoConnectOptionsMapper.SYSTRUSTSTORE));
        assertEquals(TEST_TRUSTSTORE_TYPE, sslProperties.get(MqttPahoConnectOptionsMapper.SYSTRUSTSTORETYPE));
        assertArrayEquals(TEST_TRUSTSTORE_P, (char[]) sslProperties.get(MqttPahoConnectOptionsMapper.SYSTRUSTSTOREPWD));
        assertEquals(TEST_TRUST_MANAGER_ALGORITHM, sslProperties.get(MqttPahoConnectOptionsMapper.SYSTRUSTMGRALGO));

    }

    @Test
    public void testFromWithMqttVersion3_1_1() {
        MqttConnectOptions testOptions =
                MqttConnectOptions.builder(TEST_USERNAME, TEST_API_KEY)
                        .mqttVersion(MqttVersion.MQTT_3_1_1)
                        .build();

        org.eclipse.paho.client.mqttv3.MqttConnectOptions pahoOptions = MqttPahoConnectOptionsMapper.from(testOptions);

        assertEquals(TEST_USERNAME, pahoOptions.getUserName());
        assertArrayEquals(TEST_API_KEY, pahoOptions.getPassword());
        assertEquals(org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1_1, pahoOptions.getMqttVersion());
    }
}