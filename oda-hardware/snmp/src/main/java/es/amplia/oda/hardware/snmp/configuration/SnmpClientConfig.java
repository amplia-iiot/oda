package es.amplia.oda.hardware.snmp.configuration;

import lombok.Data;

@Data
public class SnmpClientConfig {
    String deviceId;
    String ip;
    int port;
    int listenPort;
    int version;
    int retries = 2;
    int timeout = 30000;

    // needed for snmp v1 and v2
    SnmpClientOptions options;

    // needed for snmpV3
    SnmpClientV3Options v3Options;

    public SnmpClientConfig(String deviceId, String ip, int port, int listenPort, int version, SnmpClientOptions options) {
        this.deviceId = deviceId;
        this.port = port;
        this.listenPort = listenPort;
        this.ip = ip;
        this.version = version;
        this.options = options;
    }

    public SnmpClientConfig(String deviceId, String ip, int port, int listenPort, int version, SnmpClientV3Options options) {
        this.deviceId = deviceId;
        this.port = port;
        this.listenPort = listenPort;
        this.ip = ip;
        this.version = version;
        this.v3Options = options;
    }
}
