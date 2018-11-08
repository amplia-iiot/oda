package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ConnectorConfigurationTest {

    private static final String TCP_URL_HEADER = "tcp://";
    private static final String SSL_URL_HEADER = "ssl://";

    private static final String HOST = "test.host";
    private static final int PORT = 1883;
    private static final int SECURE_PORT = 8883;
    private static final String CLIENT_ID = "testidentifier";
    private static final boolean SECURE_CONNECTION = true;
    private static final boolean UNSECURE_CONNECTION = false;

    private ConnectionConfiguration mockerConnectionConfiguration;
    private LwtConfiguration mockedLwtConfiguration;
    private SslConfiguration mockedSslConfiguration;
    private QueuesConfiguration mockedQueuesConfiguration;

    private ConnectorConfiguration testConnectorConfiguration;

    @Before
    public void setUp() {
        mockerConnectionConfiguration = mock(ConnectionConfiguration.class);
        mockedLwtConfiguration = mock(LwtConfiguration.class);
        mockedSslConfiguration = mock(SslConfiguration.class);
        mockedQueuesConfiguration = mock(QueuesConfiguration.class);

        testConnectorConfiguration = new ConnectorConfiguration(HOST, PORT, SECURE_PORT, CLIENT_ID, SECURE_CONNECTION,
                mockerConnectionConfiguration, mockedLwtConfiguration, mockedSslConfiguration,
                mockedQueuesConfiguration);
    }

    @Test
    public void testConstructor() {
        assertNotNull(testConnectorConfiguration);
    }

    @Test
    public void testGetBrokerUrlUnsecure() {
        String brokerUrl = testConnectorConfiguration.getBrokerUrl();

        assertTrue(brokerUrl.startsWith(SSL_URL_HEADER));
        assertTrue(brokerUrl.endsWith(Integer.toString(SECURE_PORT)));
    }

    @Test
    public void testGetClientId() {
        assertEquals(CLIENT_ID, testConnectorConfiguration.getClientId());
    }

    @Test
    public void testGetBrokerUrlSecure() {
        ConnectorConfiguration unsecureConnectorConfiguration = new ConnectorConfiguration(HOST, PORT, SECURE_PORT,
                CLIENT_ID, UNSECURE_CONNECTION, mockerConnectionConfiguration, mockedLwtConfiguration,
                mockedSslConfiguration, mockedQueuesConfiguration);
        String brokerUrl = unsecureConnectorConfiguration.getBrokerUrl();

        assertTrue(brokerUrl.startsWith(TCP_URL_HEADER));
        assertTrue(brokerUrl.endsWith(Integer.toString(PORT)));
    }

    @Test
    public void testGetMqttConnectOptions() {
        MqttConnectOptions options = testConnectorConfiguration.getMqttConnectOptions();

        verify(mockerConnectionConfiguration).configure(eq(options));
        verify(mockedLwtConfiguration).configure(eq(options));
        verify(mockedSslConfiguration).configure(eq(options));
    }

    @Test
    public void testGetQueuesConfiguration() {
        assertEquals(mockedQueuesConfiguration, testConnectorConfiguration.getQueuesConfiguration());
    }

}