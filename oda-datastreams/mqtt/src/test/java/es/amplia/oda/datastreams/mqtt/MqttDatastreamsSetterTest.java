package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
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

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MqttDatastreamsSetterTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_WRITE_REQUEST_OPERATION_ROOT_TOPIC = "test/operation/write/request";
    private static final String TEST_WRITE_RESPONSE_OPERATION_ROOT_TOPIC = "test/operation/write/response";
    private static final String TEST_SUBSCRIBED_WRITE_RESPONSE_OPERATION_TOPIC =
            TEST_WRITE_RESPONSE_OPERATION_ROOT_TOPIC + MqttDatastreams.ONE_TOPIC_LEVEL_WILDCARD +
                    MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
    private static final String TEST_VALUE = "helloWorld!";
    private static final int OPERATION_ID = 12345;
    private static final String DEVICES_MANAGED_FIELD_NAME = "devicesManaged";
    private static final String FUTURES_FIELD_NAME = "futures";

    @Mock
    private MqttClient mockedClient;
    @Mock
    private MqttDatastreamsPermissionManager mockedPermissionManager;
    @Mock
    private Serializer mockedSerializer;

    private MqttDatastreamsSetter testSetter;

    @Spy
    private List<String> spiedDevicesManaged = new ArrayList<>();
    @Spy
    private Map<Integer, CompletableFuture<Void>> spiedFutures = new ConcurrentHashMap<>();
    @Mock
    CompletableFuture<Void> mockedFuture;
    private static final byte[] TEST_PAYLOAD = new byte[]{1, 2, 3, 4};

    @Before
    public void setUp() throws MqttException {
        testSetter = new MqttDatastreamsSetter(TEST_DATASTREAM_ID, mockedClient, mockedPermissionManager,
                mockedSerializer, TEST_WRITE_REQUEST_OPERATION_ROOT_TOPIC, TEST_WRITE_RESPONSE_OPERATION_ROOT_TOPIC);
    }

    @Test
    public void testConstructor() throws MqttException {
        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_WRITE_RESPONSE_OPERATION_TOPIC), any(MqttMessageListener.class));
    }

    @Test
    public void testGetDatastreamIdSatisfied() {
        assertEquals(TEST_DATASTREAM_ID, testSetter.getDatastreamIdSatisfied());
    }

    @Test
    public void testGetDatastreamType() {
        assertEquals(Object.class, testSetter.getDatastreamType());
    }

    @Test
    public void testAddManagedDevice() {
        Whitebox.setInternalState(testSetter, DEVICES_MANAGED_FIELD_NAME, spiedDevicesManaged);

        testSetter.addManagedDevice(TEST_DEVICE_ID);

        verify(spiedDevicesManaged).add(eq(TEST_DEVICE_ID));
    }

    @Test
    public void testRemoveManagedDevice() {
        spiedDevicesManaged.add(TEST_DEVICE_ID);
        Whitebox.setInternalState(testSetter, DEVICES_MANAGED_FIELD_NAME, spiedDevicesManaged);

        testSetter.removeManagedDevice(TEST_DEVICE_ID);

        verify(spiedDevicesManaged).remove(eq(TEST_DEVICE_ID));
    }

    @Test
    public void testRemoveManagedDeviceThatNotExistsDoesNotThrowException() {
        Whitebox.setInternalState(testSetter, DEVICES_MANAGED_FIELD_NAME, spiedDevicesManaged);

        testSetter.removeManagedDevice(TEST_DEVICE_ID);

        verify(spiedDevicesManaged, never()).remove(anyInt());
    }

    @Test
    public void testGetDevicesIdManaged() {
        spiedDevicesManaged.add(TEST_DEVICE_ID);

        Whitebox.setInternalState(testSetter, DEVICES_MANAGED_FIELD_NAME, spiedDevicesManaged);

        List<String> devicesManaged = testSetter.getDevicesIdManaged();

        assertEquals(1, devicesManaged.size());
        assertTrue(devicesManaged.contains(TEST_DEVICE_ID));
    }

    @Test
    public void testSet() throws IOException, MqttException {
        String testTopic = TEST_WRITE_REQUEST_OPERATION_ROOT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR +
                TEST_DEVICE_ID + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        ArgumentCaptor<MqttDatastreamsSetter.WriteRequestOperation> writeRequestCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsSetter.WriteRequestOperation.class);

        Whitebox.setInternalState(testSetter, FUTURES_FIELD_NAME, spiedFutures);

        when(mockedPermissionManager.hasWritePermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.serialize(any(MqttDatastreamsSetter.WriteRequestOperation.class))).thenReturn(TEST_PAYLOAD);

        CompletableFuture<Void> future = testSetter.set(TEST_DEVICE_ID, TEST_VALUE);

        assertFalse(future.isDone());
        verify(mockedPermissionManager).hasWritePermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verify(mockedSerializer).serialize(writeRequestCaptor.capture());
        MqttDatastreamsSetter.WriteRequestOperation request = writeRequestCaptor.getValue();
        assertEquals(TEST_VALUE, request.getValue());
        verify(mockedClient).publish(eq(testTopic), eq(MqttMessage.newInstance(TEST_PAYLOAD)));
        //noinspection unchecked
        verify(spiedFutures).put(anyInt(), any(CompletableFuture.class));
    }

    @Test
    public void testSetWithoutWriteAccessPermission() {
        when(mockedPermissionManager.hasWritePermission(anyString(), anyString())).thenReturn(false);

        CompletableFuture<Void> future = testSetter.set(TEST_DEVICE_ID, TEST_VALUE);

        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testSetSerializerThrowsIOException() throws IOException {
        when(mockedPermissionManager.hasWritePermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.serialize(any(MqttDatastreamsSetter.WriteRequestOperation.class))).thenThrow(new IOException(""));

        CompletableFuture<Void> future = testSetter.set(TEST_DEVICE_ID, TEST_VALUE);

        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testSetMqttClientThrowsMqttException() throws IOException, MqttException {
        Whitebox.setInternalState(testSetter, FUTURES_FIELD_NAME, spiedFutures);

        when(mockedPermissionManager.hasWritePermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.serialize(any(MqttDatastreamsSetter.WriteRequestOperation.class))).thenReturn(TEST_PAYLOAD);
        doThrow(new MqttException("")).when(mockedClient).publish(anyString(), any(MqttMessage.class));

        CompletableFuture<Void> future = testSetter.set(TEST_DEVICE_ID, TEST_VALUE);

        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    public void testWriteResponseMessageListenerMessageArriveWithCreatedStatus() throws IOException, MqttException {
        String testTopic = TEST_WRITE_RESPONSE_OPERATION_ROOT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR +
                TEST_DEVICE_ID + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        int status = 201;
        MqttDatastreamsSetter.WriteResponseOperation testWriteResponse =
                new MqttDatastreamsSetter.WriteResponseOperation(OPERATION_ID, status, null);
        ArgumentCaptor<MqttDatastreamsSetter.WriteResponseMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsSetter.WriteResponseMessageListener.class);

        spiedFutures.put(OPERATION_ID, mockedFuture);
        Whitebox.setInternalState(testSetter, FUTURES_FIELD_NAME, spiedFutures);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testWriteResponse);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_WRITE_RESPONSE_OPERATION_TOPIC), listenerCaptor.capture());
        MqttDatastreamsSetter.WriteResponseMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedSerializer).deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsSetter.WriteResponseOperation.class));
        verify(spiedFutures).remove(eq(OPERATION_ID));
        verify(mockedFuture).complete(eq(null));
    }

    @Test
    public void testWriteResponseMessageListenerMessageArriveWithErrorStatus() throws IOException, MqttException {
        String testTopic = TEST_WRITE_RESPONSE_OPERATION_ROOT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR +
                TEST_DEVICE_ID + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        int status = 500;
        MqttDatastreamsSetter.WriteResponseOperation testWriteResponse =
                new MqttDatastreamsSetter.WriteResponseOperation(OPERATION_ID, status, null);
        ArgumentCaptor<MqttDatastreamsSetter.WriteResponseMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsSetter.WriteResponseMessageListener.class);

        spiedFutures.put(OPERATION_ID, mockedFuture);
        Whitebox.setInternalState(testSetter, FUTURES_FIELD_NAME, spiedFutures);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testWriteResponse);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_WRITE_RESPONSE_OPERATION_TOPIC), listenerCaptor.capture());
        MqttDatastreamsSetter.WriteResponseMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedSerializer).deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsSetter.WriteResponseOperation.class));
        verify(spiedFutures).remove(eq(OPERATION_ID));
        verify(mockedFuture).completeExceptionally(any(RuntimeException.class));
    }

    @Test
    public void testWriteResponseMessageListenerMessageArriveThrowsIOException() throws IOException, MqttException {
        String testTopic = TEST_WRITE_RESPONSE_OPERATION_ROOT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR +
                TEST_DEVICE_ID + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        ArgumentCaptor<MqttDatastreamsSetter.WriteResponseMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsSetter.WriteResponseMessageListener.class);

        spiedFutures.put(OPERATION_ID, mockedFuture);
        Whitebox.setInternalState(testSetter, FUTURES_FIELD_NAME, spiedFutures);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenThrow(new IOException(""));

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_WRITE_RESPONSE_OPERATION_TOPIC), listenerCaptor.capture());
        MqttDatastreamsSetter.WriteResponseMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(spiedFutures, never()).remove(anyInt());
    }

    @Test
    public void testClose() throws MqttException {
        testSetter.close();

        verify(mockedClient).unsubscribe(eq(TEST_SUBSCRIBED_WRITE_RESPONSE_OPERATION_TOPIC));
    }

    @Test
    public void testCloseCaptureMqttException() throws MqttException {
        doThrow(new MqttException("")).when(mockedClient).unsubscribe(anyString());

        testSetter.close();

        verify(mockedClient).unsubscribe(eq(TEST_SUBSCRIBED_WRITE_RESPONSE_OPERATION_TOPIC));
    }
}