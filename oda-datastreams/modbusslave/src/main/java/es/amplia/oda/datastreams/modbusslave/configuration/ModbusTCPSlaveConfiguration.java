package es.amplia.oda.datastreams.modbusslave.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModbusTCPSlaveConfiguration {

    String ipAddress;
    int listenPort;
    String deviceId;
    int slaveAddress;
}
