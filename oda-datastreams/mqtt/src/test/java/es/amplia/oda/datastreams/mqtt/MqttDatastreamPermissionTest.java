package es.amplia.oda.datastreams.mqtt;

import org.junit.Test;

import static org.junit.Assert.*;

public class MqttDatastreamPermissionTest {

    @Test
    public void testRDPermissions() {
        assertTrue(MqttDatastreamPermission.RD.isReadable());
        assertFalse(MqttDatastreamPermission.RD.isWritable());
    }

    @Test
    public void testWRPermissions() {
        assertFalse(MqttDatastreamPermission.WR.isReadable());
        assertTrue(MqttDatastreamPermission.WR.isWritable());
    }

    @Test
    public void testRWPermissions() {
        assertTrue(MqttDatastreamPermission.RW.isReadable());
        assertTrue(MqttDatastreamPermission.RW.isWritable());
    }
}