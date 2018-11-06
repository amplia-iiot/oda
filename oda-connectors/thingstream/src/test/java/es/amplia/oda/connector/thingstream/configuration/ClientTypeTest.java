package es.amplia.oda.connector.thingstream.configuration;

import org.junit.Test;

import static es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration.ClientType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ClientTypeTest {

    @Test
    public void testParseMQTTClientType() {
        assertEquals(ClientType.MQTT, ClientType.parse("mqtt"));
    }

    @Test
    public void testParseInternetClientType() {
        assertEquals(ClientType.MQTT, ClientType.parse("internet"));
    }

    @Test
    public void testParseMQTTSNClientType() {
        assertEquals(ClientType.MQTTSN, ClientType.parse("mqtt-sn"));
    }

    @Test
    public void testParseSerialClientType() {
        assertEquals(ClientType.MQTTSN, ClientType.parse("serial"));
    }

    @Test
    public void testParseUsbClientType() {
        assertEquals(ClientType.MQTTSN, ClientType.parse("usb"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIllegalArgument() {
        ClientType.parse("Invalid");

        fail("Illegal argument exception must be thrown");
    }
}
