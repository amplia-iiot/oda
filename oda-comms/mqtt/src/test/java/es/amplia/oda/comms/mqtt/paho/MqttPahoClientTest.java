package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;

import es.amplia.oda.core.commons.entities.ContentType;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MqttPahoConnectOptionsMapper.class)
public class MqttPahoClientTest {

    private static final String TEST_USERNAME = "testUser";
    private static final char[] TEST_API_KEY = UUID.randomUUID().toString().toCharArray();
    private static final String TEST_TOPIC = "test/topic";

    @Mock
    private org.eclipse.paho.client.mqttv3.MqttAsyncClient mockedInnerClient;
    @Mock
    private ResubscribeTopicsOnReconnectCallback mockedResubscribeTopicsCallback;
    @InjectMocks
    private MqttPahoClient testClient;

    @Mock
    private MqttMessageListener mockedListener;


    @Test
    public void testConnect() throws MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        testClient.connect();

        verify(mockedResubscribeTopicsCallback).listenTo(eq(mockedInnerClient));
        verify(mockedInnerClient).connect();
    }

    @Test(expected = MqttException.class)
    public void testConnectThrowException() throws MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        doThrow(new org.eclipse.paho.client.mqttv3.MqttException(1)).when(mockedInnerClient).connect();

        testClient.connect();
    }

    @Test
    public void testConnectWithOptions() throws MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        MqttConnectOptions options = MqttConnectOptions.builder(TEST_USERNAME, TEST_API_KEY).build();
        org.eclipse.paho.client.mqttv3.MqttConnectOptions mockedInnerOptions =
                mock(org.eclipse.paho.client.mqttv3.MqttConnectOptions.class);

        PowerMockito.mockStatic(MqttPahoConnectOptionsMapper.class);
        PowerMockito.when(MqttPahoConnectOptionsMapper.from(any(MqttConnectOptions.class)))
                .thenReturn(mockedInnerOptions);

        testClient.connect(options, null);

        PowerMockito.verifyStatic(MqttPahoConnectOptionsMapper.class);
        MqttPahoConnectOptionsMapper.from(eq(options));
        verify(mockedResubscribeTopicsCallback).listenTo(eq(mockedInnerClient));
        verify(mockedInnerClient).connect(eq(mockedInnerOptions), eq(null), any(IMqttActionListener.class));
    }

    @Test(expected = MqttException.class)
    public void testConnectWithOptionsException() throws MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        MqttConnectOptions options = MqttConnectOptions.builder(TEST_USERNAME, TEST_API_KEY).build();
        org.eclipse.paho.client.mqttv3.MqttConnectOptions mockedInnerOptions =
                mock(org.eclipse.paho.client.mqttv3.MqttConnectOptions.class);

        PowerMockito.mockStatic(MqttPahoConnectOptionsMapper.class);
        PowerMockito.when(MqttPahoConnectOptionsMapper.from(any(MqttConnectOptions.class)))
                .thenReturn(mockedInnerOptions);
        doThrow(new org.eclipse.paho.client.mqttv3.MqttException(1)).when(mockedInnerClient).connect(any(), any(), any());

        testClient.connect(options, null);
    }

    @Test
    public void testIsConnected() {
        when(mockedInnerClient.isConnected()).thenReturn(true);

        assertTrue(testClient.isConnected());
        verify(mockedInnerClient).isConnected();
    }

    @Test
    public void testPublish() throws MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        byte[] testPayload = new byte[] { 0x1, 0x2, 0x3, 0x4 };
        int testQos = 1;
        MqttMessage testMessage = MqttMessage.newInstance(testPayload, testQos, true);

        testClient.publish(TEST_TOPIC, testMessage, ContentType.JSON);

        verify(mockedInnerClient).publish(eq(TEST_TOPIC), eq(testPayload), eq(testQos), eq(true));
    }

    @Test(expected = MqttException.class)
    public void testPublishException() throws MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        byte[] testPayload = new byte[] { 0x1, 0x2, 0x3, 0x4 };
        int testQos = 1;
        MqttMessage testMessage = MqttMessage.newInstance(testPayload, testQos, true);

        doThrow(new org.eclipse.paho.client.mqttv3.MqttException(1)).when(mockedInnerClient)
                .publish(anyString(), any(byte[].class), anyInt(), anyBoolean());

        testClient.publish(TEST_TOPIC, testMessage, ContentType.JSON);
    }

    /*@Test
    public void testSubscribe() throws Exception {
        ArgumentCaptor<IMqttMessageListener> innerListenerCaptor = ArgumentCaptor.forClass(IMqttMessageListener.class);

        testClient.subscribe(TEST_TOPIC, mockedListener);

        verify(mockedInnerClient).subscribe(eq(TEST_TOPIC), 2, innerListenerCaptor.capture());
        IMqttMessageListener innerListener = innerListenerCaptor.getValue();
        verify(mockedResubscribeTopicsCallback).addSubscribedTopic(eq(TEST_TOPIC), eq(innerListener));
        assertEquals(mockedListener, Whitebox.getInternalState(innerListener, "mqttMessageListener"));
    }*/

    /*@Test(expected = MqttException.class)
    public void testSubscribeException() throws Exception {
        doThrow(new org.eclipse.paho.client.mqttv3.MqttException(1)).when(mockedInnerClient)
                .subscribe(anyString(), eq(2), any(MqttPahoMessageListener.class));

        testClient.subscribe(TEST_TOPIC, mockedListener);
    }*/

    @Test
    public void testUnsubscribe() throws MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        testClient.unsubscribe(TEST_TOPIC);

        verify(mockedInnerClient).unsubscribe(eq(TEST_TOPIC));
    }

    @Test(expected = MqttException.class)
    public void testUnsubscribeException() throws MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        doThrow(new org.eclipse.paho.client.mqttv3.MqttException(1)).when(mockedInnerClient).unsubscribe(anyString());

        testClient.unsubscribe(TEST_TOPIC);
    }

    @Test
    public void testDisconnect() throws MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        testClient.disconnect();

        verify(mockedInnerClient).disconnect();
    }

    @Test(expected = MqttException.class)
    public void testDisconnectException() throws MqttException, org.eclipse.paho.client.mqttv3.MqttException {
        doThrow(new org.eclipse.paho.client.mqttv3.MqttException(1)).when(mockedInnerClient).disconnect();

        testClient.disconnect();
    }
}