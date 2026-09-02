package es.amplia.oda.comms.mqtt.paho;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ResubscribeTopicsOnReconnectCallbackTest {

    private static final String TEST_TOPIC = "test/topic";


    private final ResubscribeTopicsOnReconnectCallback testCallback = new ResubscribeTopicsOnReconnectCallback();

    @Mock
    private IMqttAsyncClient mockedInnerClient;
    @Mock
    private IMqttMessageListener mockedListener;

    @Test
    public void testListenTo() {
        testCallback.listenTo(mockedInnerClient);

        assertEquals(mockedInnerClient, Whitebox.getInternalState(testCallback, "innerClient"));
        verify(mockedInnerClient).setCallback(eq(testCallback));
    }

    @Test
    public void testAddSubscribedTopic() {
        testCallback.addSubscribedTopic(TEST_TOPIC, mockedListener);

        Map<String, IMqttMessageListener> listeners = Whitebox.getInternalState(testCallback, "subscribedListeners");
        assertTrue(listeners.containsKey(TEST_TOPIC));
        assertEquals(mockedListener, listeners.get(TEST_TOPIC));
    }

    @Test
    public void testConnectCompleteReconnectionWithSubscribedListeners() throws MqttException {
        Map<String, IMqttMessageListener> listeners = new HashMap<>();
        listeners.put(TEST_TOPIC, mockedListener);

        Whitebox.setInternalState(testCallback, "innerClient", mockedInnerClient);
        Whitebox.setInternalState(testCallback, "subscribedListeners", listeners);

        testCallback.connectComplete(true, "http://test.uri:1883");

        verify(mockedInnerClient).subscribe(eq(TEST_TOPIC), eq(2), eq(null), any(IMqttActionListener.class), eq(mockedListener));
    }

    @Test
    public void testConnectCompleteReconnectionWithSubscribedListenersExceptionsAreCaught() throws MqttException {
        Map<String, IMqttMessageListener> listeners = new HashMap<>();
        listeners.put(TEST_TOPIC, mockedListener);

        Whitebox.setInternalState(testCallback, "innerClient", mockedInnerClient);
        Whitebox.setInternalState(testCallback, "subscribedListeners", listeners);

        doThrow(new MqttException(1)).when(mockedInnerClient).subscribe(anyString(), eq(2), any(IMqttMessageListener.class));

        testCallback.connectComplete(true, "http://test.uri:1883");

        verify(mockedInnerClient).subscribe(eq(TEST_TOPIC), eq(2), eq(null), any(IMqttActionListener.class), eq(mockedListener));
    }

    @Test
    public void testConnectCompleteNoReconnectionWithSubscribedListeners() throws MqttException {
        Map<String, IMqttMessageListener> listeners = new HashMap<>();
        listeners.put(TEST_TOPIC, mockedListener);

        Whitebox.setInternalState(testCallback, "innerClient", mockedInnerClient);
        Whitebox.setInternalState(testCallback, "subscribedListeners", listeners);

        testCallback.connectComplete(false, "http://test.uri:1883");

        verify(mockedInnerClient).subscribe(eq(TEST_TOPIC), eq(2), eq(null), any(IMqttActionListener.class), eq(mockedListener));
    }
}