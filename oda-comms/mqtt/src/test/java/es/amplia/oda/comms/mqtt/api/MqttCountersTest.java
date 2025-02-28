package es.amplia.oda.comms.mqtt.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class MqttCountersTest {

    @Test
    public void testTopicComparison() {

        assertTrue(MqttCounters.compareTopics("odm/iot/+", "odm/iot/test"));
        assertTrue(MqttCounters.compareTopics("odm/#", "odm/iot/test"));
        assertFalse(MqttCounters.compareTopics("odm/iot/new", "odm/iot/test"));
        assertFalse(MqttCounters.compareTopics("odm/iot/+", "odm/test/iot"));
        assertTrue(MqttCounters.compareTopics("odm/+/test", "odm/iot/test"));
    }
}
