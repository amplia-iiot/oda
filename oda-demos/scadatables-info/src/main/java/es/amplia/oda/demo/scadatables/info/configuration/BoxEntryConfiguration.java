package es.amplia.oda.demo.scadatables.info.configuration;

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
     */
    public BoxEntryConfiguration(String dataType, String datastreamId) {
        super(dataType, datastreamId);
    }
}
