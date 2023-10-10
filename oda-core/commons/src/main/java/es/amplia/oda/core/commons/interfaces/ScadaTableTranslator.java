package es.amplia.oda.core.commons.interfaces;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;

import java.util.List;

import es.amplia.oda.core.commons.utils.DatastreamInfo;
import lombok.Value;

/**
 * SCADA translator to translate from Datastream info {device id, datastream id} to SCADA info {index, SCADA type}.
 */
public interface ScadaTableTranslator {

    @Value
    class ScadaInfo {
        int index;
        Object type;
    }
    
    /**
     * translate from the given Datastream info (device id and datastream id) to SCADA info (index and type).
     * @param info Datastream info to translate.
     * @param isEvent indicates if the signal is an event or not
     * @return Correspondent SCADA info.
     * @throws DataNotFoundException Data with the given parameters was not found.
     */
    ScadaInfo translate(DatastreamInfo info, boolean isEvent);

    /**
     * Transform value using the associated script
     *
     * @param address address info
     * @param type    type info
     * @param isEvent indicates if the signal is an event or not
     * @param value   value to apply script to
     * @return value transformed
     */
    Object transformValue(int address, Object type, boolean isEvent, Object value);

    @Value
    class ScadaTranslationInfo {
        String deviceId;
        String datastreamId;
        String feed;
    }

    ScadaTranslationInfo getTranslationInfo(ScadaInfo scadaInfo, boolean isEvent);

	List<String> getRecollectionDatastreamsIds();

    List<String> getRecollectionDeviceIds();
}
