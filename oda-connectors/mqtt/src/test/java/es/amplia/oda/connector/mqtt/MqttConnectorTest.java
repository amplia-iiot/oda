package es.amplia.oda.connector.mqtt;

import es.amplia.oda.comms.mqtt.api.*;
import es.amplia.oda.connector.mqtt.configuration.ConnectorConfiguration;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MqttConnectorTest {

    private static final String TEST_USERNAME = "testUser";
    private static final char[] TEST_API_KEY = UUID.randomUUID().toString().toCharArray();
    private static final String TEST_BROKER_URL = "tcp://test.url.es";
    private static final String TEST_CLIENT_ID = "testClient";
    private static final String TEST_IOT_TOPIC = "iot/topic";
    private static final String TEST_REQUEST_TOPIC = "request/topic";
    private static final String TEST_RESPONSE_TOPIC = "response/topic";
    private static final int TEST_QOS = 0;
    private static final boolean TEST_RETAINED = false;

    private static final String TEST_TOPIC = "test/topic";
    private static final byte[] TEST_PAYLOAD = new byte[] { 1, 2, 3, 4 };
    private static final MqttMessage TEST_MESSAGE = MqttMessage.newInstance(TEST_PAYLOAD);

    private static final String CLIENT_FIELD_NAME = "client";
    private static final String IOT_TOPIC_FIELD_NAME = "iotTopic";
    private static final String RESPONSE_TOPIC_FIELD_NAME = "responseTopic";
    private static final String QOS_FIELD_NAME = "qos";
    private static final String RETAINED_FIELD_NAME = "retained";

    @Mock
    private MqttClientFactory mockedFactory;
    @Mock
    private Dispatcher mockedDispatcher;
    @InjectMocks
    private MqttConnector testConnector;

    @Mock
    private MqttClient mockedMqttClient;
    @Mock
    private CompletableFuture<byte[]> mockedResponse;
    @Captor
    private ArgumentCaptor<Consumer<byte[]>> byteArrayConsumerCaptor;

    @Before
    public void setUp() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, mockedMqttClient);
        Whitebox.setInternalState(testConnector, IOT_TOPIC_FIELD_NAME, TEST_IOT_TOPIC);
        Whitebox.setInternalState(testConnector, RESPONSE_TOPIC_FIELD_NAME, TEST_RESPONSE_TOPIC);
        Whitebox.setInternalState(testConnector, QOS_FIELD_NAME, TEST_QOS);
        Whitebox.setInternalState(testConnector, RETAINED_FIELD_NAME, TEST_RETAINED);
    }

    @Test
    public void testLoadConfigurationAndInitWithOldClientConfigured() throws MqttException {
        MqttConnectOptions testOptions = MqttConnectOptions.builder(TEST_USERNAME, TEST_API_KEY).build();
        ConnectorConfiguration testConfiguration = new ConnectorConfiguration(TEST_BROKER_URL, TEST_CLIENT_ID,
                testOptions, TEST_IOT_TOPIC, TEST_REQUEST_TOPIC, TEST_RESPONSE_TOPIC, TEST_QOS, TEST_RETAINED);
        MqttClient newMockedClient = mock(MqttClient.class);

        when(mockedFactory.createMqttClient(anyString(), anyString())).thenReturn(newMockedClient);

        testConnector.loadConfigurationAndInit(testConfiguration);

        verify(mockedMqttClient).disconnect();
        verify(mockedFactory).createMqttClient(eq(TEST_BROKER_URL), eq(TEST_CLIENT_ID));
        verify(newMockedClient).connect(eq(testOptions));
        verify(newMockedClient).subscribe(eq(TEST_REQUEST_TOPIC), eq(testConnector));
        assertEquals(TEST_IOT_TOPIC, Whitebox.getInternalState(testConnector, IOT_TOPIC_FIELD_NAME));
        assertEquals(TEST_RESPONSE_TOPIC, Whitebox.getInternalState(testConnector, RESPONSE_TOPIC_FIELD_NAME));
        assertEquals(TEST_QOS, Whitebox.getInternalState(testConnector, QOS_FIELD_NAME));
        assertEquals(TEST_RETAINED, Whitebox.getInternalState(testConnector, RETAINED_FIELD_NAME));
    }

    @Test
    public void testLoadConfigurationAndInitWithNoClientConfigured() throws MqttException {
        MqttConnectOptions testOptions = MqttConnectOptions.builder(TEST_USERNAME, TEST_API_KEY).build();
        ConnectorConfiguration testConfiguration = new ConnectorConfiguration(TEST_BROKER_URL, TEST_CLIENT_ID,
                testOptions, TEST_IOT_TOPIC, TEST_REQUEST_TOPIC, TEST_RESPONSE_TOPIC, TEST_QOS, TEST_RETAINED);
        MqttClient newMockedClient = mock(MqttClient.class);

        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, null);

        when(mockedFactory.createMqttClient(anyString(), anyString())).thenReturn(newMockedClient);

        testConnector.loadConfigurationAndInit(testConfiguration);

        verify(newMockedClient).connect(eq(testOptions));
        verify(newMockedClient).subscribe(eq(TEST_REQUEST_TOPIC), eq(testConnector));
        assertEquals(TEST_IOT_TOPIC, Whitebox.getInternalState(testConnector, IOT_TOPIC_FIELD_NAME));
        assertEquals(TEST_RESPONSE_TOPIC, Whitebox.getInternalState(testConnector, RESPONSE_TOPIC_FIELD_NAME));
        assertEquals(TEST_QOS, Whitebox.getInternalState(testConnector, QOS_FIELD_NAME));
        assertEquals(TEST_RETAINED, Whitebox.getInternalState(testConnector, RETAINED_FIELD_NAME));
    }

    @Test
    public void testMessageArrived() throws MqttException {
        byte[] testResponseBytes = new byte[] { 5, 6, 7, 8 };
        MqttMessage responseMessage = MqttMessage.newInstance(testResponseBytes, TEST_QOS, TEST_RETAINED);

        when(mockedDispatcher.process(any(byte[].class))).thenReturn(mockedResponse);

        testConnector.messageArrived(TEST_TOPIC, TEST_MESSAGE);

        verify(mockedDispatcher).process(aryEq(TEST_PAYLOAD));
        verify(mockedResponse).thenAccept(byteArrayConsumerCaptor.capture());
        byteArrayConsumerCaptor.getValue().accept(testResponseBytes);
        verify(mockedMqttClient).publish(eq(TEST_RESPONSE_TOPIC), eq(responseMessage));
    }

    @Test
    public void testMessageArrivedNullResponse() {
        when(mockedDispatcher.process(any(byte[].class))).thenReturn(null);

        testConnector.messageArrived(TEST_TOPIC, TEST_MESSAGE);

        verify(mockedDispatcher).process(aryEq(TEST_PAYLOAD));
        verifyZeroInteractions(mockedMqttClient);
    }

    @Test
    public void testUplink() throws MqttException {
        byte[] payload = new byte[]{1, 2, 3, 4};
        MqttMessage expectedMessage = MqttMessage.newInstance(payload, TEST_QOS, TEST_RETAINED);

        testConnector.uplink(payload);

        verify(mockedMqttClient).publish(eq(TEST_IOT_TOPIC), eq(expectedMessage));
    }

    @Test
    public void testUplinkMqttDisconnected() {
        byte[] payload = new byte[]{1, 2, 3, 4};

        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, null);

        testConnector.uplink(payload);

        verifyZeroInteractions(mockedMqttClient);
    }

    @Test
    public void testUplinkNullPayload() {
        testConnector.uplink(null);

        verifyZeroInteractions(mockedMqttClient);
    }

    @Test
    public void testUplinkMqttPublishExceptionCaught() throws MqttException {
        byte[] payload = new byte[]{1, 2, 3, 4};
        MqttMessage expectedMessage = MqttMessage.newInstance(payload, TEST_QOS, TEST_RETAINED);

        doThrow(new MqttException("")).when(mockedMqttClient).publish(anyString(), any(MqttMessage.class));

        testConnector.uplink(payload);

        verify(mockedMqttClient).publish(eq(TEST_IOT_TOPIC), eq(expectedMessage));
    }

    @Test
    public void testIsConnected() {
        when(mockedMqttClient.isConnected()).thenReturn(true);

        boolean connected = testConnector.isConnected();

        assertTrue(connected);
    }

    @Test
    public void testIsConnectedNullClient() {
        Whitebox.setInternalState(testConnector, CLIENT_FIELD_NAME, null);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testIsConnectedClientNotConnected() {
        when(mockedMqttClient.isConnected()).thenReturn(false);

        boolean connected = testConnector.isConnected();

        assertFalse(connected);
    }

    @Test
    public void testClose() throws MqttException {
        testConnector.close();

        verify(mockedMqttClient).disconnect();
    }

    @Test
    public void testCloseWithNullClient() {
        Whitebox.setInternalState(testConnector, "client", null);

        testConnector.close();
    }

    @Test
    public void testCloseExceptionIsCaught() throws MqttException {
        doThrow(new MqttException("")).when(mockedMqttClient).disconnect();

        testConnector.close();

        verify(mockedMqttClient).disconnect();
    }
}