package es.amplia.oda.hardware.modbus;

import es.amplia.oda.core.commons.countermanager.CounterManager;
import es.amplia.oda.core.commons.countermanager.Counters;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ModbusCounters extends Counters {

    private static CounterManager counterManager;

    public enum ModbusCounterType {
        MODBUS_REGISTER_READ_OK("MODBUS/READ/OK/device/type"),
        MODBUS_REGISTER_READ_FAILED("MODBUS/READ/FAILED/device/type"),
        MODBUS_REGISTER_WRITE_OK("MODBUS/WRITE/OK/device/type"),
        MODBUS_REGISTER_WRITE_FAILED("MODBUS/WRITE/FAILED/device/type");

        private final String m_name;

        ModbusCounterType(String _nameString) {
            m_name = _nameString;
        }

        public String getCounterString(ModbusType type, String deviceId) {

            String res = m_name;

            if (type != null) {
                res = res.replaceAll("type", String.valueOf(type));
            } else {
                log.warn("Type is null");
            }

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

    public ModbusCounters(CounterManager _counterManager) {
        counterManager = _counterManager;
    }

    public static void incrCounter(ModbusCounterType counter, ModbusType type, String deviceId, int number) {
        if (counterManager == null) {
            log.warn("Counter manager is null. No counters will be registered");
            return;
        }
        counterManager.incrementCounter(counter.getCounterString(type, deviceId), number);
    }
}
