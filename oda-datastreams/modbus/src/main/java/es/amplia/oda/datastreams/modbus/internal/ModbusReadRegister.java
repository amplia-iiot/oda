package es.amplia.oda.datastreams.modbus.internal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModbusReadRegister {
    long at;
    Object[] value;
}
