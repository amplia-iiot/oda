package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;

import lombok.Value;

/**
 * SCADA translator to translate from Datastream info {device id, datastream id} to SCADA info {index, SCADA type).
 */
public interface ScadaTableTranslator {

	@Value
	class ScadaInfo {
		private int index;
		private Object type;
		private Object value;
	}
	
    /**
     * translate from the given Datastream info (device id and datastream id) to SCADA info (index and type).
     * @param info Datastream info to translate.
     * @return Correspondent SCADA info.
     * @throws DataNotFoundException Data with the given parameters was not found.
     */
    ScadaInfo translate(DatastreamInfo info) throws DataNotFoundException;

    @Value
    class DatastreamInfo {
        private String deviceId;
        private String datastreamId;
        private Object value;
    }

    DatastreamInfo getDatastreamInfo(ScadaInfo info) throws DataNotFoundException;
}
