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
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MqttDatastreamsLwtHandlerTest {

    private static final String TEST_LWT_TOPIC = "test/will";

    @Mock
    private MqttClient mockedClient;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private MqttDatastreamsPermissionManager mockedPermissionManager;
    @Mock
    private MqttDatastreamsManager mockedDatastreamsManager;

    private MqttDatastreamsLwtHandler testLwtHandler;

    @Captor
    private ArgumentCaptor<MqttDatastreamsLwtHandler.LwtMessageListener> messageListenerCaptor;

    @Before
    public void setUp() throws Exception {
        testLwtHandler = new MqttDatastreamsLwtHandler(mockedClient, mockedSerializer, mockedPermissionManager,
                mockedDatastreamsManager, TEST_LWT_TOPIC);
    }

    @Test
    public void testConstructor() throws MqttException {
        verify(mockedClient).subscribe(eq(TEST_LWT_TOPIC), any(MqttMessageListener.class));
    }

    @Test
    public void testMessageArrived() throws MqttException, IOException {
        byte[] testPayload = new byte[] { 1, 2, 3, 4 };
        String testDevice1 = "testDevice1";
        String testDevice2 = "testDevice2";
        String testDevice3 = "testDevice3";
        MqttDatastreamsLwtHandler.LwtMessage message =
                new MqttDatastreamsLwtHandler.LwtMessage(Arrays.asList(testDevice1, testDevice2, testDevice3));

        verify(mockedClient).subscribe(eq(TEST_LWT_TOPIC), messageListenerCaptor.capture());

        MqttMessageListener testMessageListener = messageListenerCaptor.getValue();

        when(mockedSerializer.deserialize(any(byte[].class), eq(MqttDatastreamsLwtHandler.LwtMessage.class)))
                .thenReturn(message);

        testMessageListener.messageArrived(TEST_LWT_TOPIC, MqttMessage.newInstance(testPayload));

        verify(mockedSerializer).deserialize(aryEq(testPayload), eq(MqttDatastreamsLwtHandler.LwtMessage.class));
        verify(mockedPermissionManager).removeDevicePermissions(eq(testDevice1));
        verify(mockedPermissionManager).removeDevicePermissions(eq(testDevice2));
        verify(mockedPermissionManager).removeDevicePermissions(eq(testDevice3));
        verify(mockedDatastreamsManager).removeDevice(eq(testDevice1));
        verify(mockedDatastreamsManager).removeDevice(eq(testDevice2));
        verify(mockedDatastreamsManager).removeDevice(eq(testDevice3));
    }

    @Test
    public void testMessageArrivedIOExceptionIsCaught() throws MqttException, IOException {
        verify(mockedClient).subscribe(eq(TEST_LWT_TOPIC), messageListenerCaptor.capture());

        MqttMessageListener testMessageListener = messageListenerCaptor.getValue();

        doThrow(new IOException()).when(mockedSerializer).deserialize(any(), any());

        testMessageListener.messageArrived(TEST_LWT_TOPIC, MqttMessage.newInstance(new byte[0]));

        verifyZeroInteractions(mockedPermissionManager);
        verifyZeroInteractions(mockedDatastreamsManager);
    }

    @Test
    public void testClose() throws MqttException {
        testLwtHandler.close();

        verify(mockedClient).unsubscribe(eq(TEST_LWT_TOPIC));
    }
}