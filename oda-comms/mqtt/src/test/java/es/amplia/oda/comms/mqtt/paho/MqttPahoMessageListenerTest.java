package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MqttPahoMessageListenerTest {

    private static final String TEST_TOPIC = "test/topic";
    private static final byte[] TEST_PAYLOAD = "Hello World!".getBytes(StandardCharsets.UTF_8);
    private static final int TEST_QOS = 1;
    private static final boolean TEST_RETAINED = true;

    @Mock
    private MqttMessageListener mockedInnerListener;
    @Mock
    private IMqttAsyncClient mockedInnerMqttClient;
    @InjectMocks
    private MqttPahoMessageListener testListener;


    @Test
    public void testMessageArrived() throws MqttException, InterruptedException {
        org.eclipse.paho.client.mqttv3.MqttMessage testMessage =
                new org.eclipse.paho.client.mqttv3.MqttMessage(TEST_PAYLOAD);
        testMessage.setQos(TEST_QOS);
        testMessage.setRetained(TEST_RETAINED);
        ArgumentCaptor<MqttMessage> messageCaptor = ArgumentCaptor.forClass(MqttMessage.class);

        testListener.messageArrived(TEST_TOPIC, testMessage);

        Thread.sleep(1000);

        verify(mockedInnerListener).messageArrived(eq(TEST_TOPIC), messageCaptor.capture());
        verify(mockedInnerMqttClient).messageArrivedComplete(testMessage.getId(),TEST_QOS);
        MqttMessage createdMessage = messageCaptor.getValue();
        assertArrayEquals(TEST_PAYLOAD, createdMessage.getPayload());
        assertEquals(TEST_QOS, createdMessage.getQos());
        assertEquals(TEST_RETAINED, createdMessage.isRetained());
    }
}