package es.amplia.oda.connector.mqtt.configuration;

import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.connector.mqtt.MqttConnector;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

import static es.amplia.oda.connector.mqtt.configuration.ConfigurationUpdateHandlerImpl.*;
import static es.amplia.oda.comms.mqtt.api.MqttConnectOptions.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationUpdateHandlerImplTest {

    private static final String TEST_DEVICE_ID = "testUser";
    private static final char[] TEST_API_KEY = UUID.randomUUID().toString().toCharArray();
    private static final MqttVersion TEST_MQTT_VERSION = MqttVersion.MQTT_3_1_1;
    private static final int TEST_KEEP_ALIVE_INTERVAL = 45;
    private static final int TEST_MAX_IN_FLIGHT = 15;
    private static final boolean TEST_CLEAN_SESSION = false;
    private static final int TEST_CONNECTION_TIMEOUT = 25;
    private static final boolean TEST_AUTOMATIC_RECONNECT = false;
    private static final String TEST_LWT_TOPIC = "lwt/topic";
    private static final byte[] TEST_LWT_PAYLOAD = new byte[] { 1, 2, 3, 4 };
    private static final int TEST_LWT_QOS = 2;
    private static final boolean TEST_LWT_RETAINED = true;
    private static final String TEST_KEY_STORE_PATH = "/path/to/keystore";
    private static final KeyStoreType TEST_KEY_STORE_TYPE = KeyStoreType.PKCS12;
    private static final char[] TEST_KEY_STORE_P = "keyStoreP".toCharArray();
    private static final KeyManagerAlgorithm TEST_KEY_MANAGER_ALGORITHM = KeyManagerAlgorithm.SUN_JSSE;
    private static final String TEST_TRUST_STORE_PATH = "/path/to/truststore";
    private static final KeyStoreType TEST_TRUST_STORE_TYPE = KeyStoreType.PKCS11;
    private static final char[] TEST_TRUST_STORE_P = "trustStoreP".toCharArray();
    private static final KeyManagerAlgorithm TEST_TRUST_MANAGER_ALGORITHM = KeyManagerAlgorithm.SUN_X509;
    private static final String TEST_HOST = "test.url.es";
    private static final int TEST_PORT = 1234;
    private static final int TEST_SECURE_PORT = 5678;
    private static final String TEST_BROKER_URL = "tcp://" + TEST_HOST + ":" + TEST_PORT;
    private static final String TEST_BROKER_URL_WITH_DEFAULT_PORT = "tcp://" + TEST_HOST + ":" + DEFAULT_PORT;
    private static final String TEST_BROKER_SECURE_URL = "ssl://" + TEST_HOST + ":" + TEST_SECURE_PORT;
    private static final String TEST_BROKER_SECURE_URL_WITH_DEFAULT_PORT = "ssl://" + TEST_HOST + ":" + DEFAULT_SECURE_PORT;
    private static final String TEST_IOT_TOPIC = "iot/topic";
    private static final String TEST_REQUEST_TOPIC = "request/topic";
    private static final String TEST_RESPONSE_TOPIC = "response/topic";
    private static final int TEST_QOS = 2;
    private static final boolean TEST_RETAINED = false;

    @Mock
    private MqttConnector mockedConnector;
    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @InjectMocks
    private ConfigurationUpdateHandlerImpl testConfigHandler;

    @Test
    public void testLoadCompleteConfiguration() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(PORT_PROPERTY_NAME, Integer.toString(TEST_PORT));
        testProperties.put(SECURE_PORT_PROPERTY_NAME, Integer.toString(TEST_SECURE_PORT));
        testProperties.put(SECURE_CONNECTION_PROPERTY_NAME, Boolean.toString(false));
        testProperties.put(MQTT_VERSION_PROPERTY_NAME, TEST_MQTT_VERSION.toString());
        testProperties.put(KEEP_ALIVE_INTERVAL_PROPERTY_NAME, Integer.toString(TEST_KEEP_ALIVE_INTERVAL));
        testProperties.put(MAX_IN_FLIGHT_PROPERTY_NAME, Integer.toString(TEST_MAX_IN_FLIGHT));
        testProperties.put(CLEAN_SESSION_PROPERTY_NAME, Boolean.toString(TEST_CLEAN_SESSION));
        testProperties.put(CONNECTION_TIMEOUT_PROPERTY, Integer.toString(TEST_CONNECTION_TIMEOUT));
        testProperties.put(AUTOMATIC_RECONNECT_PROPERTY_NAME, Boolean.toString(TEST_AUTOMATIC_RECONNECT));
        testProperties.put(LWT_TOPIC_PROPERTY_NAME, TEST_LWT_TOPIC);
        testProperties.put(LWT_PAYLOAD_PROPERTY_NAME, new String(TEST_LWT_PAYLOAD));
        testProperties.put(LWT_QOS_PROPERTY_NAME, Integer.toString(TEST_LWT_QOS));
        testProperties.put(LWT_RETAINED_PROPERTY_NAME, Boolean.toString(TEST_LWT_RETAINED));
        testProperties.put(KEY_STORE_PATH_PROPERTY_NAME, TEST_KEY_STORE_PATH);
        testProperties.put(KEY_STORE_TYPE_PROPERTY_NAME, TEST_KEY_STORE_TYPE.toString());
        testProperties.put(KEY_STORE_PASSWORD_PROPERTY_NAME, new String(TEST_KEY_STORE_P));
        testProperties.put(KEY_MANAGER_ALGORITHM_PROPERTY_NAME, TEST_KEY_MANAGER_ALGORITHM.toString());
        testProperties.put(TRUST_STORE_PATH_PROPERTY_NAME, TEST_TRUST_STORE_PATH);
        testProperties.put(TRUST_STORE_TYPE_PROPERTY_NAME, TEST_TRUST_STORE_TYPE.toString());
        testProperties.put(TRUST_STORE_PASSWORD_PROPERTY_NAME, new String(TEST_TRUST_STORE_P));
        testProperties.put(TRUST_MANAGER_ALGORITHM_PROPERTY_NAME, TEST_TRUST_MANAGER_ALGORITHM.toString());
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);
        testProperties.put(MESSAGE_QOS_PROPERTY_NAME, Integer.toString(TEST_QOS));
        testProperties.put(MESSAGE_RETAINED_PROPERTY, Boolean.toString(TEST_RETAINED));
        MqttConnectOptions expectedOptions =
                MqttConnectOptions.builder(TEST_DEVICE_ID, TEST_API_KEY)
                        .mqttVersion(TEST_MQTT_VERSION)
                        .keepAliveInterval(TEST_KEEP_ALIVE_INTERVAL)
                        .maxInFlight(TEST_MAX_IN_FLIGHT)
                        .cleanSession(TEST_CLEAN_SESSION)
                        .connectionTimeout(TEST_CONNECTION_TIMEOUT)
                        .automaticReconnect(TEST_AUTOMATIC_RECONNECT)
                        .will(getExpectedTopic(TEST_LWT_TOPIC), TEST_LWT_PAYLOAD, TEST_LWT_QOS, TEST_LWT_RETAINED)
                        .ssl(TEST_KEY_STORE_PATH, TEST_KEY_STORE_TYPE, TEST_KEY_STORE_P, TEST_KEY_MANAGER_ALGORITHM,
                                TEST_TRUST_STORE_PATH, TEST_TRUST_STORE_TYPE, TEST_TRUST_STORE_P,
                                TEST_TRUST_MANAGER_ALGORITHM)
                        .build();
        ConnectorConfiguration expectedConfiguration =
                new ConnectorConfiguration(TEST_BROKER_URL, TEST_DEVICE_ID, expectedOptions,
                        getExpectedTopic(TEST_IOT_TOPIC), getExpectedTopic(TEST_REQUEST_TOPIC),
                        getExpectedTopic(TEST_RESPONSE_TOPIC), TEST_QOS, TEST_RETAINED);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedDeviceInfoProvider).getApiKey();
        assertEquals(expectedConfiguration, Whitebox.getInternalState(testConfigHandler, "currentConfiguration"));
    }

    private String getExpectedTopic(String topic) {
        return topic + "/" + TEST_DEVICE_ID;
    }

    @Test
    public void testLoadMinimumConfiguration() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);
        MqttConnectOptions expectedOptions = MqttConnectOptions.builder(TEST_DEVICE_ID, TEST_API_KEY).build();
        ConnectorConfiguration expectedConfiguration =
                new ConnectorConfiguration(TEST_BROKER_URL_WITH_DEFAULT_PORT, TEST_DEVICE_ID, expectedOptions,
                        getExpectedTopic(TEST_IOT_TOPIC), getExpectedTopic(TEST_REQUEST_TOPIC),
                        getExpectedTopic(TEST_RESPONSE_TOPIC), DEFAULT_QOS, DEFAULT_RETAINED);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedDeviceInfoProvider).getApiKey();
        assertEquals(expectedConfiguration, Whitebox.getInternalState(testConfigHandler, "currentConfiguration"));
    }

    @Test
    public void testLoadSecureConfiguration() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(SECURE_PORT_PROPERTY_NAME, Integer.toString(TEST_SECURE_PORT));
        testProperties.put(SECURE_CONNECTION_PROPERTY_NAME, Boolean.toString(true));
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);
        MqttConnectOptions expectedOptions = MqttConnectOptions.builder(TEST_DEVICE_ID, TEST_API_KEY).build();
        ConnectorConfiguration expectedConfiguration =
                new ConnectorConfiguration(TEST_BROKER_SECURE_URL, TEST_DEVICE_ID, expectedOptions,
                        getExpectedTopic(TEST_IOT_TOPIC), getExpectedTopic(TEST_REQUEST_TOPIC),
                        getExpectedTopic(TEST_RESPONSE_TOPIC), DEFAULT_QOS, DEFAULT_RETAINED);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedDeviceInfoProvider).getApiKey();
        assertEquals(expectedConfiguration, Whitebox.getInternalState(testConfigHandler, "currentConfiguration"));
    }

    @Test
    public void testLoadSecureConfigurationWithDefaultPort() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(SECURE_CONNECTION_PROPERTY_NAME, Boolean.toString(true));
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);
        MqttConnectOptions expectedOptions = MqttConnectOptions.builder(TEST_DEVICE_ID, TEST_API_KEY).build();
        ConnectorConfiguration expectedConfiguration =
                new ConnectorConfiguration(TEST_BROKER_SECURE_URL_WITH_DEFAULT_PORT, TEST_DEVICE_ID, expectedOptions,
                        getExpectedTopic(TEST_IOT_TOPIC), getExpectedTopic(TEST_REQUEST_TOPIC),
                        getExpectedTopic(TEST_RESPONSE_TOPIC), DEFAULT_QOS, DEFAULT_RETAINED);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedDeviceInfoProvider).getApiKey();
        assertEquals(expectedConfiguration, Whitebox.getInternalState(testConfigHandler, "currentConfiguration"));
    }

    @Test
    public void testLoadMinimumConfigurationWithMinimumWillConfiguration() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);
        testProperties.put(LWT_TOPIC_PROPERTY_NAME, TEST_LWT_TOPIC);
        testProperties.put(LWT_PAYLOAD_PROPERTY_NAME, new String(TEST_LWT_PAYLOAD));
        MqttConnectOptions expectedOptions = MqttConnectOptions.builder(TEST_DEVICE_ID, TEST_API_KEY)
                .will(getExpectedTopic(TEST_LWT_TOPIC), TEST_LWT_PAYLOAD).build();
        ConnectorConfiguration expectedConfiguration =
                new ConnectorConfiguration(TEST_BROKER_URL_WITH_DEFAULT_PORT, TEST_DEVICE_ID, expectedOptions,
                        getExpectedTopic(TEST_IOT_TOPIC), getExpectedTopic(TEST_REQUEST_TOPIC),
                        getExpectedTopic(TEST_RESPONSE_TOPIC), DEFAULT_QOS, DEFAULT_RETAINED);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedDeviceInfoProvider).getApiKey();
        assertEquals(expectedConfiguration, Whitebox.getInternalState(testConfigHandler, "currentConfiguration"));
    }

    @Test
    public void testLoadConfiguration3_1_1_MqttVersion() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(MQTT_VERSION_PROPERTY_NAME, "3.1.1");
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);
        MqttConnectOptions expectedOptions = MqttConnectOptions.builder(TEST_DEVICE_ID, TEST_API_KEY)
                .mqttVersion(MqttVersion.MQTT_3_1_1).build();
        ConnectorConfiguration expectedConfiguration =
                new ConnectorConfiguration(TEST_BROKER_URL_WITH_DEFAULT_PORT, TEST_DEVICE_ID, expectedOptions,
                        getExpectedTopic(TEST_IOT_TOPIC), getExpectedTopic(TEST_REQUEST_TOPIC),
                        getExpectedTopic(TEST_RESPONSE_TOPIC), DEFAULT_QOS, DEFAULT_RETAINED);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedDeviceInfoProvider).getApiKey();
        assertEquals(expectedConfiguration, Whitebox.getInternalState(testConfigHandler, "currentConfiguration"));
    }

    @Test
    public void testLoadConfiguration3_1_MqttVersion() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(MQTT_VERSION_PROPERTY_NAME, "3.1");
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);
        MqttConnectOptions expectedOptions = MqttConnectOptions.builder(TEST_DEVICE_ID, TEST_API_KEY)
                .mqttVersion(MqttVersion.MQTT_3_1).build();
        ConnectorConfiguration expectedConfiguration =
                new ConnectorConfiguration(TEST_BROKER_URL_WITH_DEFAULT_PORT, TEST_DEVICE_ID, expectedOptions,
                        getExpectedTopic(TEST_IOT_TOPIC), getExpectedTopic(TEST_REQUEST_TOPIC),
                        getExpectedTopic(TEST_RESPONSE_TOPIC), DEFAULT_QOS, DEFAULT_RETAINED);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedDeviceInfoProvider).getApiKey();
        assertEquals(expectedConfiguration, Whitebox.getInternalState(testConfigHandler, "currentConfiguration"));
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationMissingDeviceId() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(SECURE_CONNECTION_PROPERTY_NAME, Boolean.toString(true));
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(null);

        testConfigHandler.loadConfiguration(testProperties);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationMissingApiKey() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(SECURE_CONNECTION_PROPERTY_NAME, Boolean.toString(true));
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(null);

        testConfigHandler.loadConfiguration(testProperties);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationMissingHostRequiredField() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationMissingIotTopicRequiredField() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadMinimumMissingRequestTopicRequiredField() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadMinimumMissingResponseTopicRequiredField() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationPortIllegalArgumentException() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(PORT_PROPERTY_NAME, "invalid");
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);
    }

    @Test(expected = ConfigurationException.class)
    public void testLoadConfigurationMqttVersionIllegalArgumentException() {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(MQTT_VERSION_PROPERTY_NAME, "invalid");
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.loadConfiguration(testProperties);
    }

    @Test
    public void testApplyConfiguration() throws MqttException {
        MqttConnectOptions currentOptions = MqttConnectOptions.builder(TEST_DEVICE_ID, TEST_API_KEY).build();
        ConnectorConfiguration currentConfiguration =
                new ConnectorConfiguration(TEST_BROKER_URL, TEST_DEVICE_ID, currentOptions, TEST_IOT_TOPIC,
                        TEST_REQUEST_TOPIC, TEST_RESPONSE_TOPIC, TEST_QOS, TEST_RETAINED);
        Whitebox.setInternalState(testConfigHandler, "currentConfiguration", currentConfiguration);

        testConfigHandler.applyConfiguration();

        verify(mockedConnector).loadConfigurationAndInit(eq(currentConfiguration));
    }

    @Test
    public void testReapplyConfiguration() throws MqttException {
        Dictionary<String, String> testProperties = new Hashtable<>();
        testProperties.put(HOST_PROPERTY_NAME, TEST_HOST);
        testProperties.put(IOT_TOPIC_PROPERTY_NAME, TEST_IOT_TOPIC);
        testProperties.put(REQUEST_TOPIC_PROPERTY_NAME, TEST_REQUEST_TOPIC);
        testProperties.put(RESPONSE_TOPIC_PROPERTY_NAME, TEST_RESPONSE_TOPIC);
        MqttConnectOptions expectedOptions = MqttConnectOptions.builder(TEST_DEVICE_ID, TEST_API_KEY).build();
        ConnectorConfiguration expectedConfiguration =
                new ConnectorConfiguration(TEST_BROKER_URL_WITH_DEFAULT_PORT, TEST_DEVICE_ID, expectedOptions,
                        getExpectedTopic(TEST_IOT_TOPIC), getExpectedTopic(TEST_REQUEST_TOPIC),
                        getExpectedTopic(TEST_RESPONSE_TOPIC), DEFAULT_QOS, DEFAULT_RETAINED);

        Whitebox.setInternalState(testConfigHandler, "lastProperties", testProperties);

        when(mockedDeviceInfoProvider.getDeviceId()).thenReturn(TEST_DEVICE_ID);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(new String(TEST_API_KEY));

        testConfigHandler.reapplyConfiguration();

        verify(mockedDeviceInfoProvider).getDeviceId();
        verify(mockedDeviceInfoProvider).getApiKey();
        verify(mockedConnector).loadConfigurationAndInit(eq(expectedConfiguration));
    }

    @Test
    public void testReapplyConfigurationWithNoLastProperties() throws MqttException {
        Whitebox.setInternalState(testConfigHandler, "lastProperties", null);

        testConfigHandler.reapplyConfiguration();

        verifyZeroInteractions(mockedConnector);
    }
}