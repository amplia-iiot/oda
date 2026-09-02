package es.amplia.oda.service.scadatables.internal;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.service.scadatables.configuration.BoxEntryConfiguration;
import es.amplia.oda.service.scadatables.configuration.ScadaTableEntryConfiguration;

import es.amplia.oda.service.scadatables.configuration.ScadaTablesConfigurationHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ScadaTableInfoServiceTest {

    private ScadaTableInfoService testScadaTableInfoService;

    @BeforeEach
    public void setUp() throws ScriptException {
        Map< Map<Integer,String>, ScadaTableEntryConfiguration> scadaTablesRecollection = new HashMap<>();
        Map< Map<Integer,String>, ScadaTableEntryConfiguration> scadaTablesEvents = new HashMap<>();

        ScadaTableEntryConfiguration entryBox = new BoxEntryConfiguration(ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME,
                "booxlean", "deviceId", "feed", "dispatcher");

        // register script
        String script = "x*10";
        ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
        engine.eval(ScadaTablesConfigurationHandler.REVERSE_ENDIAN_FUNCTION + "\r\n function run(x) { return " + script + "; }");
        entryBox.setScript((Invocable) engine);

        // add entry to scadaTables
        scadaTablesRecollection.put(Collections.singletonMap(10, ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME), entryBox);

        ScadaTableEntryConfiguration entryBox2 = new BoxEntryConfiguration("M_ME_NC_1", "testDatastreamId",
                null, null, null);
        scadaTablesRecollection.put(Collections.singletonMap(11, "M_ME_NC_1"), entryBox2);

        ScadaTableEntryConfiguration entryBox3 = new BoxEntryConfiguration(ScadaTableEntryConfiguration.ANALOG_INPUT_TYPE_NAME,
                "testDatastreamId2", null, null, ScadaTableEntryConfiguration.EVENT_PUBLISH_DISPATCHER);
        scadaTablesEvents.put(Collections.singletonMap(12, ScadaTableEntryConfiguration.ANALOG_INPUT_TYPE_NAME), entryBox3);

        testScadaTableInfoService = new ScadaTableInfoService();
        testScadaTableInfoService.loadConfiguration(scadaTablesRecollection, scadaTablesEvents);
    }

    @Test
    public void testGetNumBinaryInputs() {
        int binaryInputs = testScadaTableInfoService.getNumBinaryInputs();

        assertEquals(1, binaryInputs);
    }

    @Test
    public void testGetNumAnalogInputs() {
        int analogInputs = testScadaTableInfoService.getNumAnalogInputs();

        assertEquals(1, analogInputs);
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
        ScadaTableTranslator.ScadaInfo scinfo = testScadaTableInfoService.translate(dsinfo, false);

        assertEquals(ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME, scinfo.getType());
        assertEquals(10, scinfo.getIndex());

        // same datastreamId and deviceId but search for it in events scada table
        ScadaTableTranslator.ScadaInfo scinfoEvent = testScadaTableInfoService.translate(dsinfo, true);
        Assertions.assertNull(scinfoEvent);

        // datastreamInfo doesn't exist in scada table
        DatastreamInfo dsinfoNull = new DatastreamInfo("notExists", "notExists");
        ScadaTableTranslator.ScadaInfo scinfoNull = testScadaTableInfoService.translate(dsinfoNull, false);
        Assertions.assertNull(scinfoNull);
    }

    @Test
    public void testGetTranslationInfo() {
        ScadaTableTranslator.ScadaInfo scadaInfo = new ScadaTableTranslator.ScadaInfo(10,
                ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME);
        ScadaTableTranslator.ScadaTranslationInfo scinfo = testScadaTableInfoService.getTranslationInfo(scadaInfo,
                false);

        assertEquals("booxlean", scinfo.getDatastreamId());
        assertEquals("deviceId", scinfo.getDeviceId());
        assertEquals("feed", scinfo.getFeed());

        // same scada info but search in events table
        ScadaTableTranslator.ScadaTranslationInfo scinfoEvent = testScadaTableInfoService.getTranslationInfo(scadaInfo,
                true);
        Assertions.assertNull(scinfoEvent);

        // scada info to search for doesn't exist in tables
        ScadaTableTranslator.ScadaInfo scadaInfoNull = new ScadaTableTranslator.ScadaInfo(36,
                ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME);
        ScadaTableTranslator.ScadaTranslationInfo scinfoNull = testScadaTableInfoService.getTranslationInfo(scadaInfoNull, false);
        Assertions.assertNull(scinfoNull);

    }

    @Test
    public void testGetDatastremasIds() {
        List<String> dsIds = testScadaTableInfoService.getRecollectionDatastreamsIds();

        assertEquals(2, dsIds.size());
        assertTrue(dsIds.contains("booxlean"));
        assertTrue(dsIds.contains("testDatastreamId"));
    }

    @Test
    public void testGetDeviceIds() {
        List<String> deviceIds = testScadaTableInfoService.getRecollectionDeviceIds();

        assertEquals(1, deviceIds.size());
        assertTrue(deviceIds.contains("deviceId"));
        assertFalse(deviceIds.contains(null));
    }

    @Test
    public void testTransformValue() {
        Object transformedValue = testScadaTableInfoService.transformValue(10,
                ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME, false, 2);
        assertEquals(20.0, transformedValue);

        // same values but search for it in events table
        Object transformedValueEvent = testScadaTableInfoService.transformValue(10,
                ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME, true, 2);
        assertEquals(2, transformedValueEvent);
    }

    @Test
    public void testTransformValueNoScript() {
        // if there is no script assigned, it must return the same value as input
        Object transformedValue = testScadaTableInfoService.transformValue(11, "M_ME_NC_1", false, 2);
        assertEquals(2, transformedValue);
    }

    @Test
    public void testEventPublishTypeNotValid() {
        assertThrows(ConfigurationException.class, () -> new BoxEntryConfiguration(ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME, "booxlean",
                "deviceId", "feed", "notValid"));
    }

    @Test
    public void testEventPublishTypeIgnoreCase() {
        new BoxEntryConfiguration(ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME, "booxlean",
                "deviceId", "feed", "DiSpaTcher");
        new BoxEntryConfiguration(ScadaTableEntryConfiguration.ANALOG_INPUT_TYPE_NAME, "booxlean",
                "deviceId", "feed", "StaTemAnaGer");
    }
}
