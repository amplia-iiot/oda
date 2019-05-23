package es.amplia.oda.comms.mqtt.api;

import org.junit.Test;

import java.util.UUID;

import static es.amplia.oda.comms.mqtt.api.MqttConnectOptions.*;

import static org.junit.Assert.*;

public class MqttConnectOptionsTest {

    private static final String TEST_USERNAME = "testUser";
    private static final char[] TEST_API_KEY = UUID.randomUUID().toString().toCharArray();
    private static final MqttVersion TEST_MQTT_VERSION = MqttVersion.MQTT_3_1;
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
    public void testCreateCompleteMqttConnectOptions() {
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

        assertEquals(TEST_USERNAME, testOptions.getUsername());
        assertArrayEquals(TEST_API_KEY, testOptions.getPassword());
        assertEquals(TEST_MQTT_VERSION, testOptions.getMqttVersion());
        assertEquals(TEST_KEEP_ALIVE_INTERVAL, testOptions.getKeepAliveInterval());
        assertEquals(TEST_MAX_IN_FLIGHT, testOptions.getMaxInFlight());
        assertEquals(TEST_CLEAN_SESSION, testOptions.isCleanSession());
        assertEquals(TEST_CONNECTION_TIMEOUT, testOptions.getConnectionTimeout());
        assertEquals(TEST_AUTOMATIC_RECONNECT, testOptions.isAutomaticReconnect());
        WillOptions willOptionsCaptured = testOptions.getWill();
        assertEquals(TEST_WILL_TOPIC, willOptionsCaptured.getTopic());
        assertArrayEquals(TEST_WILL_PAYLOAD, willOptionsCaptured.getPayload());
        assertEquals(TEST_WILL_QOS, willOptionsCaptured.getQos());
        assertEquals(TEST_WILL_RETAINED, willOptionsCaptured.isRetained());
        SslOptions sslOptionsCaptured = testOptions.getSsl();
        assertEquals(TEST_KEYSTORE, sslOptionsCaptured.getKeyStore());
        assertEquals(TEST_KEYSTORE_TYPE, sslOptionsCaptured.getKeyStoreType());
        assertArrayEquals(TEST_KEYSTORE_P, sslOptionsCaptured.getKeyStorePassword());
        assertEquals(TEST_KEY_MANAGER_ALGORITHM, sslOptionsCaptured.getKeyManagerFactoryAlgorithm());
        assertEquals(TEST_TRUSTSTORE, sslOptionsCaptured.getTrustStore());
        assertEquals(TEST_TRUSTSTORE_TYPE, sslOptionsCaptured.getTrustStoreType());
        assertArrayEquals(TEST_TRUSTSTORE_P, sslOptionsCaptured.getTrustStorePassword());
        assertEquals(TEST_TRUST_MANAGER_ALGORITHM, sslOptionsCaptured.getTrustManagerFactoryAlgorithm());
    }

    @Test
    public void testCreateMqttConnectOptionsWithDefaultWillOptions() {
        MqttConnectOptions testOptions =
                MqttConnectOptions.builder(TEST_USERNAME, TEST_API_KEY)
                        .will(TEST_WILL_TOPIC, TEST_WILL_PAYLOAD)
                        .build();

        assertEquals(TEST_USERNAME, testOptions.getUsername());
        assertArrayEquals(TEST_API_KEY, testOptions.getPassword());
        WillOptions willOptionsCaptured = testOptions.getWill();
        assertEquals(TEST_WILL_TOPIC, willOptionsCaptured.getTopic());
        assertArrayEquals(TEST_WILL_PAYLOAD, willOptionsCaptured.getPayload());
        assertEquals(WillOptions.DEFAULT_QOS, willOptionsCaptured.getQos());
        assertEquals(WillOptions.DEFAULT_RETAINED, willOptionsCaptured.isRetained());
    }

