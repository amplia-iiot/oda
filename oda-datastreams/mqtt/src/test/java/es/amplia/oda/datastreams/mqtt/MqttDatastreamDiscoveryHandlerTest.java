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
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static es.amplia.oda.datastreams.mqtt.MqttDatastreams.*;
import static es.amplia.oda.datastreams.mqtt.MqttDatastreamDiscoveryHandler.*;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MqttDatastreamDiscoveryHandlerTest {

    private static final String TEST_ENABLE_DATASTREAM_TOPIC = "test/enable";
    private static final String TEST_SUBSCRIBED_ENABLE_DEVICE_TOPIC =
            TEST_ENABLE_DATASTREAM_TOPIC + ONE_TOPIC_LEVEL_WILDCARD;
    private static final String TEST_SUBSCRIBED_ENABLE_DATASTREAM_TOPIC =
            TEST_ENABLE_DATASTREAM_TOPIC + TWO_TOPIC_LEVELS_WILDCARD;
    private static final String TEST_DISABLE_DATASTREAM_TOPIC = "test/disable";
    private static final String TEST_SUBSCRIBED_DISABLE_DEVICE_TOPIC =
            TEST_DISABLE_DATASTREAM_TOPIC + ONE_TOPIC_LEVEL_WILDCARD;
    private static final String TEST_SUBSCRIBED_DISABLE_DATASTREAM_TOPIC =
            TEST_DISABLE_DATASTREAM_TOPIC + TWO_TOPIC_LEVELS_WILDCARD;
    private static final String DATASTREAM_ID_1 = "testDatastream1";
    private static final String DEVICE_ID_1 = "testDevice1";
    private static final String DATASTREAM_ID_2 = "testDatastream2";
    private static final String DEVICE_ID_2 = "testDevice2";
    private static final MqttDatastreamPermission PERMISSION_1 = MqttDatastreamPermission.RD;
    private static final MqttDatastreamPermission PERMISSION_2 = MqttDatastreamPermission.WR;
    private static final MqttDatastreamPermission PERMISSION_3 = MqttDatastreamPermission.RW;

    @Mock
    private MqttClient mockedClient;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private MqttDatastreamsManager mockedDatastreamsManager;
    @Mock
    private MqttDatastreamsPermissionManager mockedPermissionManager;

    private MqttDatastreamDiscoveryHandler testHandler;

    @Mock
    private ExecutorService mockedExecutor;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;


    @Before
    public void setUp() throws MqttException {
        testHandler = new MqttDatastreamDiscoveryHandler(mockedClient, mockedSerializer, mockedDatastreamsManager,
                mockedPermissionManager, TEST_ENABLE_DATASTREAM_TOPIC, TEST_DISABLE_DATASTREAM_TOPIC);

        Whitebox.setInternalState(testHandler, "executor", mockedExecutor);
    }

    @Test
    public void testConstructor() throws MqttException {
        assertEquals(TEST_SUBSCRIBED_ENABLE_DEVICE_TOPIC,
                Whitebox.getInternalState(testHandler, "enableDeviceTopic"));
        assertEquals(TEST_SUBSCRIBED_ENABLE_DATASTREAM_TOPIC,
                Whitebox.getInternalState(testHandler, "enableDatastreamTopic"));
        assertEquals(TEST_SUBSCRIBED_DISABLE_DEVICE_TOPIC,
                Whitebox.getInternalState(testHandler, "disableDeviceTopic"));
        assertEquals(TEST_SUBSCRIBED_DISABLE_DATASTREAM_TOPIC,
                Whitebox.getInternalState(testHandler, "disableDatastreamTopic"));
        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_ENABLE_DEVICE_TOPIC), any(MqttMessageListener.class));
        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_ENABLE_DATASTREAM_TOPIC), any(MqttMessageListener.class));
        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DISABLE_DEVICE_TOPIC), any(MqttMessageListener.class));
        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DISABLE_DATASTREAM_TOPIC), any(MqttMessageListener.class));
    }

    @Test
    public void testInit() throws MqttException {
        List<DatastreamInfoWithPermission> initialConf =
                Arrays.asList(
                        new DatastreamInfoWithPermission(DEVICE_ID_1, DATASTREAM_ID_1, PERMISSION_1),
                        new DatastreamInfoWithPermission(DEVICE_ID_1, DATASTREAM_ID_2, PERMISSION_2),
                        new DatastreamInfoWithPermission(DEVICE_ID_2, DATASTREAM_ID_2, PERMISSION_3));

        testHandler.init(initialConf);

        verify(mockedPermissionManager).addPermission(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1), eq(PERMISSION_1));
        verify(mockedPermissionManager).addPermission(eq(DEVICE_ID_1), eq(DATASTREAM_ID_2), eq(PERMISSION_2));
        verify(mockedPermissionManager).addPermission(eq(DEVICE_ID_2), eq(DATASTREAM_ID_2), eq(PERMISSION_3));
        verify(mockedDatastreamsManager).createDatastream(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1));
        verify(mockedDatastreamsManager).createDatastream(eq(DEVICE_ID_1), eq(DATASTREAM_ID_2));
        verify(mockedDatastreamsManager).createDatastream(eq(DEVICE_ID_2), eq(DATASTREAM_ID_2));
    }

    @Test
    public void testEnableDeviceMessageListenerMessageArrived() throws MqttException, IOException {
        String testTopic = TEST_ENABLE_DATASTREAM_TOPIC + TOPIC_LEVEL_SEPARATOR + DEVICE_ID_1;
        byte[] testPayload = new byte[] {1,2,3,4};
        MqttMessage testMessage = MqttMessage.newInstance(testPayload);
        ArgumentCaptor<MqttDatastreamDiscoveryHandler.EnableDeviceMessageListener> enableDeviceMessageListenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamDiscoveryHandler.EnableDeviceMessageListener.class);
        EnableDeviceMessage testEnableDeviceMessage = new EnableDeviceMessage(Arrays.asList(
                new EnabledDatastream(DATASTREAM_ID_1, PERMISSION_1),
                new EnabledDatastream(DATASTREAM_ID_2, PERMISSION_2)));

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testEnableDeviceMessage);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_ENABLE_DEVICE_TOPIC),
                enableDeviceMessageListenerCaptor.capture());
        MqttDatastreamDiscoveryHandler.EnableDeviceMessageListener testCapturedListener =
                enableDeviceMessageListenerCaptor.getValue();
        testCapturedListener.messageArrived(testTopic, testMessage);

        verify(mockedSerializer)
                .deserialize(aryEq(testPayload), eq(MqttDatastreamDiscoveryHandler.EnableDeviceMessage.class));
        verify(mockedExecutor, times(2)).execute(runnableCaptor.capture());
        runnableCaptor.getAllValues().forEach(Runnable::run);
        verify(mockedPermissionManager).addPermission(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1), eq(PERMISSION_1));
        verify(mockedDatastreamsManager).createDatastream(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1));
        verify(mockedDatastreamsManager).createDatastream(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1));
        verify(mockedPermissionManager).addPermission(eq(DEVICE_ID_1), eq(DATASTREAM_ID_2), eq(PERMISSION_2));
        verify(mockedDatastreamsManager).createDatastream(eq(DEVICE_ID_1), eq(DATASTREAM_ID_2));
        verify(mockedDatastreamsManager).createDatastream(eq(DEVICE_ID_1), eq(DATASTREAM_ID_2));
    }

    @Test
    public void testEnableDeviceMessageListenerMessageArrivedSerializerIOExceptionIsCaught()
            throws MqttException, IOException {
        String testTopic = TEST_ENABLE_DATASTREAM_TOPIC + TOPIC_LEVEL_SEPARATOR + DEVICE_ID_1;
        byte[] testPayload = new byte[] {1,2,3,4};
        MqttMessage testMessage = MqttMessage.newInstance(testPayload);
        ArgumentCaptor<MqttDatastreamDiscoveryHandler.EnableDeviceMessageListener> enableDeviceMessageListenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamDiscoveryHandler.EnableDeviceMessageListener.class);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenThrow(new IOException());

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_ENABLE_DEVICE_TOPIC),
                enableDeviceMessageListenerCaptor.capture());
        MqttDatastreamDiscoveryHandler.EnableDeviceMessageListener testCapturedListener =
                enableDeviceMessageListenerCaptor.getValue();
        testCapturedListener.messageArrived(testTopic, testMessage);

        verify(mockedSerializer)
                .deserialize(aryEq(testPayload), eq(MqttDatastreamDiscoveryHandler.EnableDeviceMessage.class));
        verifyZeroInteractions(mockedExecutor);
    }

    @Test
    public void testEnableDeviceMessageListenerMessageArrivedIsCaughtAndMessageProcessingContinues()
            throws MqttException, IOException {
        String testTopic = TEST_ENABLE_DATASTREAM_TOPIC + TOPIC_LEVEL_SEPARATOR + DEVICE_ID_1;
        byte[] testPayload = new byte[] {1,2,3,4};
        MqttMessage testMessage = MqttMessage.newInstance(testPayload);
        ArgumentCaptor<MqttDatastreamDiscoveryHandler.EnableDeviceMessageListener> enableDeviceMessageListenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamDiscoveryHandler.EnableDeviceMessageListener.class);
        EnableDeviceMessage testEnableDeviceMessage = new EnableDeviceMessage(Arrays.asList(
                new EnabledDatastream(DATASTREAM_ID_1, PERMISSION_1),
                new EnabledDatastream(DATASTREAM_ID_2, PERMISSION_2)));

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testEnableDeviceMessage);
        doThrow(new MqttException("")).when(mockedDatastreamsManager).createDatastream(anyString(), anyString());

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_ENABLE_DEVICE_TOPIC),
                enableDeviceMessageListenerCaptor.capture());
        MqttDatastreamDiscoveryHandler.EnableDeviceMessageListener testCapturedListener =
                enableDeviceMessageListenerCaptor.getValue();
        testCapturedListener.messageArrived(testTopic, testMessage);

        verify(mockedSerializer)
                .deserialize(aryEq(testPayload), eq(MqttDatastreamDiscoveryHandler.EnableDeviceMessage.class));
        verify(mockedExecutor, times(2)).execute(runnableCaptor.capture());
        runnableCaptor.getAllValues().forEach(Runnable::run);
        verify(mockedPermissionManager).addPermission(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1), eq(PERMISSION_1));
        verify(mockedDatastreamsManager).createDatastream(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1));
        verify(mockedPermissionManager).addPermission(eq(DEVICE_ID_1), eq(DATASTREAM_ID_2), eq(PERMISSION_2));
        verify(mockedDatastreamsManager).createDatastream(eq(DEVICE_ID_1), eq(DATASTREAM_ID_2));
    }

    @Test
    public void testEnableDatastreamMessageListenerMessageArrived() throws MqttException, IOException {
        String testTopic = TEST_ENABLE_DATASTREAM_TOPIC + TOPIC_LEVEL_SEPARATOR + DEVICE_ID_1 + TOPIC_LEVEL_SEPARATOR +
                DATASTREAM_ID_1;
        byte[] testPayload = new byte[] {1,2,3,4};
        MqttMessage testMessage = MqttMessage.newInstance(testPayload);
        ArgumentCaptor<MqttDatastreamDiscoveryHandler.EnableDatastreamMessageListener> enableDatastreamMessageListenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamDiscoveryHandler.EnableDatastreamMessageListener.class);
        MqttDatastreamPermission testMode = MqttDatastreamPermission.RD;
        MqttDatastreamDiscoveryHandler.EnableDatastreamMessage testEnableDatastreamMessage =
                new MqttDatastreamDiscoveryHandler.EnableDatastreamMessage(testMode);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testEnableDatastreamMessage);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_ENABLE_DATASTREAM_TOPIC),
                enableDatastreamMessageListenerCaptor.capture());
        MqttDatastreamDiscoveryHandler.EnableDatastreamMessageListener testCapturedListener =
                enableDatastreamMessageListenerCaptor.getValue();
        testCapturedListener.messageArrived(testTopic, testMessage);

        verify(mockedSerializer).deserialize(aryEq(testPayload), eq(MqttDatastreamDiscoveryHandler.EnableDatastreamMessage.class));
        verify(mockedExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getAllValues().forEach(Runnable::run);
        verify(mockedPermissionManager).addPermission(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1), eq(testMode));
        verify(mockedDatastreamsManager).createDatastream(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1));
    }

    @Test
    public void testEnableDatastreamMessageListenerMessageArrivedSerializerThrowsIOException() throws MqttException,
            IOException {
        String testTopic = TEST_ENABLE_DATASTREAM_TOPIC + TOPIC_LEVEL_SEPARATOR + DEVICE_ID_1 + TOPIC_LEVEL_SEPARATOR +
                DATASTREAM_ID_1;
        byte[] testPayload = new byte[] {1,2,3,4};
        MqttMessage testMessage = MqttMessage.newInstance(testPayload);
        ArgumentCaptor<MqttDatastreamDiscoveryHandler.EnableDatastreamMessageListener> enableDatastreamMessageListenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamDiscoveryHandler.EnableDatastreamMessageListener.class);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenThrow(new IOException());

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_ENABLE_DATASTREAM_TOPIC),
                enableDatastreamMessageListenerCaptor.capture());
        MqttDatastreamDiscoveryHandler.EnableDatastreamMessageListener testCapturedListener =
                enableDatastreamMessageListenerCaptor.getValue();
        testCapturedListener.messageArrived(testTopic, testMessage);

        verify(mockedSerializer).deserialize(aryEq(testPayload), eq(MqttDatastreamDiscoveryHandler.EnableDatastreamMessage.class));
        verifyZeroInteractions(mockedExecutor);
    }

    @Test
    public void testDisableDeviceMessageListenerMessageArrived() throws MqttException, IOException {
        String testTopic = TEST_DISABLE_DATASTREAM_TOPIC + TOPIC_LEVEL_SEPARATOR + DEVICE_ID_1;
        MqttMessage testMessage = MqttMessage.newInstance(new byte[]{});
        ArgumentCaptor<MqttDatastreamDiscoveryHandler.DisableDeviceMessageListener> disableDeviceMessageListenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamDiscoveryHandler.DisableDeviceMessageListener.class);
        DisableDeviceMessage testDisableDeviceMessage = new DisableDeviceMessage(
                Arrays.asList(new DisabledDatastream(DATASTREAM_ID_1), new DisabledDatastream(DATASTREAM_ID_2)));

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenReturn(testDisableDeviceMessage);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DISABLE_DEVICE_TOPIC),
                disableDeviceMessageListenerCaptor.capture());
        MqttDatastreamDiscoveryHandler.DisableDeviceMessageListener testCapturedListener =
                disableDeviceMessageListenerCaptor.getValue();
        testCapturedListener.messageArrived(testTopic, testMessage);
        verify(mockedExecutor, times(2)).execute(runnableCaptor.capture());
        runnableCaptor.getAllValues().forEach(Runnable::run);

        verify(mockedPermissionManager).removePermission(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1));
        verify(mockedPermissionManager).removePermission(eq(DEVICE_ID_1), eq(DATASTREAM_ID_2));
    }

    @Test
    public void testDisableDeviceMessageListenerMessageArrivedSerializerIOExceptionIsCaught()
            throws MqttException, IOException {
        String testTopic = TEST_DISABLE_DATASTREAM_TOPIC + TOPIC_LEVEL_SEPARATOR + DEVICE_ID_1;
        MqttMessage testMessage = MqttMessage.newInstance(new byte[]{});
        ArgumentCaptor<MqttDatastreamDiscoveryHandler.DisableDeviceMessageListener> disableDeviceMessageListenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamDiscoveryHandler.DisableDeviceMessageListener.class);

        when(mockedSerializer.deserialize(any(byte[].class), any())).thenThrow(new IOException());

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DISABLE_DEVICE_TOPIC),
                disableDeviceMessageListenerCaptor.capture());
        MqttDatastreamDiscoveryHandler.DisableDeviceMessageListener testCapturedListener =
                disableDeviceMessageListenerCaptor.getValue();
        testCapturedListener.messageArrived(testTopic, testMessage);

        verifyZeroInteractions(mockedExecutor);
    }

    @Test
    public void testDisableDatastreamMessageListenerMessageArrived() throws MqttException {
        String testTopic = TEST_DISABLE_DATASTREAM_TOPIC + TOPIC_LEVEL_SEPARATOR + DEVICE_ID_1 + TOPIC_LEVEL_SEPARATOR +
                DATASTREAM_ID_1;
        MqttMessage testMessage = MqttMessage.newInstance(new byte[]{});
        ArgumentCaptor<MqttDatastreamDiscoveryHandler.DisableDatastreamMessageListener> disableMessageListenerCaptor =
                ArgumentCaptor.forClass(MqttDatastreamDiscoveryHandler.DisableDatastreamMessageListener.class);

        verify(mockedClient).subscribe(eq(TEST_SUBSCRIBED_DISABLE_DATASTREAM_TOPIC),
                disableMessageListenerCaptor.capture());
        MqttDatastreamDiscoveryHandler.DisableDatastreamMessageListener testCapturedListener =
                disableMessageListenerCaptor.getValue();
        testCapturedListener.messageArrived(testTopic, testMessage);
        verify(mockedExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getAllValues().forEach(Runnable::run);

        verify(mockedPermissionManager).removePermission(eq(DEVICE_ID_1), eq(DATASTREAM_ID_1));
    }

    @Test
    public void testClose() throws MqttException {
        testHandler.close();

        verify(mockedClient).unsubscribe(eq(TEST_SUBSCRIBED_ENABLE_DATASTREAM_TOPIC));
        verify(mockedClient).unsubscribe(eq(TEST_SUBSCRIBED_DISABLE_DATASTREAM_TOPIC));
        verify(mockedExecutor).shutdown();
    }
}