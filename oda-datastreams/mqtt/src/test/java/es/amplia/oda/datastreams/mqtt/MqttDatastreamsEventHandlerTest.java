package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MqttDatastreamsEventHandlerTest {

    private static final String TEST_EVENT_TOPIC = "test/event/topic";
    private static final String TEST_SUBSCRIBED_DEVICE_EVENT_TOPIC = TEST_EVENT_TOPIC +
            MqttDatastreams.ONE_TOPIC_LEVEL_WILDCARD;
    private static final String TEST_SUBSCRIBED_DATASTREAM_EVENT_TOPIC = TEST_EVENT_TOPIC +
            MqttDatastreams.TWO_TOPIC_LEVELS_WILDCARD;
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final long TEST_AT = System.currentTimeMillis();
    private static final String TEST_VALUE = "helloWorld!";
    private static final int TEST_VALUE_2 = 50;
    private static final byte[] TEST_PAYLOAD = new byte[] { 1, 2, 3, 4 };

    @Mock
    private MqttClient mockedClient;
    @Mock
    private MqttDatastreamsPermissionManager mockedPermissionManager;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private EventDispatcher mockedEventDispatcher;

    private MqttDatastreamsEventHandler testHandler;

    @Before
    public void setUp() throws MqttException {
        testHandler = new MqttDatastreamsEventHandler(mockedClient, mockedPermissionManager, mockedSerializer,
                mockedEventDispatcher, TEST_EVENT_TOPIC);
    }

    @Test
    public void testConstructor() throws MqttException {
        assertEquals(TEST_SUBSCRIBED_DEVICE_EVENT_TOPIC, Whitebox.getInternalState(testHandler, "deviceEventTopic"));
        assertEquals(TEST_SUBSCRIBED_DATASTREAM_EVENT_TOPIC,
                Whitebox.getInternalState(testHandler, "datastreamEventTopic"));
        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DEVICE_EVENT_TOPIC), any(MqttMessageListener.class));
        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DATASTREAM_EVENT_TOPIC), any(MqttMessageListener.class));
    }

    @Test
    public void testDeviceEventMessageListenerMessageArriveWithPath() throws MqttException, IOException {
        String testTopic = TEST_EVENT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID;
        ArgumentCaptor<MqttDatastreamsEventHandler.DeviceEventMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsEventHandler.DeviceEventMessageListener.class);
        String gateway1 = "gateway1";
        String gateway1_1 = "gateway1_1";
        MqttDatastreamsEventHandler.InnerDatastreamEvent event1 =
                new MqttDatastreamsEventHandler.InnerDatastreamEvent(TEST_DATASTREAM_ID, TEST_AT, TEST_VALUE);
        MqttDatastreamsEventHandler.InnerDatastreamEvent event2 =
                new MqttDatastreamsEventHandler.InnerDatastreamEvent(TEST_DATASTREAM_ID_2, null, TEST_VALUE_2);
        MqttDatastreamsEventHandler.DeviceEventMessage testDeviceEventMessage =
                new MqttDatastreamsEventHandler.DeviceEventMessage(Arrays.asList(gateway1, gateway1_1),
                        Arrays.asList(event1, event2));
        String[] expectedPath = new String[] {gateway1, gateway1_1};

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testDeviceEventMessage);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DEVICE_EVENT_TOPIC), listenerCaptor.capture());
        MqttDatastreamsEventHandler.DeviceEventMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedSerializer)
                .deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsEventHandler.DeviceEventMessage.class));
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID_2));
        verify(mockedEventDispatcher)
                .publish(eq(new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, expectedPath, TEST_AT, TEST_VALUE)));
        verify(mockedEventDispatcher)
                .publish(eq(new Event(TEST_DATASTREAM_ID_2, TEST_DEVICE_ID, expectedPath, null, TEST_VALUE_2)));
    }

    @Test
    public void testDeviceEventMessageListenerMessageArriveWithoutPath() throws MqttException, IOException {
        String testTopic = TEST_EVENT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID;
        ArgumentCaptor<MqttDatastreamsEventHandler.DeviceEventMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsEventHandler.DeviceEventMessageListener.class);
        MqttDatastreamsEventHandler.InnerDatastreamEvent event1 =
                new MqttDatastreamsEventHandler.InnerDatastreamEvent(TEST_DATASTREAM_ID, TEST_AT, TEST_VALUE);
        MqttDatastreamsEventHandler.InnerDatastreamEvent event2 =
                new MqttDatastreamsEventHandler.InnerDatastreamEvent(TEST_DATASTREAM_ID_2, null, TEST_VALUE_2);
        MqttDatastreamsEventHandler.DeviceEventMessage testDeviceEventMessage =
                new MqttDatastreamsEventHandler.DeviceEventMessage(null, Arrays.asList(event1, event2));

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testDeviceEventMessage);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DEVICE_EVENT_TOPIC), listenerCaptor.capture());
        MqttDatastreamsEventHandler.DeviceEventMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedSerializer).deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsEventHandler.DeviceEventMessage.class));
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID_2));
        verify(mockedEventDispatcher).publish(eq(new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, TEST_AT, TEST_VALUE)));
        verify(mockedEventDispatcher).publish(eq(new Event(TEST_DATASTREAM_ID_2, TEST_DEVICE_ID, null, null, TEST_VALUE_2)));
    }

    @Test
    public void testDeviceEventMessageListenerMessageArriveDatastreamWithoutReadAccessPermission()
            throws MqttException, IOException {
        String testTopic = TEST_EVENT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID;
        ArgumentCaptor<MqttDatastreamsEventHandler.DeviceEventMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsEventHandler.DeviceEventMessageListener.class);
        MqttDatastreamsEventHandler.InnerDatastreamEvent event1 =
                new MqttDatastreamsEventHandler.InnerDatastreamEvent(TEST_DATASTREAM_ID, TEST_AT, TEST_VALUE);
        MqttDatastreamsEventHandler.InnerDatastreamEvent event2 =
                new MqttDatastreamsEventHandler.InnerDatastreamEvent(TEST_DATASTREAM_ID_2, null, TEST_VALUE_2);
        MqttDatastreamsEventHandler.DeviceEventMessage testDeviceEventMessage =
                new MqttDatastreamsEventHandler.DeviceEventMessage(null, Arrays.asList(event1, event2));

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(false);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testDeviceEventMessage);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DEVICE_EVENT_TOPIC), listenerCaptor.capture());
        MqttDatastreamsEventHandler.DeviceEventMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedSerializer).deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsEventHandler.DeviceEventMessage.class));
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID_2));
        verifyZeroInteractions(mockedEventDispatcher);
    }

    @Test
    public void testDeviceEventMessageListenerMessageArriveSerializerThrowsIOException()
            throws MqttException, IOException {
        String testTopic = TEST_EVENT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID;
        ArgumentCaptor<MqttDatastreamsEventHandler.DeviceEventMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsEventHandler.DeviceEventMessageListener.class);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenThrow(new IOException());

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DEVICE_EVENT_TOPIC), listenerCaptor.capture());
        MqttDatastreamsEventHandler.DeviceEventMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedSerializer).deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsEventHandler.DeviceEventMessage.class));
        verifyZeroInteractions(mockedEventDispatcher);
    }

    @Test
    public void testDatastreamEventMessageListenerMessageArrive() throws MqttException, IOException {
        String testTopic = TEST_EVENT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID +
                MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        ArgumentCaptor<MqttDatastreamsEventHandler.DatastreamEventMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsEventHandler.DatastreamEventMessageListener.class);
        String gateway1 = "gateway1";
        String gateway1_1 = "gateway1_1";
        MqttDatastreamsEventHandler.DatastreamEvent testDatastreamEvent =
                new MqttDatastreamsEventHandler.DatastreamEvent(Arrays.asList(gateway1, gateway1_1), TEST_AT, TEST_VALUE);
        String[] expectedPath = new String[] { gateway1, gateway1_1 };

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testDatastreamEvent);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DATASTREAM_EVENT_TOPIC), listenerCaptor.capture());
        MqttDatastreamsEventHandler.DatastreamEventMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verify(mockedSerializer).deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsEventHandler.DatastreamEvent.class));
        verify(mockedEventDispatcher)
                .publish(eq(new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, expectedPath, TEST_AT, TEST_VALUE)));
    }

    @Test
    public void testDatastreamEventMessageListenerMessageArriveWithoutPath() throws MqttException, IOException {
        String testTopic = TEST_EVENT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID +
                MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        ArgumentCaptor<MqttDatastreamsEventHandler.DatastreamEventMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsEventHandler.DatastreamEventMessageListener.class);
        MqttDatastreamsEventHandler.DatastreamEvent testDatastreamEvent =
                new MqttDatastreamsEventHandler.DatastreamEvent(null, TEST_AT, TEST_VALUE);

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testDatastreamEvent);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DATASTREAM_EVENT_TOPIC), listenerCaptor.capture());
        MqttDatastreamsEventHandler.DatastreamEventMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verify(mockedSerializer).deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsEventHandler.DatastreamEvent.class));
        verify(mockedEventDispatcher).publish(eq(new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, TEST_AT, TEST_VALUE)));
    }

    @Test
    public void testDatastreamEventMessageListenerMessageArriveDatastreamWithoutReadAccessPermission()
            throws MqttException, IOException {
        String testTopic = TEST_EVENT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID +
                MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        ArgumentCaptor<MqttDatastreamsEventHandler.DatastreamEventMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsEventHandler.DatastreamEventMessageListener.class);
        MqttDatastreamsEventHandler.DatastreamEvent testDatastreamEvent =
                new MqttDatastreamsEventHandler.DatastreamEvent(null, TEST_AT, TEST_VALUE);

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(false);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testDatastreamEvent);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DATASTREAM_EVENT_TOPIC), listenerCaptor.capture());
        MqttDatastreamsEventHandler.DatastreamEventMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verifyZeroInteractions(mockedSerializer);
        verifyZeroInteractions(mockedEventDispatcher);
    }

    @Test
    public void testDatastreamEventMessageListenerMessageArriveSerializerThrowsIOException() throws MqttException,
            IOException {
        String testTopic = TEST_EVENT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID +
                MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DATASTREAM_ID;
        ArgumentCaptor<MqttDatastreamsEventHandler.DatastreamEventMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsEventHandler.DatastreamEventMessageListener.class);

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenThrow(new IOException());

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DATASTREAM_EVENT_TOPIC), listenerCaptor.capture());
        MqttDatastreamsEventHandler.DatastreamEventMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verify(mockedSerializer).deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsEventHandler.DatastreamEvent.class));
        verifyZeroInteractions(mockedEventDispatcher);
    }

    @Test
    public void testClose() throws MqttException {
        testHandler.close();

        verify(mockedClient).unsubscribe(eq(TEST_SUBSCRIBED_DEVICE_EVENT_TOPIC));
        verify(mockedClient).unsubscribe(eq(TEST_SUBSCRIBED_DATASTREAM_EVENT_TOPIC));
    }

    @Test
    public void testCloseCaptureMqttException() throws MqttException {
        doThrow(new MqttException("")).when(mockedClient).unsubscribe(anyString());

        testHandler.close();

        verify(mockedClient).unsubscribe(anyString());
    }
}