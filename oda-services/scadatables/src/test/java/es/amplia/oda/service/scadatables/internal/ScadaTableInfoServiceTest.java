package es.amplia.oda.service.scadatables.internal;

import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.service.scadatables.configuration.BoxEntryConfiguration;
import es.amplia.oda.service.scadatables.configuration.ScadaTableEntryConfiguration;

import es.amplia.oda.service.scadatables.configuration.ScadaTablesConfigurationHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ScadaTableInfoService.class)
public class ScadaTableInfoServiceTest {

    private ScadaTableInfoService testScadaTableInfoService;

    @Before
    public void setUp() throws ScriptException {
        Map< Map<Integer,String>, ScadaTableEntryConfiguration> scadaTablesConfiguration = new HashMap<>();

        ScadaTableEntryConfiguration entryBox = new BoxEntryConfiguration(ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME,
                "booxlean", "deviceId", "feed");
        // register script
        String script = "x*10";
        final ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        engine.eval(ScadaTablesConfigurationHandler.REVERSE_ENDIAN_FUNCTION + "\r\n function run(x) { return " + script + "; }");
        entryBox.setScript((Invocable) engine);
        // add entry to scadaTables
        scadaTablesConfiguration.put(Collections.singletonMap(10, ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME), entryBox);

        ScadaTableEntryConfiguration entryBox2 = new BoxEntryConfiguration("M_ME_NC_1", "testDatastreamId",
                null, null);
        scadaTablesConfiguration.put(Collections.singletonMap(11, "M_ME_NC_1"), entryBox2);

        testScadaTableInfoService = new ScadaTableInfoService();
        testScadaTableInfoService.loadConfiguration(scadaTablesConfiguration);
    }

    @Test
    public void testGetNumBinaryInputs() {
        int binaryInputs = testScadaTableInfoService.getNumBinaryInputs();

        assertEquals(1, binaryInputs);
    }

    @Test
    public void testGetNumAnalogInputs() {
        int analogInputs = testScadaTableInfoService.getNumAnalogInputs();

        assertEquals(0, analogInputs);
    }

    @Test
    public void testGetNumDoubleBinaryInputs() {
        int doubleBinaryInputs = testScadaTableInfoService.getNumDoubleBinaryInputs();

        assertEquals(0, doubleBinaryInputs);
    }

    @Test
    public void testGetNumCounters() {
        int counters = testScadaTableInfoService.getNumCounters();

        assertEquals(0, counters);
    }

    @Test
    public void testGetNumFrozenCounters() {
        int frozenCounters = testScadaTableInfoService.getNumFrozenCounters();

        assertEquals(0, frozenCounters);
    }

    @Test
    public void testGetNumBinaryOutputs() {
        int binaryOutputs = testScadaTableInfoService.getNumBinaryOutputs();

        assertEquals(0, binaryOutputs);
    }

    @Test
    public void testGetNumAnalogOutputs() {
        int analogOutputs = testScadaTableInfoService.getNumAnalogOutputs();

        assertEquals(0, analogOutputs);
    }

    @Test
    public void testTranslateBox() {
        DatastreamInfo dsinfo = new DatastreamInfo("deviceId", "booxlean");
        ScadaTableTranslator.ScadaInfo scinfo = testScadaTableInfoService.translate(dsinfo);

        assertEquals(ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME, scinfo.getType());
        assertEquals(10, scinfo.getIndex());

        DatastreamInfo dsinfoNull = new DatastreamInfo("notExists", "notExists");
        ScadaTableTranslator.ScadaInfo scinfoNull = testScadaTableInfoService.translate(dsinfoNull);
        Assert.assertNull(scinfoNull);

    }

    @Test
    public void testGetTranslationInfo() {
        ScadaTableTranslator.ScadaInfo scadaInfo = new ScadaTableTranslator.ScadaInfo(10, ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME);
        ScadaTableTranslator.ScadaTranslationInfo scinfo = testScadaTableInfoService.getTranslationInfo(scadaInfo);

        assertEquals("booxlean", scinfo.getDatastreamId());
        assertEquals("deviceId", scinfo.getDeviceId());
        assertEquals("feed", scinfo.getFeed());

        ScadaTableTranslator.ScadaInfo scadaInfoNull = new ScadaTableTranslator.ScadaInfo(36, ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME);
        ScadaTableTranslator.ScadaTranslationInfo scinfoNull = testScadaTableInfoService.getTranslationInfo(scadaInfoNull);
        Assert.assertNull(scinfoNull);

    }

    @Test
    public void testGetDatastremasIds() {
        List<String> dsIds = testScadaTableInfoService.getDatastreamsIds();

        assertEquals(2, dsIds.size());
        assertTrue(dsIds.contains("booxlean"));
        assertTrue(dsIds.contains("testDatastreamId"));
    }

    @Test
    public void testGetDeviceIds() {
        List<String> deviceIds = testScadaTableInfoService.getDeviceIds();

        assertEquals(1, deviceIds.size());
        assertTrue(deviceIds.contains("deviceId"));
        assertFalse(deviceIds.contains(null));
    }

    @Test
    public void testTransformValue() {
        Object transformedValue = testScadaTableInfoService.transformValue(10,
                ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME, 2);
        assertEquals(20.0, transformedValue);
    }

    @Test
    public void testTransformValueNoScript() {
        // if there is no script assigned, it must return the same value as input
        Object transformedValue = testScadaTableInfoService.transformValue(11, "M_ME_NC_1", 2);
        assertEquals(2, transformedValue);
    }

}
