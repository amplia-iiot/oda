package es.amplia.oda.connector.mqtt.configuration;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;


public class LwtConfigurationTest {

    private static final String LWT_TOPIC = "client/lwt";
    private static final byte[] LWT_PAYLOAD = {0x0C, 0x0A, 0x0F, 0x0E};
    private static final String LWT_PAYLOAD_AS_STRING = "I'm gone";
    private static final int LWT_QOS = 1;
    private static final boolean LWT_RETAINED = true;

    private LwtConfiguration lwtConfiguration;

    @Test
    public void testConstructor() {
        lwtConfiguration = new LwtConfiguration(LWT_TOPIC, LWT_PAYLOAD, LWT_QOS, LWT_RETAINED);
        assertNotNull(lwtConfiguration);
    }

    @Test
    public void testConstructorStringPayload() {
        lwtConfiguration = new LwtConfiguration(LWT_TOPIC, LWT_PAYLOAD_AS_STRING, LWT_QOS, LWT_RETAINED);
        assertNotNull(lwtConfiguration);
    }

    @Test
    public void testConfigure() {
        lwtConfiguration = new LwtConfiguration(LWT_TOPIC, LWT_PAYLOAD, LWT_QOS, LWT_RETAINED);
        MqttConnectOptions options = new MqttConnectOptions();

        lwtConfiguration.configure(options);

        MqttMessage willMessage = options.getWillMessage();
        assertEquals(LWT_TOPIC, options.getWillDestination());
        assertArrayEquals(LWT_PAYLOAD, willMessage.getPayload());
        assertEquals(LWT_QOS, willMessage.getQos());
        assertEquals(LWT_RETAINED, willMessage.isRetained());
    }

    @Test
    public void testConfigurePayloadAsString() {
        lwtConfiguration = new LwtConfiguration(LWT_TOPIC, LWT_PAYLOAD_AS_STRING, LWT_QOS, LWT_RETAINED);
        MqttConnectOptions options = new MqttConnectOptions();

        lwtConfiguration.configure(options);

        MqttMessage willMessage = options.getWillMessage();
        assertEquals(LWT_TOPIC, options.getWillDestination());
        assertEquals(LWT_PAYLOAD_AS_STRING, new String(willMessage.getPayload(), StandardCharsets.UTF_8));
        assertEquals(LWT_QOS, willMessage.getQos());
        assertEquals(LWT_RETAINED, willMessage.isRetained());
    }
}