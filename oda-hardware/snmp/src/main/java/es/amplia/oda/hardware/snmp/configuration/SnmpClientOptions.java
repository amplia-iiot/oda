package es.amplia.oda.hardware.snmp.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SnmpClientOptions {

    // needed for snmp v1 and v2
    String community;
}
