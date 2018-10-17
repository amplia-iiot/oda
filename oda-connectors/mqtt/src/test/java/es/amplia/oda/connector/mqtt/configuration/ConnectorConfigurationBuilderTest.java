package es.amplia.oda.connector.mqtt.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConnectorConfigurationBuilderTest {

    /**
     * Test constants.
     */
    private static final String TCP_PROTOCOL_URL_HEADER = "tcp://";
    private static final String SSL_PROTOCOL_URL_HEADER = "ssl://";

    /**
     * Connector configuration build default values.
     */
    private static final int DEFAULT_PORT = 1883;
    private static final int DEFAULT_SECURE_PORT = 8883;
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
     * Connector configuration build test values.
     */
    private static final String HOST = "test.host";
    private static final int PORT = 50123;
    private static final int SECURE_PORT = 50523;
    private static final String CLIENT_ID = "testidentifier";
    private static final boolean SECURE_CONNECTION = false;
    private static final int MQTT_VERSION = MqttConnectOptions.MQTT_VERSION_3_1;
    private static final boolean AUTOMATIC_RECONNECT = false;
    private static final int CONNECTION_TIMEOUT = 10;
    private static final int KEEP_ALIVE_INTERVAL = 10;
    private static final int MAX_IN_FLIGHT = 100;
    private static final boolean CLEAN_SESSION = true;
    private static final String USER_NAME = "testusername";
    private static final String PASSWORD = "testpassword";
    private static final String LWT_TOPIC = "lwttopic";
    private static final String LWT_PAYLOAD = "lwtpayload";
    private static final int LWT_QUALITY_OF_SERVICE = 1;
    private static final boolean LWT_RETAINED = true;
    private static final String KEY_STORE_PATH = "key/store/path";
    private static final String KEY_STORE_TYPE = "jks";
    private static final String KEY_STORE_PASSWORD = "keystorepassword";
    private static final String REQUEST_QUEUE = "queue/request";
    private static final String RESPONSE_QUEUE = "queue/response";
    private static final String IOT_QUEUE = "queue/iot";
    private static final int QUALITY_OF_SERVICE = 1;
    private static final boolean RETAINED = true;

    private ConnectorConfigurationBuilder testBuilder;

    @Before
    public void setUp() {
        testBuilder = ConnectorConfigurationBuilder.newBuilder();
    }

    @Test
    public void testNewBuilder() {
        assertNotNull(testBuilder);
    }

    @Test
    public void testCompleteConfiguredBuild() {
        testBuilder.setHost(HOST);
        testBuilder.setPort(PORT);
        testBuilder.setSecurePort(SECURE_PORT);
        testBuilder.setClientId(CLIENT_ID);
        testBuilder.setSecureConnection(SECURE_CONNECTION);
        testBuilder.setMqttVersion(MQTT_VERSION);
        testBuilder.setAutomaticReconnect(AUTOMATIC_RECONNECT);
        testBuilder.setConnectionTimeout(CONNECTION_TIMEOUT);
        testBuilder.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        testBuilder.setMaxInflight(MAX_IN_FLIGHT);
        testBuilder.setCleanSession(CLEAN_SESSION);
        testBuilder.setUserName(USER_NAME);
        testBuilder.setPassword(PASSWORD);
        testBuilder.setLwtTopic(LWT_TOPIC);
        testBuilder.setLwtPayload(LWT_PAYLOAD);
        testBuilder.setLwtQualityOfService(LWT_QUALITY_OF_SERVICE);
        testBuilder.setLwtRetained(LWT_RETAINED);
        testBuilder.setKeyStorePath(KEY_STORE_PATH);
        testBuilder.setKeyStoreType(KEY_STORE_TYPE);
        testBuilder.setKeyStorePassword(KEY_STORE_PASSWORD);
        testBuilder.setRequestQueue(REQUEST_QUEUE);
        testBuilder.setResponseQueue(RESPONSE_QUEUE);
        testBuilder.setIotQueue(IOT_QUEUE);
        testBuilder.setQualityOfService(QUALITY_OF_SERVICE);
        testBuilder.setRetained(RETAINED);

        ConnectorConfiguration conf = testBuilder.build();

        assertNotNull(conf);
    }

    private void setMinimumConfigurationToBuild() {
        testBuilder.setHost(HOST);
        testBuilder.setClientId(CLIENT_ID);
        testBuilder.setSecureConnection(SECURE_CONNECTION);
        testBuilder.setUserName(USER_NAME);
        testBuilder.setPassword(PASSWORD);
        testBuilder.setRequestQueue(REQUEST_QUEUE);
        testBuilder.setResponseQueue(RESPONSE_QUEUE);
        testBuilder.setIotQueue(IOT_QUEUE);
    }

    @Test
    public void testSetHost() {
        setMinimumConfigurationToBuild();
        testBuilder.setHost(HOST);

        ConnectorConfiguration conf = testBuilder.build();
        String brokerUrl = conf.getBrokerUrl();

        assertTrue(brokerUrl.contains(HOST));
    }

    @Test
    public void testSetPort() {
        setMinimumConfigurationToBuild();
        testBuilder.setPort(PORT);

        ConnectorConfiguration conf = testBuilder.build();
        String brokerUrl = conf.getBrokerUrl();

        assertTrue(brokerUrl.contains(Integer.toString(PORT)));
    }

    @Test
    public void testSetClientId() {
        setMinimumConfigurationToBuild();
        testBuilder.setClientId(CLIENT_ID);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(CLIENT_ID, conf.getClientId());
    }

    @Test
    public void testSetUnsecureConnection() {
        setMinimumConfigurationToBuild();
        testBuilder.setSecureConnection(false);

        ConnectorConfiguration conf = testBuilder.build();
        String brokerUrl = conf.getBrokerUrl();

        assertTrue(brokerUrl.contains(TCP_PROTOCOL_URL_HEADER));
    }

    @Test
    public void testSetSecureConnection() {
        setMinimumConfigurationToBuild();
        testBuilder.setSecureConnection(true);

        ConnectorConfiguration conf = testBuilder.build();
        String brokerUrl = conf.getBrokerUrl();

        assertTrue(brokerUrl.contains(SSL_PROTOCOL_URL_HEADER));
    }

    @Test
    public void testSetMqttVersion() {
        setMinimumConfigurationToBuild();
        testBuilder.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(MqttConnectOptions.MQTT_VERSION_3_1_1, conf.getMqttConnectOptions().getMqttVersion());
    }

    @Test
    public void testSetAutomaticReconnect() {
        setMinimumConfigurationToBuild();
        testBuilder.setAutomaticReconnect(AUTOMATIC_RECONNECT);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(AUTOMATIC_RECONNECT, conf.getMqttConnectOptions().isAutomaticReconnect());
    }

    @Test
    public void testSetConnectionTimeout() {
        setMinimumConfigurationToBuild();
        testBuilder.setConnectionTimeout(CONNECTION_TIMEOUT);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(CONNECTION_TIMEOUT, conf.getMqttConnectOptions().getConnectionTimeout());
    }

    @Test
    public void testSetKeepAliveInterval() {
        setMinimumConfigurationToBuild();
        testBuilder.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(KEEP_ALIVE_INTERVAL, conf.getMqttConnectOptions().getKeepAliveInterval());
    }

    @Test
    public void testSetMaxInflight() {
        setMinimumConfigurationToBuild();
        testBuilder.setMaxInflight(MAX_IN_FLIGHT);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(MAX_IN_FLIGHT, conf.getMqttConnectOptions().getMaxInflight());
    }

    @Test
    public void testSetCleanSession() {
        setMinimumConfigurationToBuild();
        testBuilder.setCleanSession(CLEAN_SESSION);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(CLEAN_SESSION, conf.getMqttConnectOptions().isCleanSession());
    }

    @Test
    public void testSetUserName() {
        setMinimumConfigurationToBuild();
        testBuilder.setUserName(USER_NAME);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(USER_NAME, conf.getMqttConnectOptions().getUserName());
    }

    @Test
    public void testSetPassword() {
        setMinimumConfigurationToBuild();
        testBuilder.setUserName(PASSWORD);

        ConnectorConfiguration conf = testBuilder.build();

        assertArrayEquals(PASSWORD.toCharArray(), conf.getMqttConnectOptions().getPassword());
    }

    private void setLwtConfiguration() {
        testBuilder.setLwtTopic(LWT_TOPIC);
        testBuilder.setLwtPayload(LWT_PAYLOAD);
        testBuilder.setLwtQualityOfService(LWT_QUALITY_OF_SERVICE);
        testBuilder.setLwtRetained(LWT_RETAINED);
    }

    @Test
    public void testSetLwtTopic() {
        setMinimumConfigurationToBuild();
        setLwtConfiguration();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(LWT_TOPIC, conf.getMqttConnectOptions().getWillDestination());
    }

    @Test
    public void testSetLwtPayload() {
        setMinimumConfigurationToBuild();
        setLwtConfiguration();

        ConnectorConfiguration conf = testBuilder.build();

        assertArrayEquals(LWT_PAYLOAD.getBytes(), conf.getMqttConnectOptions().getWillMessage().getPayload());
    }

    @Test
    public void testSetLwtQualityOfService() {
        setMinimumConfigurationToBuild();
        setLwtConfiguration();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(LWT_QUALITY_OF_SERVICE, conf.getMqttConnectOptions().getWillMessage().getQos());
    }

    @Test
    public void testSetLwtRetained() {
        setMinimumConfigurationToBuild();
        setLwtConfiguration();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(LWT_RETAINED, conf.getMqttConnectOptions().getWillMessage().isRetained());
    }

    @Test
    public void testSetResponseQueue() {
        setMinimumConfigurationToBuild();
        testBuilder.setResponseQueue(RESPONSE_QUEUE);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(RESPONSE_QUEUE, conf.getQueuesConfiguration().getResponseQueue());
    }

    @Test
    public void testSetRequestQueue() {
        setMinimumConfigurationToBuild();
        testBuilder.setRequestQueue(REQUEST_QUEUE);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(REQUEST_QUEUE, conf.getQueuesConfiguration().getRequestQueue());
    }

    @Test
    public void testSetIotQueue() {
        setMinimumConfigurationToBuild();
        testBuilder.setIotQueue(IOT_QUEUE);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(IOT_QUEUE, conf.getQueuesConfiguration().getIotQueue());
    }

    @Test
    public void testSetQualityOfService() {
        setMinimumConfigurationToBuild();
        testBuilder.setQualityOfService(QUALITY_OF_SERVICE);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(QUALITY_OF_SERVICE, conf.getQueuesConfiguration().getQualityOfService());
    }

    @Test
    public void testSetRetained() {
        setMinimumConfigurationToBuild();
        testBuilder.setRetained(RETAINED);

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(RETAINED, conf.getQueuesConfiguration().isRetained());
    }

    @Test
    public void testDefaultPortAndDefaultSecureConnection() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();
        String brokerUrl = conf.getBrokerUrl();

        assertTrue(brokerUrl.endsWith(Integer.toString(DEFAULT_PORT)));
    }

    @Test
    public void testDefaultSecurePort() {
        setMinimumConfigurationToBuild();
        testBuilder.setSecureConnection(true);

        ConnectorConfiguration conf = testBuilder.build();
        String brokerUrl = conf.getBrokerUrl();

        assertTrue(brokerUrl.endsWith(Integer.toString(DEFAULT_SECURE_PORT)));
    }

    @Test
    public void testDefaultMqttVersion() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(DEFAULT_MQTT_VERSION, conf.getMqttConnectOptions().getMqttVersion());
    }

    @Test
    public void testDefaultAutomaticReconnect() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(DEFAULT_AUTOMATIC_RECONNECT, conf.getMqttConnectOptions().isAutomaticReconnect());
    }

    @Test
    public void testDefaultConnectionTimeout() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(DEFAULT_CONNECTION_TIMEOUT, conf.getMqttConnectOptions().getConnectionTimeout());
    }

    @Test
    public void testDefaultKeepAliveInterval() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(DEFAULT_KEEP_ALIVE_INTERVAL, conf.getMqttConnectOptions().getKeepAliveInterval());
    }

    @Test
    public void testDefaultMaxInflight() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(DEFAULT_MAX_INFLIGHT, conf.getMqttConnectOptions().getMaxInflight());
    }

    @Test
    public void testDefaultCleanSession() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(DEFAULT_CLEAN_SESSION, conf.getMqttConnectOptions().isCleanSession());
    }

    private void setMinimumLwtConfiguration() {
        testBuilder.setLwtTopic(LWT_TOPIC);
        testBuilder.setLwtPayload(LWT_PAYLOAD);
    }

    @Test
    public void testDefaultLwtQualityOfService() {
        setMinimumConfigurationToBuild();
        setMinimumLwtConfiguration();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(DEFAULT_LWT_QUALITY_OF_SERVICE, conf.getMqttConnectOptions().getWillMessage().getQos());
    }

    @Test
    public void testDefaultLwtRetained() {
        setMinimumConfigurationToBuild();
        setMinimumLwtConfiguration();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(DEFAULT_LWT_RETAINED, conf.getMqttConnectOptions().getWillMessage().isRetained());
    }

    @Test
    public void testDefaultQualityOfService() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(DEFAULT_QUALITY_OF_SERVICE, conf.getQueuesConfiguration().getQualityOfService());
    }

    @Test
    public void testDefaultRetained() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertEquals(DEFAULT_RETAINED, conf.getQueuesConfiguration().isRetained());
    }

    @Test
    public void testOptionalLwtConfiguration() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertNull(conf.getMqttConnectOptions().getWillDestination());
        assertNull(conf.getMqttConnectOptions().getWillMessage());
    }

    @Test
    public void testOptionalSslConfiguration() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertNull(conf.getMqttConnectOptions().getSSLProperties());
        assertNull(conf.getMqttConnectOptions().getSSLHostnameVerifier());
        assertNull(conf.getMqttConnectOptions().getSocketFactory());
    }

    @Test
    public void testOnlyRequiredConfigurationBuild() {
        setMinimumConfigurationToBuild();

        ConnectorConfiguration conf = testBuilder.build();

        assertNotNull(conf);
        assertNotNull(conf.getBrokerUrl());
        assertNotNull(conf.getClientId());
        assertNotNull(conf.getMqttConnectOptions());
        assertNotNull(conf.getQueuesConfiguration());
    }

    @Test(expected = ConfigurationException.class)
    public void testUnconfiguredBuild() {
        testBuilder.build();
        fail("Build without configuration must throw an exception");
    }

    @Test(expected = ConfigurationException.class)
    public void testMissingHostBuild() {
        // Not configure host
        testBuilder.setClientId(CLIENT_ID);
        testBuilder.setSecureConnection(SECURE_CONNECTION);
        testBuilder.setUserName(USER_NAME);
        testBuilder.setPassword(PASSWORD);
        testBuilder.setRequestQueue(REQUEST_QUEUE);
        testBuilder.setResponseQueue(RESPONSE_QUEUE);
        testBuilder.setIotQueue(IOT_QUEUE);

        testBuilder.build();
        fail("Build without host must throw an exception");
    }

    @Test(expected = ConfigurationException.class)
    public void testMissingClientIdBuild() {
        testBuilder.setHost(HOST);
        // Not configure Client id
        testBuilder.setSecureConnection(SECURE_CONNECTION);
        testBuilder.setUserName(USER_NAME);
        testBuilder.setPassword(PASSWORD);
        testBuilder.setRequestQueue(REQUEST_QUEUE);
        testBuilder.setResponseQueue(RESPONSE_QUEUE);
        testBuilder.setIotQueue(IOT_QUEUE);

        testBuilder.build();
        fail("Build without client identifier must throw an exception");
    }

    @Test(expected = ConfigurationException.class)
    public void testMissingUserNameBuild() {
        testBuilder.setHost(HOST);
        testBuilder.setClientId(CLIENT_ID);
        testBuilder.setSecureConnection(SECURE_CONNECTION);
        // Not configure user name
        testBuilder.setPassword(PASSWORD);
        testBuilder.setRequestQueue(REQUEST_QUEUE);
        testBuilder.setResponseQueue(RESPONSE_QUEUE);
        testBuilder.setIotQueue(IOT_QUEUE);

        testBuilder.build();
        fail("Build without user name must throw an exception");
    }

    @Test(expected = ConfigurationException.class)
    public void testMissingPasswordBuild() {
        testBuilder.setHost(HOST);
        testBuilder.setClientId(CLIENT_ID);
        testBuilder.setSecureConnection(SECURE_CONNECTION);
        testBuilder.setUserName(USER_NAME);
        // Not configure password
        testBuilder.setRequestQueue(REQUEST_QUEUE);
        testBuilder.setResponseQueue(RESPONSE_QUEUE);
        testBuilder.setIotQueue(IOT_QUEUE);

        testBuilder.build();
        fail("Build without password must throw an exception");
    }

    @Test(expected = ConfigurationException.class)
    public void testMissingRequestQueueBuild() {
        testBuilder.setHost(HOST);
        testBuilder.setClientId(CLIENT_ID);
        testBuilder.setSecureConnection(SECURE_CONNECTION);
        testBuilder.setUserName(USER_NAME);
        testBuilder.setPassword(PASSWORD);
        // Not configure request queue
        testBuilder.setResponseQueue(RESPONSE_QUEUE);
        testBuilder.setIotQueue(IOT_QUEUE);

        testBuilder.build();
        fail("Build without request queue must throw an exception");
    }

    @Test(expected = ConfigurationException.class)
    public void testMissingResponseQueueBuild() {
        testBuilder.setHost(HOST);
        testBuilder.setClientId(CLIENT_ID);
        testBuilder.setSecureConnection(SECURE_CONNECTION);
        testBuilder.setUserName(USER_NAME);
        testBuilder.setPassword(PASSWORD);
        testBuilder.setRequestQueue(REQUEST_QUEUE);
        // Not configure response queue
        testBuilder.setIotQueue(IOT_QUEUE);

        testBuilder.build();
        fail("Build without response queue must throw an exception");
    }

    @Test(expected = ConfigurationException.class)
    public void testMissingIotQueueBuild() {
        testBuilder.setHost(HOST);
        testBuilder.setClientId(CLIENT_ID);
        testBuilder.setSecureConnection(SECURE_CONNECTION);
        testBuilder.setUserName(USER_NAME);
        testBuilder.setPassword(PASSWORD);
        testBuilder.setRequestQueue(REQUEST_QUEUE);
        testBuilder.setResponseQueue(RESPONSE_QUEUE);
        // Not configure IOT queue

        testBuilder.build();
        fail("Build without IOT queue must throw an exception");
    }
}