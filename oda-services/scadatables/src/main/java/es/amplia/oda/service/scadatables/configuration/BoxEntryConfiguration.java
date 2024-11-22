package es.amplia.oda.service.scadatables.configuration;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Box variable configuration information.
 */
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
     * @param eventPublishing Indicates if it is an event and the way it will be published
     */
    public BoxEntryConfiguration(String dataType, String datastreamId, String deviceId, String feed, String eventPublishing) {
        super(dataType, datastreamId, deviceId, feed, null, null, eventPublishing);
    }
}
