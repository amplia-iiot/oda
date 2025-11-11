package es.amplia.oda.datastreams.modbusslave;

import es.amplia.oda.core.commons.countermanager.CounterManager;
import es.amplia.oda.core.commons.countermanager.Counters;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusSlaveCounters extends Counters {

    private static CounterManager counterManager;

    public enum ModbusCounterType {
        MODBUS_RECEIVED_WRITE_COIL("MODBUS/RECEIVED/WRITE/COIL/device"),
        MODBUS_RECEIVED_WRITE_COILS("MODBUS/RECEIVED/WRITE/COILS/device"),
        MODBUS_RECEIVED_WRITE_REGISTER("MODBUS/RECEIVED/WRITE/REGISTER/device"),
        MODBUS_RECEIVED_WRITE_REGISTERS("MODBUS/RECEIVED/WRITE/REGISTERS/device");

        private final String m_name;

        ModbusCounterType(String _nameString) {
            m_name = _nameString;
        }

        public String getCounterString(String deviceId) {

            String res = m_name;

            if (deviceId != null) {
                res = res.replaceAll("device", deviceId);
            } else {
                log.warn("DeviceId is null");
            }

            if (log.isTraceEnabled()) {
                log.trace("counter string retrieved: {}", res);
            }

            return res;
        }
    }

    public ModbusSlaveCounters(CounterManager _counterManager) {
        counterManager = _counterManager;
    }

    public static void incrCounter(ModbusCounterType counter, String deviceId, int number) {
        if (counterManager == null) {
            log.warn("Counter manager is null. No counters will be registered");
            return;
        }
        counterManager.incrementCounter(counter.getCounterString(deviceId), number);
    }
}
