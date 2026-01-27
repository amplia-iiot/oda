package es.amplia.oda.core.commons.snmp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SnmpEntry {

    public static final String EVENT_PUBLISH_TYPE_DISPATCHER = "dispatcher";
    public static final String EVENT_PUBLISH_TYPE_STATEMANAGER = "statemanager";

    private String OID;
    private String dataType;
    private String datastreamId;
    private String deviceId;
    private String feed;

    /**
     * Indicates the way to publish the events
     * dispatcher means that the event will be published as soon as it is received (can't apply rules)
     * statemanager means that the event will pass to the stateManager and will be published by the collector (rules can be applied)
     */
    private String eventPublishType;
}
