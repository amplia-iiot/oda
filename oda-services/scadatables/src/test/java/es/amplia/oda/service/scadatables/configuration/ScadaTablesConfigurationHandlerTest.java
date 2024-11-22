package es.amplia.oda.service.scadatables.configuration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import es.amplia.oda.service.scadatables.internal.ScadaTableInfoService;

import java.util.*;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ScadaTablesConfigurationHandler.class)
public class ScadaTablesConfigurationHandlerTest {
    @Mock
    ScadaTableInfoService mockedScadaTableInfoService;
    @Mock
    ScadaTableEntryConfiguration mockedEntry;

    ScadaTablesConfigurationHandler testScadaTablesConfigurationHandler;

    @Before
    public void setUp() {
        testScadaTablesConfigurationHandler = new ScadaTablesConfigurationHandler(mockedScadaTableInfoService);
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        whenNew(ScadaTableEntryConfiguration.class).withAnyArguments().thenReturn(mockedEntry);
        doNothing().when(mockedEntry).setScript(any());
        Dictionary<String, String> dic = new Hashtable<String, String>() {{
            put("BinaryInput,10001", "datastream:batteryAlarm, device:testDevice");
            put("BinaryInput,10015", "datastream:doorAlarm, device:testDevice2");
        }};

        testScadaTablesConfigurationHandler.loadConfiguration(dic);

        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableRecollection =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableRecollection");
        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableEvents =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableEvents");
        assertEquals(2, scadaTableRecollection.size());
        assertEquals(0, scadaTableEvents.size());
    }

