package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.event.api.ResponseDispatcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MqttDatastreamsEventTest {

    private static final String TEST_EVENT_TOPIC = "test/event/topic/+";
    private static final String TEST_RRESPONSE_TOPIC = "test/response/topic/+";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TOPIC_SEPARATOR = "/";
    private static final long TEST_AT = System.currentTimeMillis();
    private static final String TEST_VALUE = "helloWorld!";
    private static final int TEST_VALUE_2 = 50;
    private static final byte[] TEST_PAYLOAD = new byte[] { 1, 2, 3, 4 };


    @Mock
    private EventPublisher mockedEventPublisher;
    @Mock
    private MqttClient mockedClient;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private DeviceInfoProviderProxy mockedDeviceInfoProvider;
    @Mock
    private ResponseDispatcher mockedResponseDispatcher;

    private MqttDatastreamsEvent testHandler;

    @Before
    public void setUp() throws MqttException {
        testHandler = new MqttDatastreamsEvent(mockedEventPublisher, mockedClient, mockedSerializer, TEST_EVENT_TOPIC, mockedDeviceInfoProvider, TEST_RRESPONSE_TOPIC, mockedResponseDispatcher, null);
    }

    @Test
    public void testConstructorAndRegisterToEventSource() throws MqttException {
        assertEquals(TEST_EVENT_TOPIC, Whitebox.getInternalState(testHandler, "eventTopic"));
        verify(mockedClient).subscribe(eq(TEST_EVENT_TOPIC), any(MqttMessageListener.class));
    }

    @Test
    public void testDeviceEventMessageListenerMessageArriveWithPath() throws MqttException, IOException {
        /*String testTopic = TEST_EVENT_TOPIC + TOPIC_SEPARATOR + TEST_DEVICE_ID;
        ArgumentCaptor<MqttDatastreamsEvent.EventMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsEvent.EventMessageListener.class);
        String gateway1 = "gateway1";
        String gateway1_1 = "gateway1_1";
        MqttDatastreamsEvent.InnerDatastreamEvent event1 =
                new MqttDatastreamsEvent.InnerDatastreamEvent(TEST_DATASTREAM_ID, TEST_AT, TEST_VALUE);
        MqttDatastreamsEvent.InnerDatastreamEvent event2 =
                new MqttDatastreamsEvent.InnerDatastreamEvent(TEST_DATASTREAM_ID_2, null, TEST_VALUE_2);

        Map<String, Map<String, Map<Long, Object>>> events = new HashMap<>();
        Map<String, Map<Long, Object>> eventsByFeed = new HashMap<>();

        Map<Long, Object> datastream1 = new HashMap<>();
        datastream1.put(event1.getAt(), event1.getValue());
        Map<Long, Object> datastream2 = new HashMap<>();
        datastream2.put(event2.getAt(), event2.getValue());
        eventsByFeed.put(null, datastream1);
        events.put(event1.getDatastreamId(), eventsByFeed);
        eventsByFeed.put(null, datastream2);
        events.put(event2.getDatastreamId(), eventsByFeed);
        MqttDatastreamsEvent.DeviceEventMessage testDeviceEventMessage =
                new MqttDatastreamsEvent.DeviceEventMessage(Arrays.asList(gateway1, gateway1_1),
                        Arrays.asList(event1, event2));
        String[] expectedPath = new String[] {gateway1, gateway1_1};

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testDeviceEventMessage);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DEVICE_EVENT_TOPIC), listenerCaptor.capture());
        MqttDatastreamsEvent.DeviceEventMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedSerializer)
                .deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsEvent.DeviceEventMessage.class));
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID_2));
        verify(mockedEventPublisher)
                .publishEvents(eq(TEST_DEVICE_ID), aryEq(expectedPath), eq(events));*/
    }

    @Test
    public void testDeviceEventMessageListenerMessageArriveWithoutPath() throws MqttException, IOException {
        /*String testTopic = TEST_EVENT_TOPIC + MqttDatastreams.TOPIC_LEVEL_SEPARATOR + TEST_DEVICE_ID;
        ArgumentCaptor<MqttDatastreamsEvent.DeviceEventMessageListener> listenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamsEvent.DeviceEventMessageListener.class);
        MqttDatastreamsEvent.InnerDatastreamEvent event1 =
                new MqttDatastreamsEvent.InnerDatastreamEvent(TEST_DATASTREAM_ID, TEST_AT, TEST_VALUE);
        MqttDatastreamsEvent.InnerDatastreamEvent event2 =
                new MqttDatastreamsEvent.InnerDatastreamEvent(TEST_DATASTREAM_ID_2, null, TEST_VALUE_2);
        Map<String, Map<String, Map<Long, Object>>> events = new HashMap<>();
        Map<String, Map<Long, Object>> eventsByFeed = new HashMap<>();
        Map<Long, Object> datastream1 = new HashMap<>();
        datastream1.put(event1.getAt(), event1.getValue());
        Map<Long, Object> datastream2 = new HashMap<>();
        datastream2.put(event2.getAt(), event2.getValue());
        eventsByFeed.put(null, datastream1);
        events.put(event1.getDatastreamId(), eventsByFeed);
        eventsByFeed.put(null, datastream2);
        events.put(event2.getDatastreamId(), eventsByFeed);
        MqttDatastreamsEvent.DeviceEventMessage testDeviceEventMessage =
                new MqttDatastreamsEvent.DeviceEventMessage(null, Arrays.asList(event1, event2));

        when(mockedPermissionManager.hasReadPermission(anyString(), anyString())).thenReturn(true);
        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testDeviceEventMessage);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DEVICE_EVENT_TOPIC), listenerCaptor.capture());
        MqttDatastreamsEvent.DeviceEventMessageListener testListener = listenerCaptor.getValue();

        testListener.messageArrived(testTopic, MqttMessage.newInstance(TEST_PAYLOAD));

        verify(mockedSerializer).deserialize(aryEq(TEST_PAYLOAD), eq(MqttDatastreamsEvent.DeviceEventMessage.class));
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID));
        verify(mockedPermissionManager).hasReadPermission(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAM_ID_2));

        verify(mockedEventPublisher)
                .publishEvents(eq(TEST_DEVICE_ID), eq(null), eq(events));*/
    }

}