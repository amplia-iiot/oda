package es.amplia.oda.service.scadatables.internal;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.service.scadatables.configuration.ScadaTableEntryConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptException;

import java.util.*;

public class ScadaTableInfoService implements ScadaTableInfo, ScadaTableTranslator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScadaTableInfoService.class);

    private Map< Map<Integer,String>, ScadaTableEntryConfiguration> scadaTablesConfiguration = new HashMap<>();


    public ScadaTableInfoService() {
    }

    public void loadConfiguration(Map< Map<Integer,String>, ScadaTableEntryConfiguration> scadaTablesConfiguration) {
        this.scadaTablesConfiguration = scadaTablesConfiguration;
    }

    @Override
    public int getNumBinaryInputs() {
        return getNumberOfEntriesOfDataType(ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME);
    }

    private int getNumberOfEntriesOfDataType(String dataType) {

        int numberOfEntries = 0;

        for (Map.Entry<Map<Integer, String>, ScadaTableEntryConfiguration> map : scadaTablesConfiguration.entrySet()) {
            if (map.getValue().getDataType().equals(dataType)) {
                numberOfEntries++;
            }
        }

        return numberOfEntries;
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
        for (Map.Entry<Map<Integer, String>, ScadaTableEntryConfiguration> map : scadaTablesConfiguration.entrySet()) {
            ScadaTableEntryConfiguration entry = map.getValue();
            for (Map.Entry<Integer, String> addressAsduMap : map.getKey().entrySet()) {
                if (entry.getDatastreamId().equals(datastreamInfo.getDatastreamId())) {
                    return new ScadaInfo(addressAsduMap.getKey(), entry.getDataType());
                }
            }
        }

        throw new DataNotFoundException(datastreamInfo.getDatastreamId() + " not found in SCADA tables");
    }

    private boolean isOutputType(ScadaTableEntryConfiguration entryConfiguration) {
        return entryConfiguration.getDataType().equals(ScadaTableEntryConfiguration.BINARY_OUTPUT_TYPE_NAME)
                || entryConfiguration.getDataType().equals(ScadaTableEntryConfiguration.ANALOG_OUTPUT_TYPE_NAME);
    }

    public Object transformValue(int address, Object type, Object value) {
        Invocable script = null;

        Map<Integer, String> addressAsduMap = Collections.singletonMap(address, type.toString());
        ScadaTableEntryConfiguration scadaEntry = scadaTablesConfiguration.get(addressAsduMap);

        if (scadaEntry != null) {
            script = scadaEntry.getScript();
        }

        try {
            return (script != null ? script.invokeFunction("run", value) : value);
        } catch (ScriptException | NoSuchMethodException e) {
            LOGGER.error("Scada collected value cannot be transformed, returning value without executing script: {}", value);
            return value;
        }
    }

    @Override
    public ScadaTranslationInfo getTranslationInfo(ScadaInfo scadaInfo) {

        ScadaTableEntryConfiguration scadaEntry = scadaTablesConfiguration.get(
                Collections.singletonMap(scadaInfo.getIndex(), scadaInfo.getType()));

        if (scadaEntry != null) {
            return new ScadaTranslationInfo(scadaEntry.getDeviceId(), scadaEntry.getDatastreamId(),
                    scadaEntry.getFeed());
        }

        throw new DataNotFoundException("Can not found Analog Output with index " + scadaInfo.getIndex());
    }


    @Override
    public List<String> getDatastreamsIds() {
        List<String> ret = new ArrayList<>();
        scadaTablesConfiguration.forEach((key, value) -> ret.add(value.getDatastreamId()));
        return ret;
    }
}
