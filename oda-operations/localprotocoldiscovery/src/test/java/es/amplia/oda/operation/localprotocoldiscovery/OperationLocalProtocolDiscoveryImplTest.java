package es.amplia.oda.operation.localprotocoldiscovery;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.Serializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.operation.api.OperationDiscover.*;
import static es.amplia.oda.operation.localprotocoldiscovery.OperationLocalProtocolDiscoveryImpl.EMPTY_MESSAGE;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OperationLocalProtocolDiscoveryImplTest {

    private static final String TEST_SERVER_URI = "http://test.server:1883";
    private static final String TEST_CLIENT_ID = "testClientId";
    private static final String TEST_TOPIC = "test/topic";
    private static final byte[] TEST_BYTE_STREAM = new byte[] { 1, 2, 3, 4 };
    private static final String MQTT_CLIENT_FIELD_NAME = "mqttClient";


    @Mock
    private MqttClientFactory mockedMqttClientFactory;
    @Mock
    private Serializer mockedSerializer;
    @InjectMocks
    private OperationLocalProtocolDiscoveryImpl testOperationLocalProtocolDiscovery;

    @Mock
    private MqttClient mockedClient;

    @Test
    public void testDiscover() throws ExecutionException, InterruptedException, IOException {
        Whitebox.setInternalState(testOperationLocalProtocolDiscovery, MQTT_CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testOperationLocalProtocolDiscovery, "topic", TEST_TOPIC);

        when(mockedSerializer.serialize(anyString())).thenReturn(TEST_BYTE_STREAM);

        CompletableFuture<Result> future = testOperationLocalProtocolDiscovery.discover();
        Result result = future.get();

        assertEquals(ResultCode.SUCCESSFUL, result.getResultCode());
        verify(mockedSerializer).serialize(eq(EMPTY_MESSAGE));
        verify(mockedClient).publish(eq(TEST_TOPIC), eq(MqttMessage.newInstance(TEST_BYTE_STREAM)), eq(ContentType.CBOR));
    }

    @Test
    public void testDiscoverSerializerThrowsAnException() throws ExecutionException, InterruptedException, IOException {
        String errorDescription = "This is the error description";

        when(mockedSerializer.serialize(anyString())).thenThrow(new IOException(errorDescription));

        CompletableFuture<Result> future = testOperationLocalProtocolDiscovery.discover();
        Result result = future.get();

        assertEquals(ResultCode.ERROR_PROCESSING, result.getResultCode());
        assertEquals(errorDescription, result.getResultDescription());
    }

    @Test
    public void testDiscoverMqttClientThrowsMqttException() throws ExecutionException, InterruptedException, IOException {
        String errorDescription = "This is the error description";

        Whitebox.setInternalState(testOperationLocalProtocolDiscovery, MQTT_CLIENT_FIELD_NAME, mockedClient);
        Whitebox.setInternalState(testOperationLocalProtocolDiscovery, "topic", TEST_TOPIC);

        when(mockedSerializer.serialize(anyString())).thenReturn(TEST_BYTE_STREAM);
        doThrow(new MqttException(errorDescription)).when(mockedClient)
                .publish(anyString(), any(MqttMessage.class), any(ContentType.class));

        CompletableFuture<Result> future = testOperationLocalProtocolDiscovery.discover();
        Result result = future.get();

        assertEquals(ResultCode.ERROR_PROCESSING, result.getResultCode());
        assertEquals(errorDescription, result.getResultDescription());
    }

    @Test
    public void testLoadConfiguration() {
        when(mockedMqttClientFactory.createMqttClient(anyString(), anyString())).thenReturn(mockedClient);

        testOperationLocalProtocolDiscovery.loadConfiguration(TEST_SERVER_URI, TEST_CLIENT_ID, TEST_TOPIC);

        assertEquals(TEST_TOPIC, Whitebox.getInternalState(testOperationLocalProtocolDiscovery, "topic"));
        assertEquals(mockedClient,
                Whitebox.getInternalState(testOperationLocalProtocolDiscovery, MQTT_CLIENT_FIELD_NAME));
        verify(mockedMqttClientFactory).createMqttClient(eq(TEST_SERVER_URI), eq(TEST_CLIENT_ID));
    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testOperationLocalProtocolDiscovery, MQTT_CLIENT_FIELD_NAME, mockedClient);

        testOperationLocalProtocolDiscovery.close();

        verify(mockedClient).disconnect();
    }

    @Test
    public void testCloseNoMqttClient() {
        testOperationLocalProtocolDiscovery.close();

        assertTrue("No exception is thrown", true);
    }

    @Test
    public void testCloseCatchesMqttExceptionWhenDisconnecting() {
        Whitebox.setInternalState(testOperationLocalProtocolDiscovery, MQTT_CLIENT_FIELD_NAME, mockedClient);

        doThrow(new MqttException("")).when(mockedClient).disconnect();

        testOperationLocalProtocolDiscovery.close();

        assertTrue("Exception is caught", true);
        verify(mockedClient).disconnect();
    }
}