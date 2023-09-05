package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;

import java.util.List;

import lombok.Value;

import javax.script.Invocable;

/**
 * SCADA translator to translate from Datastream info {device id, datastream id} to SCADA info {index, SCADA type).
 */
public interface ScadaTableTranslator {

    @Value
    class ScadaInfo {
        private int index;
        private Object type;
        private Object value;
        private Invocable script;
    }
    
    /**
     * translate from the given Datastream info (device id and datastream id) to SCADA info (index and type).
     * @param info Datastream info to translate.
     * @return Correspondent SCADA info.
     * @throws DataNotFoundException Data with the given parameters was not found.
     */
    ScadaInfo translate(DatastreamInfo info);

    /**
     * Transform value using the indicated script
     * @param script script to apply to value
     * @param value value to apply script to
     * @return value transformed
     */
    Object transformValue(Invocable script, Object value);

    @Value
    class DatastreamInfo {
        private String deviceId;
        private String datastreamId;
        private Object value;
    }

    DatastreamInfo getDatastreamInfo(ScadaInfo info);
	
	List<String> getDatastreamsIds();

}
