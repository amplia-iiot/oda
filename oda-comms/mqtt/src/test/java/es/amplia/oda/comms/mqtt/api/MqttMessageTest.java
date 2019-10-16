package es.amplia.oda.comms.mqtt.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class MqttMessageTest {

    private final byte[] TEST_PAYLOAD = { 0x1, 0x2, 0x3, 0x4 };
    private final int TEST_QOS = 2;
    private final boolean TEST_RETAINED = true;
    private final MqttMessage TEST_MESSAGE = MqttMessage.newInstance(TEST_PAYLOAD, TEST_QOS, TEST_RETAINED);

    @Test
    public void testNewInstanceWithDefaultParams() {
        MqttMessage message = MqttMessage.newInstance(TEST_PAYLOAD);

        assertArrayEquals(TEST_PAYLOAD, message.getPayload());
        assertEquals(MqttMessage.DEFAULT_QOS, message.getQos());
        assertEquals(MqttMessage.DEFAULT_RETAINED, message.isRetained());
    }

    @Test
    public void testNewInstanceWithAllParams() {
        int testQos = 1;

        MqttMessage message = MqttMessage.newInstance(TEST_PAYLOAD, testQos, true);

        assertArrayEquals(TEST_PAYLOAD, message.getPayload());
        assertEquals(testQos, message.getQos());
        assertTrue(message.isRetained());
    }

    @Test
    public void testMqttMessageIsEffectivelyImmutableChangingPayloadPassedInConstructor() {
        byte[] payloadToChange = TEST_PAYLOAD.clone();

        MqttMessage message = MqttMessage.newInstance(payloadToChange);
        payloadToChange[2] = 0;

        assertArrayEquals(TEST_PAYLOAD, message.getPayload());
    }

    @Test
    public void tetMqttMessageIsEffectivelyImmutableChangingPayloadReturnedInGet() {
        MqttMessage message = MqttMessage.newInstance(TEST_PAYLOAD);

        byte[] payloadToChange = message.getPayload();
        payloadToChange[2] = 0;

        assertArrayEquals(TEST_PAYLOAD, message.getPayload());
    }

    @Test
    public void testEqualsWithThis() {
        assertEquals(TEST_MESSAGE, TEST_MESSAGE);
    }

    @Test
    @SuppressWarnings({"ObjectEqualsNull", "ConstantConditions"})
    public void testEqualsWithNullObject() {
        boolean equals = TEST_MESSAGE.equals(null);

        assertFalse(equals);
    }

    @Test
    public void testEqualsWithNullOtherClass() {
        boolean equals = TEST_MESSAGE.equals(new Object());

        assertFalse(equals);
    }

    @Test
    public void testEqualsWithEqualObject() {
        MqttMessage message2 = MqttMessage.newInstance(TEST_PAYLOAD, TEST_QOS, TEST_RETAINED);

        boolean equals = TEST_MESSAGE.equals(message2);

        assertTrue(equals);
    }

    @Test
    public void testEqualsWithDifferentPayloadObject() {
        MqttMessage message2 = MqttMessage.newInstance(new byte[] {5,6,7,8}, TEST_QOS, TEST_RETAINED);

        boolean equals = TEST_MESSAGE.equals(message2);

        assertFalse(equals);
    }

    @Test
    public void testEqualsWithDifferentQosObject() {
        MqttMessage message2 = MqttMessage.newInstance(TEST_PAYLOAD, 1, TEST_RETAINED);

        boolean equals = TEST_MESSAGE.equals(message2);

        assertFalse(equals);
    }

    @Test
    public void testEqualsWithDifferentRetainedObject() {
        MqttMessage message2 = MqttMessage.newInstance(TEST_PAYLOAD, TEST_QOS, false);

        boolean equals = TEST_MESSAGE.equals(message2);

        assertFalse(equals);
    }

    @Test
    public void testHashCodeWithEqualObject() {
        MqttMessage message2 = MqttMessage.newInstance(TEST_PAYLOAD, TEST_QOS, TEST_RETAINED);

        assertEquals(TEST_MESSAGE.hashCode(), message2.hashCode());
    }

    @Test
    public void testHashCodeWithDifferentPayloadObject() {
        MqttMessage message2 = MqttMessage.newInstance(new byte[] {5,6,7,8}, TEST_QOS, TEST_RETAINED);

        assertNotEquals(TEST_MESSAGE.hashCode(), message2.hashCode());
    }

    @Test
    public void testHashCodeWithDifferentQosObject() {
        MqttMessage message2 = MqttMessage.newInstance(TEST_PAYLOAD, 1, TEST_RETAINED);

        assertNotEquals(TEST_MESSAGE.hashCode(), message2.hashCode());
    }

    @Test
    public void testHashCodeWithDifferentRetainedObject() {
        MqttMessage message2 = MqttMessage.newInstance(TEST_PAYLOAD, TEST_QOS, false);

        assertNotEquals(TEST_MESSAGE.hashCode(), message2.hashCode());
    }
}