package es.amplia.oda.hardware.snmp;

import es.amplia.oda.core.commons.countermanager.CounterManager;
import es.amplia.oda.core.commons.countermanager.Counters;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnmpCounters extends Counters {

    private static CounterManager counterManager;

    // Counters constants
    public enum SnmpCounterType {
        SNMP_RECEIVED_EVENT("SNMP/RECEIVED/EVENT/device"),
        SNMP_GET_OK("SNMP/GET/OK/device"),
        SNMP_GET_ERROR("SNMP/GET/ERROR/device"),
        SNMP_SET_OK("SNMP/SET/OK/device"),
        SNMP_SET_ERROR("SNMP/SET/ERROR/device"),
        ;

        private final String m_name;

        SnmpCounterType(String _nameString) {
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

    public SnmpCounters(CounterManager _counterManager) {
        counterManager = _counterManager;
    }

    public static void incrCounter(SnmpCounterType counter, String deviceId, int number) {
        if (counterManager == null) {
            log.warn("Counter manager is null. No counters will be registered");
            return;
        }
        counterManager.incrementCounter(counter.getCounterString(deviceId), number);
    }
    
}
