package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.Serializer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static es.amplia.oda.datastreams.mqtt.MqttDatastreams.*;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MqttDatastreamsGetterTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_READ_REQUEST_OPERATION_ROOT_TOPIC = "test/operation/read/request";
    private static final String TEST_READ_RESPONSE_OPERATION_ROOT_TOPIC = "test/operation/read/response";
    private static final String TEST_SUBSCRIBED_READ_RESPONSE_OPERATION_TOPIC =
            TEST_READ_RESPONSE_OPERATION_ROOT_TOPIC + ONE_TOPIC_LEVEL_WILDCARD + TOPIC_LEVEL_SEPARATOR +
                    TEST_DATASTREAM_ID;
    private static final String DEVICES_MANAGED_FIELD_NAME = "devicesManaged";
    private static final String FUTURES_FIELD_NAME = "futures";

    @Mock
    private MqttClient mockedClient;
    @Mock
    private MqttDatastreamsPermissionManager mockedPermissionManager;
    @Mock
    private Serializer mockedSerializer;

    private MqttDatastreamsGetter testGetter;

    @Spy
    private List<String> spiedDevicesManaged = new ArrayList<>();
    @Spy
    private Map<Integer, CompletableFuture<DatastreamsGetter.CollectedValue>> spiedFutures = new ConcurrentHashMap<>();
    @Mock
    CompletableFuture<DatastreamsGetter.CollectedValue> mockedFuture;

    @Before
    public void setUp() throws MqttException {
        testGetter = new MqttDatastreamsGetter(TEST_DATASTREAM_ID, mockedClient, mockedPermissionManager,
                mockedSerializer, TEST_READ_REQUEST_OPERATION_ROOT_TOPIC, TEST_READ_RESPONSE_OPERATION_ROOT_TOPIC);
    }

    @Test
    public void testConstructor() throws MqttException {
        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_READ_RESPONSE_OPERATION_TOPIC), any(MqttMessageListener.class));
    }

    @Test
    public void testGetDatastreamIdSatisfied() {
        assertEquals(TEST_DATASTREAM_ID, testGetter.getDatastreamIdSatisfied());
    }

    @Test
    public void testAddManagedDevice() {
        Whitebox.setInternalState(testGetter, DEVICES_MANAGED_FIELD_NAME, spiedDevicesManaged);

        testGetter.addManagedDevice(TEST_DEVICE_ID);

        verify(spiedDevicesManaged).add(eq(TEST_DEVICE_ID));
    }

    @Test
    public void testRemoveManagedDevice() {
        spiedDevicesManaged.add(TEST_DEVICE_ID);
        Whitebox.setInternalState(testGetter, DEVICES_MANAGED_FIELD_NAME, spiedDevicesManaged);

        testGetter.removeManagedDevice(TEST_DEVICE_ID);

        verify(spiedDevicesManaged).remove(eq(TEST_DEVICE_ID));
    }

    @Test
    public void testRemoveManagedDeviceThatNotExistsDoesNotThrowException() {
        Whitebox.setInternalState(testGetter, DEVICES_MANAGED_FIELD_NAME, spiedDevicesManaged);

        testGetter.removeManagedDevice(TEST_DEVICE_ID);

        verify(spiedDevicesManaged, never()).remove(anyInt());
    }

    @Test
    public void testGetDevicesIdManaged() {
        spiedDevicesManaged.add(TEST_DEVICE_ID);

        Whitebox.setInternalState(testGetter, DEVICES_MANAGED_FIELD_NAME, spiedDevicesManaged);

        List<String> devicesManaged = testGetter.getDevicesIdManaged();

        assertEquals(1, devicesManaged.size());
        assertTrue(devicesManaged.contains(TEST_DEVICE_ID));
    }

    @Test
    public void testGet() throws IOException, MqttException {
        byte[] testPayload = new byte[] {1,2,3,4};
        String testTopic = TEST_READ_REQUEST_OPERATION_ROOT_TOPIC + TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID +
                TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;

        Whitebox.setInternalState(testGetter, FUTURES_FIELD_NAME, spiedFutures);

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.serialize(any(MqttDatastreamsGetter.ReadRequest.class))).thenReturn(testPayload);

        CompletableFuture<DatastreamsGetter.CollectedValue> future = testGetter.get(TEST_DEVICE_ID);

        assertFalse(future.isDone());
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verify(mockedSerializer).serialize(any(MqttDatastreamsGetter.ReadRequest.class));
        verify(mockedClient).publish(eq(testTopic), eq(MqttMessage.newInstance(testPayload)));
        //noinspection unchecked
        verify(spiedFutures).put(anyInt(), any(CompletableFuture.class));
    }

    @Test
    public void testGetOfDatastreamWithoutReadAccessPermission() {
        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(false);

        CompletableFuture<DatastreamsGetter.CollectedValue> future = testGetter.get(TEST_DEVICE_ID);

        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testGetSerializerThrowsIOException() throws IOException {
        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.serialize(any(MqttDatastreamsGetter.ReadRequest.class))).thenThrow(new IOException());

        CompletableFuture<DatastreamsGetter.CollectedValue> future = testGetter.get(TEST_DEVICE_ID);

        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testGetMqttClientThrowsMqttException() throws IOException, MqttException {
        byte[] testPayload = new byte[] {1,2,3,4};

        Whitebox.setInternalState(testGetter, FUTURES_FIELD_NAME, spiedFutures);

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.serialize(any(MqttDatastreamsGetter.ReadRequest.class))).thenReturn(testPayload);
        doThrow(new MqttException("")).when(mockedClient).publish(anyString(), any(MqttMessage.class));

        CompletableFuture<DatastreamsGetter.CollectedValue> future = testGetter.get(TEST_DEVICE_ID);

        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testReadResponseMessageListenerMessageArriveWithOKStatus() throws IOException, MqttException {
        String testTopic = TEST_READ_RESPONSE_OPERATION_ROOT_TOPIC + TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID +
                TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        byte[] testPayload = new byte[] {1,2,3,4};
        int operationId = 12345;
        int status = 200;
        long at = 123456789;
        String value = "helloWorld!";
        MqttDatastreamsGetter.ReadResponse testReadResponse =
                new MqttDatastreamsGetter.ReadResponse(operationId, status, null, at, value);
        ArgumentCaptor<MqttDatastreamsGetter.ReadResponseMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsGetter.ReadResponseMessageListener.class);

        spiedFutures.put(operationId, mockedFuture);
        Whitebox.setInternalState(testGetter, FUTURES_FIELD_NAME, spiedFutures);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testReadResponse);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_READ_RESPONSE_OPERATION_TOPIC), listenerCaptor.capture());
        MqttDatastreamsGetter.ReadResponseMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(testPayload));

        verify(mockedSerializer).deserialize(aryEq(testPayload), eq(MqttDatastreamsGetter.ReadResponse.class));
        verify(spiedFutures).remove(eq(operationId));
        verify(mockedFuture).complete(eq(new DatastreamsGetter.CollectedValue(at, value)));
    }

    @Test
    public void testReadResponseMessageListenerMessageArriveWithErrorStatus() throws IOException, MqttException {
        String testTopic = TEST_READ_RESPONSE_OPERATION_ROOT_TOPIC + TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID +
                TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        byte[] testPayload = new byte[] {1,2,3,4};
        int operationId = 12345;
        int status = 400;
        long at = 123456789;
        String value = "helloWorld!";
        MqttDatastreamsGetter.ReadResponse testReadResponse =
                new MqttDatastreamsGetter.ReadResponse(operationId, status, null, at, value);
        ArgumentCaptor<MqttDatastreamsGetter.ReadResponseMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsGetter.ReadResponseMessageListener.class);

        spiedFutures.put(operationId, mockedFuture);
        Whitebox.setInternalState(testGetter, FUTURES_FIELD_NAME, spiedFutures);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testReadResponse);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_READ_RESPONSE_OPERATION_TOPIC), listenerCaptor.capture());
        MqttDatastreamsGetter.ReadResponseMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(testPayload));

        verify(mockedSerializer).deserialize(aryEq(testPayload), eq(MqttDatastreamsGetter.ReadResponse.class));
        verify(spiedFutures).remove(eq(operationId));
        verify(mockedFuture).completeExceptionally(any(RuntimeException.class));
    }

    @Test
    public void testReadResponseMessageListenerMessageArriveThrowsIOException() throws IOException, MqttException {
        String testTopic = TEST_READ_RESPONSE_OPERATION_ROOT_TOPIC + TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID +
                TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        byte[] testPayload = new byte[] {1,2,3,4};
        ArgumentCaptor<MqttDatastreamsGetter.ReadResponseMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsGetter.ReadResponseMessageListener.class);

        Whitebox.setInternalState(testGetter, FUTURES_FIELD_NAME, spiedFutures);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenThrow(new IOException());

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_READ_RESPONSE_OPERATION_TOPIC), listenerCaptor.capture());
        MqttDatastreamsGetter.ReadResponseMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(testPayload));

        verify(spiedFutures, never()).remove(anyInt());
    }

    @Test
    public void testClose() throws MqttException {
        testGetter.close();

        verify(mockedClient).unsubscribe(eq(TEST_SUBSCRIBED_READ_RESPONSE_OPERATION_TOPIC));
    }

    @Test
    public void testCloseCaptureMqttException() throws MqttException {
        doThrow(new MqttException("")).when(mockedClient).unsubscribe(anyString());

        testGetter.close();

        verify(mockedClient).unsubscribe(eq(TEST_SUBSCRIBED_READ_RESPONSE_OPERATION_TOPIC));
    }
}