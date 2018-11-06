package es.amplia.oda.connector.thingstream.configuration;

import org.junit.Test;

import static es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration.QOS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class QOSTypeTest {

    @Test
    public void testParseQOS0() {
        assertEquals(QOS.QOS_0, QOS.parse("qos_0"));
    }

    @Test
    public void testParse0() {
        assertEquals(QOS.QOS_0, QOS.parse("0"));
    }

    @Test
    public void testParseQOS1() {
        assertEquals(QOS.QOS_1, QOS.parse("qos_1"));
    }

    @Test
    public void testParse1() {
        assertEquals(QOS.QOS_1, QOS.parse("1"));
    }

    @Test
    public void testParseQOS2() {
        assertEquals(QOS.QOS_2, QOS.parse("qos_2"));
    }

    @Test
    public void testParse2() {
        assertEquals(QOS.QOS_2, QOS.parse("2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIllegalArgument() {
        QOS.parse("Invalid");

        fail("Illegal argument exception must be thrown");
    }
}
