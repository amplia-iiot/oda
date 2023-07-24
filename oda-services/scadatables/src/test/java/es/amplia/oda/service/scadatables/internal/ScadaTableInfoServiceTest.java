package es.amplia.oda.service.scadatables.internal;

import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.service.scadatables.configuration.BoxEntryConfiguration;
import es.amplia.oda.service.scadatables.configuration.ScadaTableEntryConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ScadaTableInfoService.class)
public class ScadaTableInfoServiceTest {
    private ScadaTableInfoService testScadaTableInfoService;

    @Before
    public void setUp() {
        Map<ScadaTableEntryConfiguration, Integer> scadaTablesConfiguration = new HashMap<>();
        ScadaTableEntryConfiguration entryBox =
                new BoxEntryConfiguration(
                        ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME,
                        "booxlean"
                );
        scadaTablesConfiguration.put(entryBox, 10);

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
        ScadaTableTranslator.DatastreamInfo dsinfo = new ScadaTableTranslator.DatastreamInfo("", "booxlean", true);

        ScadaTableTranslator.ScadaInfo scinfo = testScadaTableInfoService.translate(dsinfo);

        assertTrue((Boolean) scinfo.getValue());
        assertEquals(ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME, scinfo.getType());
    }

    @Test
    public void testGetDatastremasIds() {
        List<String> dsIds = testScadaTableInfoService.getDatastreamsIds();
        String[] expected = new String[]{"booxlean"};
        assertArrayEquals(expected, dsIds.toArray());
    }

}
