package es.amplia.oda.service.scadatables.internal;

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

    // we will have two scadaTables
    // one will save the relation between ASDUS and datastream info for signals that are events (spontaneous)
    // one will save the relation between ASDUS and datastream info for signals that are recollected the normal way (interrogation command)
    // map is <<address, type>, scadaSignalInfo>
    private Map< Map<Integer,String>, ScadaTableEntryConfiguration> scadaTablesRecollection = new HashMap<>();
    private Map< Map<Integer,String>, ScadaTableEntryConfiguration> scadaTablesEvents = new HashMap<>();

    public ScadaTableInfoService() {
    }

    public void loadConfiguration(Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTablesRecollection
            , Map<Map<Integer, String>, ScadaTableEntryConfiguration> scadaTablesEvents) {
        this.scadaTablesRecollection = scadaTablesRecollection;
        this.scadaTablesEvents = scadaTablesEvents;
    }

    @Override
    public int getNumBinaryInputs() {
        return getNumberOfEntriesOfDataType(ScadaTableEntryConfiguration.BINARY_INPUT_TYPE_NAME);
    }

    private int getNumberOfEntriesOfDataType(String dataType) {

        int numberOfEntries = 0;

        for (Map.Entry<Map<Integer, String>, ScadaTableEntryConfiguration> map : scadaTablesRecollection.entrySet()) {
            if (map.getValue().getDataType().equals(dataType)) {
                numberOfEntries++;
            }
        }

        for (Map.Entry<Map<Integer, String>, ScadaTableEntryConfiguration> map : scadaTablesEvents.entrySet()) {
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
    public ScadaInfo translate(DatastreamInfo datastreamInfo, boolean isEvent) {
        return getBoxIndex(datastreamInfo, isEvent);
    }

    private ScadaInfo getBoxIndex(DatastreamInfo datastreamInfo, boolean isEvent) {

        Set<Map.Entry<Map<Integer, String>, ScadaTableEntryConfiguration>> entries;

        if (isEvent) {
            entries = scadaTablesEvents.entrySet();
        } else {
            entries = scadaTablesRecollection.entrySet();
        }

        for (Map.Entry<Map<Integer, String>, ScadaTableEntryConfiguration> map : entries) {
            for (Map.Entry<Integer, String> addressAsduMap : map.getKey().entrySet()) {
                ScadaTableEntryConfiguration entryValue = map.getValue();
                // check that datastreamId is the same
                if (entryValue.getDatastreamId().equals(datastreamInfo.getDatastreamId())) {
                    // check that deviceId is the same or is null the one in the entry
                    // deviceId in entry is null when there is no device defined in scadatables
                    // in this case, the deviceId in the translation info is the deviceId from the physical connection
                    if ( entryValue.getDeviceId() == null || entryValue.getDeviceId().equals(datastreamInfo.getDeviceId())) {
                        return new ScadaInfo(addressAsduMap.getKey(), entryValue.getDataType());
                    }
                }
            }
        }

        LOGGER.info("DatasteamId {} for deviceId {} not found in SCADA tables", datastreamInfo.getDatastreamId(), datastreamInfo.getDeviceId());
        return null;
    }

    private boolean isOutputType(ScadaTableEntryConfiguration entryConfiguration) {
        return entryConfiguration.getDataType().equals(ScadaTableEntryConfiguration.BINARY_OUTPUT_TYPE_NAME)
                || entryConfiguration.getDataType().equals(ScadaTableEntryConfiguration.ANALOG_OUTPUT_TYPE_NAME);
    }

    public Object transformValue(int address, Object type, boolean isEvent, Object value) {
        Invocable script = null;

        Map<Integer, String> addressAsduMap = Collections.singletonMap(address, type.toString());
        ScadaTableEntryConfiguration scadaEntry;

        if (isEvent) {
            scadaEntry = scadaTablesEvents.get(addressAsduMap);
        } else {
            scadaEntry = scadaTablesRecollection.get(addressAsduMap);
        }

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
    public ScadaTranslationInfo getTranslationInfo(ScadaInfo scadaInfo, boolean isEvent) {
        ScadaTableEntryConfiguration scadaEntry;

        if (isEvent) {
            scadaEntry = scadaTablesEvents.get(Collections.singletonMap(scadaInfo.getIndex(), scadaInfo.getType()));
        } else {
            scadaEntry = scadaTablesRecollection.get(Collections.singletonMap(scadaInfo.getIndex(), scadaInfo.getType()));
        }

        if (scadaEntry != null) {
            return new ScadaTranslationInfo(scadaEntry.getDeviceId(), scadaEntry.getDatastreamId(),
                    scadaEntry.getFeed());
        }

        return null;
    }

    @Override
    public List<String> getRecollectionDatastreamsIds() {
        List<String> ret = new ArrayList<>();
        scadaTablesRecollection.forEach((key, value) -> {
            if (value.getDatastreamId() != null) {
                ret.add(value.getDatastreamId());
            }
        });
        return ret;
    }

    @Override
    public List<String> getRecollectionDeviceIds() {
        List<String> ret = new ArrayList<>();
        scadaTablesRecollection.forEach((key, value) -> {
            if (value.getDeviceId() != null) {
                ret.add(value.getDeviceId());
            }
        });
        return ret;
    }
}
