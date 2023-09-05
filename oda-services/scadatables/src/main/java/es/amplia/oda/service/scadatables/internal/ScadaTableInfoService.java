package es.amplia.oda.service.scadatables.internal;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.service.scadatables.configuration.BoxEntryConfiguration;
import es.amplia.oda.service.scadatables.configuration.ScadaTableEntryConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScadaTableInfoService implements ScadaTableInfo, ScadaTableTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScadaTableInfoService.class);

    private Map<ScadaTableEntryConfiguration, Integer> scadaTablesConfiguration = new HashMap<>();


    public ScadaTableInfoService() {
    }

    public void loadConfiguration(Map<ScadaTableEntryConfiguration, Integer> scadaTablesConfiguration) {
        this.scadaTablesConfiguration = scadaTablesConfiguration;
    }

    @Override
    public int getNumBinaryInputs() {
        return getNumberOfEntriesOfDataType(ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME);
    }

    private int getNumberOfEntriesOfDataType(String dataType) {
        return (int) scadaTablesConfiguration.keySet()
                .stream()
                .filter(entry -> entry.getDataType().equals(dataType))
                .count();
    }

    @Override
    public int getNumDoubleBinaryInputs() {
        return getNumberOfEntriesOfDataType(ScadaTableEntryConfiguration.DOUBLE_BINARY_INPUT_TYPE_NAME);
    }

    @Override
    public int getNumAnalogInputs() {
        return getNumberOfEntriesOfDataType(ScadaTableEntryConfiguration.ANALOG_INPUT_TYPE_NAME);
    }

    @Override
    public int getNumCounters() {
        return getNumberOfEntriesOfDataType(ScadaTableEntryConfiguration.COUNTER_TYPE_NAME);
    }

    @Override
    public int getNumFrozenCounters() {
        return getNumberOfEntriesOfDataType(ScadaTableEntryConfiguration.FROZEN_COUNTER_TYPE_NAME);
    }

    @Override
    public int getNumBinaryOutputs() {
        return getNumberOfEntriesOfDataType(ScadaTableEntryConfiguration.BINARY_OUTPUT_TYPE_NAME);
    }

    @Override
    public int getNumAnalogOutputs() {
        return getNumberOfEntriesOfDataType(ScadaTableEntryConfiguration.ANALOG_OUTPUT_TYPE_NAME);
    }

    @Override
    public ScadaInfo translate(DatastreamInfo datastreamInfo) {
        return getBoxIndex(datastreamInfo);
    }

    private ScadaInfo getBoxIndex(DatastreamInfo datastreamInfo) {
        String datastreamId = datastreamInfo.getDatastreamId();
        return scadaTablesConfiguration.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof BoxEntryConfiguration)
                .filter(entry -> !isOutputType(entry.getKey()))
                .filter(entry -> entry.getKey().getDatastreamId().equals(datastreamId))
                .findFirst()
                .map(entry -> new ScadaInfo(entry.getValue(), entry.getKey().getDataType(),
                        datastreamInfo.getValue(), entry.getKey().getScript()))
                .orElseThrow(() -> new DataNotFoundException(datastreamId + " not found in SCADA tables"));
    }

    private boolean isOutputType(ScadaTableEntryConfiguration entryConfiguration) {
        return entryConfiguration.getDataType().equals(ScadaTableEntryConfiguration.BINARY_OUTPUT_TYPE_NAME)
                || entryConfiguration.getDataType().equals(ScadaTableEntryConfiguration.ANALOG_OUTPUT_TYPE_NAME);
    }

    public Object transformValue(Invocable script, Object value) {
        try {
            return (script != null ? script.invokeFunction("run", value) : value);
        } catch (ScriptException | NoSuchMethodException e) {
            LOGGER.error("Scada collected value cannot be transformed, returning value without executing script: {}", value);
            return value;
        }
    }

    @Override
    public DatastreamInfo getDatastreamInfo(ScadaInfo scadaInfo) {
        return scadaTablesConfiguration.entrySet().stream()
                .filter(entry -> isOutputType(entry.getKey()))
                .filter(entry -> entry.getValue() == scadaInfo.getIndex())
                .map(entry -> new DatastreamInfo("", entry.getKey().getDatastreamId(), scadaInfo.getValue()))
                .findFirst()
                .orElseThrow(() -> new DataNotFoundException("Can not found Analog Output with index " + scadaInfo.getIndex()));
    }

    @Override
    public List<String> getDatastreamsIds() {
        List<String> ret = new ArrayList<>();
        scadaTablesConfiguration.entrySet().stream().forEach(entry -> ret.add(entry.getKey().getDatastreamId()));
        return ret;
    }
}
