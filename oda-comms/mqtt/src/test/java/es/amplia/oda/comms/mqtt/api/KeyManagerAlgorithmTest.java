package es.amplia.oda.comms.mqtt.api;

import org.junit.Test;

import static es.amplia.oda.comms.mqtt.api.MqttConnectOptions.KeyManagerAlgorithm;
import static org.junit.Assert.assertEquals;

public class KeyManagerAlgorithmTest {

    @Test
    public void testFromWithPKIX() {
        assertEquals(KeyManagerAlgorithm.PKIX, KeyManagerAlgorithm.from("PKIX"));
    }

    @Test
    public void testFromWithSunX509() {
        assertEquals(KeyManagerAlgorithm.SUN_X509, KeyManagerAlgorithm.from("SunX509"));
    }

    @Test
    public void testFromWithSunJSSE() {
        assertEquals(KeyManagerAlgorithm.SUN_JSSE, KeyManagerAlgorithm.from("SunJSSE"));
    }

    @Test
    public void testFromWithEnumValue() {
        KeyManagerAlgorithm.from(KeyManagerAlgorithm.SUN_JSSE.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromWithInvalidArgument() {
        KeyManagerAlgorithm.from("Invalid");
    }
}