    @Test
    public void testCreateMqttConnectOptionsWithDefaultSslOptions() {
        MqttConnectOptions testOptions =
                MqttConnectOptions.builder(TEST_USERNAME, TEST_API_KEY)
                        .ssl(TEST_KEYSTORE, TEST_KEYSTORE_P, TEST_TRUSTSTORE, TEST_TRUSTSTORE_P)
                        .build();

        assertEquals(TEST_USERNAME, testOptions.getUsername());
        assertArrayEquals(TEST_API_KEY, testOptions.getPassword());
        SslOptions sslOptionsCaptured = testOptions.getSsl();
        assertEquals(TEST_KEYSTORE, sslOptionsCaptured.getKeyStore());
        assertEquals(SslOptions.DEFAULT_KEY_STORE_TYPE, sslOptionsCaptured.getKeyStoreType());
        assertArrayEquals(TEST_KEYSTORE_P, sslOptionsCaptured.getKeyStorePassword());
        assertEquals(SslOptions.DEFAULT_KEY_MANAGER_ALGORITHM, sslOptionsCaptured.getKeyManagerFactoryAlgorithm());
        assertEquals(TEST_TRUSTSTORE, sslOptionsCaptured.getTrustStore());
        assertEquals(SslOptions.DEFAULT_KEY_STORE_TYPE, sslOptionsCaptured.getTrustStoreType());
        assertArrayEquals(TEST_TRUSTSTORE_P, sslOptionsCaptured.getTrustStorePassword());
        assertEquals(SslOptions.DEFAULT_KEY_MANAGER_ALGORITHM, sslOptionsCaptured.getTrustManagerFactoryAlgorithm());
    }

    @Test
    public void testCreateMqttConnectOptionsWithDifferentKeyStoreTypesAndKeyManagerAlgorithms() {
        MqttConnectOptions testOptions =
                MqttConnectOptions.builder(TEST_USERNAME, TEST_API_KEY)
                        .ssl(TEST_KEYSTORE, KeyStoreType.BKS,  TEST_KEYSTORE_P, KeyManagerAlgorithm.SUN_JSSE,
                             TEST_TRUSTSTORE, KeyStoreType.DKS, TEST_TRUSTSTORE_P, KeyManagerAlgorithm.SUN_X509)
                        .build();

        assertEquals(TEST_USERNAME, testOptions.getUsername());
        assertArrayEquals(TEST_API_KEY, testOptions.getPassword());
        SslOptions sslOptionsCaptured = testOptions.getSsl();
        assertEquals(TEST_KEYSTORE, sslOptionsCaptured.getKeyStore());
        assertEquals(KeyStoreType.BKS, sslOptionsCaptured.getKeyStoreType());
        assertArrayEquals(TEST_KEYSTORE_P, sslOptionsCaptured.getKeyStorePassword());
        assertEquals(KeyManagerAlgorithm.SUN_JSSE, sslOptionsCaptured.getKeyManagerFactoryAlgorithm());
        assertEquals(TEST_TRUSTSTORE, sslOptionsCaptured.getTrustStore());
        assertEquals(KeyStoreType.DKS, sslOptionsCaptured.getTrustStoreType());
        assertArrayEquals(TEST_TRUSTSTORE_P, sslOptionsCaptured.getTrustStorePassword());
        assertEquals(KeyManagerAlgorithm.SUN_X509, sslOptionsCaptured.getTrustManagerFactoryAlgorithm());
    }

    @Test
    public void testCreateMqttConnectOptionsWithDefaults() {
        MqttConnectOptions testOptions = MqttConnectOptions.builder(TEST_USERNAME, TEST_API_KEY).build();

        assertEquals(TEST_USERNAME, testOptions.getUsername());
        assertArrayEquals(TEST_API_KEY, testOptions.getPassword());
        assertEquals(DEFAULT_MQTT_VERSION, testOptions.getMqttVersion());
        assertEquals(DEFAULT_KEEP_ALIVE_INTERVAL, testOptions.getKeepAliveInterval());
        assertEquals(DEFAULT_MAX_IN_FLIGHT, testOptions.getMaxInFlight());
        assertEquals(DEFAULT_CLEAN_SESSION, testOptions.isCleanSession());
        assertEquals(DEFAULT_CONNECTION_TIMEOUT, testOptions.getConnectionTimeout());
        assertEquals(DEFAULT_AUTOMATIC_RECONNECT, testOptions.isAutomaticReconnect());
        assertNull(testOptions.getWill());
        assertNull(testOptions.getSsl());
    }
}