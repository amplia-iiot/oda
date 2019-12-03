package es.amplia.oda.comms.mqtt.paho;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResubscribeTopicsOnReconnectCallbackTest {

    private static final String TEST_TOPIC = "test/topic";


    private final ResubscribeTopicsOnReconnectCallback testCallback = new ResubscribeTopicsOnReconnectCallback();

    @Mock
    private IMqttClient mockedInnerClient;
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

        verify(mockedInnerClient).subscribe(eq(TEST_TOPIC), eq(mockedListener));
    }

    @Test
    public void testConnectCompleteReconnectionWithSubscribedListenersExceptionsAreCaught() throws MqttException {
        Map<String, IMqttMessageListener> listeners = new HashMap<>();
        listeners.put(TEST_TOPIC, mockedListener);

        Whitebox.setInternalState(testCallback, "innerClient", mockedInnerClient);
        Whitebox.setInternalState(testCallback, "subscribedListeners", listeners);

        doThrow(new MqttException(1)).when(mockedInnerClient).subscribe(anyString(), any(IMqttMessageListener.class));

        testCallback.connectComplete(true, "http://test.uri:1883");

        verify(mockedInnerClient).subscribe(eq(TEST_TOPIC), eq(mockedListener));
    }

    @Test
    public void testConnectCompleteNoReconnectionWithSubscribedListeners() {
        Map<String, IMqttMessageListener> listeners = new HashMap<>();
        listeners.put(TEST_TOPIC, mockedListener);

        Whitebox.setInternalState(testCallback, "innerClient", mockedInnerClient);
        Whitebox.setInternalState(testCallback, "subscribedListeners", listeners);

        testCallback.connectComplete(false, "http://test.uri:1883");

        verifyZeroInteractions(mockedInnerClient);
    }
}