package es.amplia.oda.datastreams.snmp.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SnmpDatastreamsEntry {
    private String OID;
    private String dataType;
    private String datastreamId;
    private String deviceId;
    private String feed;
}
