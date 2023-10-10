package es.amplia.oda.service.scadatables.configuration;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Box variable configuration information.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BoxEntryConfiguration extends ScadaTableEntryConfiguration {

    /**
     * Constructor.
     *
     * @param dataType     Data type.
     * @param datastreamId Datastream identifier.
     * @param deviceId     Device identifier.
     * @param feed         Feed identifier.
     * @param isEvent      Indicates if it is an event or an interrogation command recollection response
     */
    public BoxEntryConfiguration(String dataType, String datastreamId, String deviceId, String feed, boolean isEvent) {
        super(dataType, datastreamId, deviceId, feed, null, null, isEvent);
    }
}