    @Test
    public void testLoadConfigurationData() {
        Dictionary<String, String> dic = new Hashtable<String, String>() {{
            put("BinaryInput,10001", "datastream:batteryAlarm, feed: espontaneo, device: testDevice, " +
                    "transformation: testTransformation, eventPublish: dispatcher");
            put("BinaryInput,10011", "datastream:temperatureAlarm, feed: recoleccion");
            put("BinaryInput,10015", "datastream:doorAlarm");
        }};

        testScadaTablesConfigurationHandler.loadConfiguration(dic);

        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableRecollection =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableRecollection");
        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableEvents =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableEvents");
        assertEquals(2, scadaTableRecollection.size());
        assertEquals(1, scadaTableEvents.size());

        // get from table element of address 10001
        Map<Integer, String> pairAsduAddress = java.util.Collections.singletonMap(10001, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo = scadaTableEvents.get(pairAsduAddress);
        assertEquals("BinaryInput", scadaInfo.getDataType());
        assertEquals("testDevice", scadaInfo.getDeviceId());
        assertEquals("espontaneo", scadaInfo.getFeed());
        assertTrue(scadaInfo.isEvent());
        assertNotNull(scadaInfo.getScript());

        // get from table element of address 10011
        Map<Integer, String> pairAsduAddress2 = java.util.Collections.singletonMap(10011, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo2 = scadaTableRecollection.get(pairAsduAddress2);
        assertEquals("BinaryInput", scadaInfo2.getDataType());
        Assert.assertNull(scadaInfo2.getDeviceId());
        assertEquals("recoleccion", scadaInfo2.getFeed());
        assertFalse(scadaInfo2.isEvent());
        assertNull(scadaInfo2.getScript());

        // get from table element of address 10015
        Map<Integer, String> pairAsduAddress3 = java.util.Collections.singletonMap(10015, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo3 = scadaTableRecollection.get(pairAsduAddress3);
        assertEquals("BinaryInput", scadaInfo3.getDataType());
        assertNull(scadaInfo3.getDeviceId());

        assertNull(scadaInfo3.getFeed());
        assertFalse(scadaInfo2.isEvent());
        assertNull(scadaInfo3.getScript());
    }

    @Test
    public void testLoadConfigurationWrongConfig() {
        Dictionary<String, String> dic = new Hashtable<String, String>() {{
            put("BinaryInput,10001", "testConfig: test");
        }};

        testScadaTablesConfigurationHandler.loadConfiguration(dic);

        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableRecollection =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableRecollection");
        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableEvents =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableEvents");
        assertEquals(0, scadaTableRecollection.size());
        assertEquals(0, scadaTableEvents.size());
    }

    @Test
    public void testSameDatastreamIdSameDeviceIdDifferentTable() {

        // we can have the same datastreamId associated to the same deviceId as long one it is an event and the other not
        Dictionary<String, String> dic = new Hashtable<String, String>() {{
            put("BinaryInput,10001", "datastream:batteryAlarm, device:deviceTest1");
            put("BinaryInput,10011", "datastream:batteryAlarm, device:deviceTest1, eventPublish: statemanager");
        }};

        testScadaTablesConfigurationHandler.loadConfiguration(dic);

        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableRecollection =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableRecollection");
        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableEvents =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableEvents");
        assertEquals(1, scadaTableRecollection.size());
        assertEquals(1, scadaTableEvents.size());

        Map<Integer, String> pairAsduAddress1 = java.util.Collections.singletonMap(10001, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo1 = scadaTableRecollection.get(pairAsduAddress1);
        assertEquals("batteryAlarm", scadaInfo1.getDatastreamId());
        assertFalse(scadaInfo1.isEvent());

        Map<Integer, String> pairAsduAddress2 = java.util.Collections.singletonMap(10011, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo2 = scadaTableEvents.get(pairAsduAddress2);
        assertEquals("batteryAlarm", scadaInfo2.getDatastreamId());
        assertTrue(scadaInfo2.isEvent());
    }

    @Test
    public void testSameDatastreamSameDeviceIdSameTable() {

        // we can't have the same datastreamId associated to the same deviceId if both are of the same type (events or not)
        Dictionary<String, String> dic = new Hashtable<String, String>() {{
            put("BinaryInput,10001", "datastream:batteryAlarm, device:deviceTest1, event: false");
            put("BinaryInput,10011", "datastream:batteryAlarm, device:deviceTest1, event: false");
        }};

        testScadaTablesConfigurationHandler.loadConfiguration(dic);

        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableRecollection =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableRecollection");
        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableEvents =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableEvents");

        // when loading the second entry with the same datastreamId and deviceId, error will be logged and entry won't be loaded
        assertEquals(1, scadaTableRecollection.size());
        assertEquals(0, scadaTableEvents.size());

        Map<Integer, String> pairAsduAddress1 = java.util.Collections.singletonMap(10001, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo1 = scadaTableRecollection.get(pairAsduAddress1);
        assertEquals("batteryAlarm", scadaInfo1.getDatastreamId());
        assertFalse(scadaInfo1.isEvent());

        Map<Integer, String> pairAsduAddress2 = java.util.Collections.singletonMap(10011, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo2 = scadaTableRecollection.get(pairAsduAddress2);
        assertNull(scadaInfo2);
    }


    @Test
    public void testSameDatastreamIdDiffDeviceSameTable() {

        // we can have the same datastreamId associated to two different deviceIds
        Dictionary<String, String> dic = new Hashtable<String, String>() {{
            put("BinaryInput,10001", "datastream:batteryAlarm, device:deviceTest1, event: false");
            put("BinaryInput,10011", "datastream:batteryAlarm, device:deviceTest2, event: false");
        }};

        testScadaTablesConfigurationHandler.loadConfiguration(dic);

        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableRecollection =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableRecollection");
        Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTableEvents =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableEvents");

        assertEquals(2, scadaTableRecollection.size());
        assertEquals(0, scadaTableEvents.size());

        Map<Integer, String> pairAsduAddress1 = java.util.Collections.singletonMap(10001, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo1 = scadaTableRecollection.get(pairAsduAddress1);
        assertEquals("batteryAlarm", scadaInfo1.getDatastreamId());
        assertFalse(scadaInfo1.isEvent());

        Map<Integer, String> pairAsduAddress2 = java.util.Collections.singletonMap(10011, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo2 = scadaTableRecollection.get(pairAsduAddress2);
        assertEquals("batteryAlarm", scadaInfo2.getDatastreamId());
        assertFalse(scadaInfo2.isEvent());
    }
}
