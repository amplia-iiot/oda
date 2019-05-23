package es.amplia.oda.datastreams.mqtt;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MqttDatastreamsPermissionManagerTest {

    private static final String TEST_DEVICE_1 = "testDevice1";
    private static final String TEST_DATASTREAM_1 = "testDatastream1";
    private static final String TEST_DEVICE_2 = "testDevice2";
    private static final String TEST_DATASTREAM_2 = "testDatastream2";
    private static final String TEST_DEVICE_3 = "testDevice3";
    private static final String TEST_DATASTREAM_3 = "testDatastream3";


    private final MqttDatastreamsPermissionManager testManager = new MqttDatastreamsPermissionManager();

    @Spy
    private Map<DatastreamInfo, MqttDatastreamPermission> spiedPermissions = new ConcurrentHashMap<>();

    @Before
    public void setUp() {
        Whitebox.setInternalState(testManager, "permissions", spiedPermissions);
    }

    @Test
    public void testAddPermission() {
        testManager.addPermission(TEST_DEVICE_1, TEST_DATASTREAM_1, MqttDatastreamPermission.RD);

        verify(spiedPermissions)
                .put(eq(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1)), eq(MqttDatastreamPermission.RD));
    }

    @Test
    public void testAddPermissionAlreadyRegisteredDatastreamAndDevice() {
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1), MqttDatastreamPermission.RD);

        testManager.addPermission(TEST_DEVICE_1, TEST_DATASTREAM_1, MqttDatastreamPermission.WR);

        verify(spiedPermissions)
                .put(eq(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1)), eq(MqttDatastreamPermission.WR));
        assertEquals(MqttDatastreamPermission.WR,
                spiedPermissions.get(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1)));
    }

    @Test
    public void testRemovePermission() {
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1), MqttDatastreamPermission.RD);

        testManager.removePermission(TEST_DEVICE_1, TEST_DATASTREAM_1);

        verify(spiedPermissions).remove(eq(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1)));
        assertTrue(spiedPermissions.isEmpty());
    }

    @Test
    public void testRemovePermissionDoesNotThrowExceptionWithEmptyPermissionsCollection() {
        testManager.removePermission(TEST_DEVICE_1, TEST_DATASTREAM_1);

        verify(spiedPermissions).remove(eq(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1)));
    }

    @Test
    public void testRemovePermissionDoesNotThrowExceptionWithNoPresentElement() {
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1), MqttDatastreamPermission.RD);
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_2, TEST_DATASTREAM_2), MqttDatastreamPermission.WR);

        testManager.removePermission(TEST_DEVICE_1, TEST_DATASTREAM_2);

        verify(spiedPermissions).remove(eq(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_2)));
        assertEquals(2, spiedPermissions.size());
    }

    @Test
    public void testRemoveDevicePermissions() {
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1), MqttDatastreamPermission.RD);
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_2), MqttDatastreamPermission.WR);
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_2, TEST_DATASTREAM_1), MqttDatastreamPermission.RD);
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_2, TEST_DATASTREAM_2), MqttDatastreamPermission.WR);

        testManager.removeDevicePermissions(TEST_DEVICE_1);

        verify(spiedPermissions).remove(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1));
        verify(spiedPermissions).remove(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_2));
        verify(spiedPermissions, never()).remove(new DatastreamInfo(TEST_DEVICE_2, TEST_DATASTREAM_1));
        verify(spiedPermissions, never()).remove(new DatastreamInfo(TEST_DEVICE_2, TEST_DATASTREAM_2));
    }

    @Test
    public void testHasReadPermission() {
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1), MqttDatastreamPermission.RD);
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_2, TEST_DATASTREAM_2), MqttDatastreamPermission.WR);
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_3, TEST_DATASTREAM_3), MqttDatastreamPermission.RW);

        assertTrue(testManager.hasReadPermission(TEST_DEVICE_1, TEST_DATASTREAM_1));
        assertFalse(testManager.hasReadPermission(TEST_DEVICE_2, TEST_DATASTREAM_2));
        assertTrue(testManager.hasReadPermission(TEST_DEVICE_3, TEST_DATASTREAM_3));
        assertFalse(testManager.hasReadPermission(TEST_DEVICE_1, TEST_DATASTREAM_2));
    }

    @Test
    public void testHasWritePermission() {
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_1, TEST_DATASTREAM_1), MqttDatastreamPermission.RD);
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_2, TEST_DATASTREAM_2), MqttDatastreamPermission.WR);
        spiedPermissions.put(new DatastreamInfo(TEST_DEVICE_3, TEST_DATASTREAM_3), MqttDatastreamPermission.RW);

        assertFalse(testManager.hasWritePermission(TEST_DEVICE_1, TEST_DATASTREAM_1));
        assertTrue(testManager.hasWritePermission(TEST_DEVICE_2, TEST_DATASTREAM_2));
        assertTrue(testManager.hasWritePermission(TEST_DEVICE_3, TEST_DATASTREAM_3));
        assertFalse(testManager.hasWritePermission(TEST_DEVICE_1, TEST_DATASTREAM_2));
    }
}