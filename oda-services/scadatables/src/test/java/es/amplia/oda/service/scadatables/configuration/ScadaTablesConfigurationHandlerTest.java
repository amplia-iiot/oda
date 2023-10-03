package es.amplia.oda.service.scadatables.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import es.amplia.oda.service.scadatables.internal.ScadaTableInfoService;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

        Map< Map<Integer,String>, ScadaTableEntryConfiguration> tableConfig =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableConfig");
        assertEquals(2, tableConfig.size());
    }

    @Test
    public void testLoadConfigurationData() {
        Dictionary<String, String> dic = new Hashtable<String, String>() {{
            put("BinaryInput,10001", "datastream:batteryAlarm, feed: espontaneo, device: testDevice, transformation: testTransformation");
            put("BinaryInput,10015", "datastream:doorAlarm");
        }};

        testScadaTablesConfigurationHandler.loadConfiguration(dic);

        Map< Map<Integer,String>, ScadaTableEntryConfiguration> tableConfig =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableConfig");
        assertEquals(2, tableConfig.size());

        // get from table element of address 10001
        Map<Integer,String> pairAsduAddress = java.util.Collections.singletonMap(10001, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo = tableConfig.get(pairAsduAddress);
        assertEquals("BinaryInput", scadaInfo.getDataType());
        assertEquals("testDevice", scadaInfo.getDeviceId());
        assertEquals("espontaneo", scadaInfo.getFeed());
        assertNotNull(scadaInfo.getScript());

        // get from table element of address 10015
        Map<Integer,String> pairAsduAddress2 = java.util.Collections.singletonMap(10015, "BinaryInput");
        ScadaTableEntryConfiguration scadaInfo2 = tableConfig.get(pairAsduAddress2);
        assertEquals("BinaryInput", scadaInfo2.getDataType());
        assertNull(scadaInfo2.getDeviceId());
        assertNull(scadaInfo2.getFeed());
        assertNull(scadaInfo2.getScript());
    }

    @Test
    public void testLoadConfigurationWrongConfig(){
        Dictionary<String, String> dic = new Hashtable<String, String>() {{
            put("BinaryInput,10001", "testConfig: test");
        }};

        testScadaTablesConfigurationHandler.loadConfiguration(dic);

        Map< Map<Integer,String>, ScadaTableEntryConfiguration> tableConfig =
                Whitebox.getInternalState(testScadaTablesConfigurationHandler, "currentScadaTableConfig");
        assertEquals(0, tableConfig.size());
    }
}
