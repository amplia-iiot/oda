package es.amplia.oda.hardware.snmp.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SnmpClientV3Options {

    // needed for snmpV3
    String securityName;
    String authPassphrase;
    String privPassphrase;
    String contextName;
    String authProtocol;
    String privacyProtocol;
}
