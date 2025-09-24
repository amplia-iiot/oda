package es.amplia.oda.core.commons.snmp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SnmpEntry {
    private String OID;
    private String dataType;
    private String datastreamId;
    private String deviceId;
    private String feed;
}
