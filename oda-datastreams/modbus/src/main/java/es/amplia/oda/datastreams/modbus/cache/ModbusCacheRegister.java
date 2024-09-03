package es.amplia.oda.datastreams.modbus.cache;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModbusCacheRegister {
    long at;
    Object register;
}
