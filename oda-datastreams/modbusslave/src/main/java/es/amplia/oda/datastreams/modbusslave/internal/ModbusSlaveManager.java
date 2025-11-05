package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.ModbusException;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.datastreams.modbusslave.configuration.ModbusSlaveConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbusslave.configuration.ModbusTCPSlaveConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModbusSlaveManager implements AutoCloseable {

    private final StateManager stateManager;
    List<ModbusTCPSlaveImpl> tcpModbusSlaves = new ArrayList<>();

    public ModbusSlaveManager(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    public void loadConfiguration(Map<String, List<Object>> modbusSlaves) {
        // load tcp slaves
        List<Object> tcpModbusSlaves = modbusSlaves.get(ModbusSlaveConfigurationUpdateHandler.TCP_MODBUS_TYPE);
        if (tcpModbusSlaves == null) {
            return;
        }

        for (int i = 0; i < tcpModbusSlaves.size(); i++) {
            ModbusTCPSlaveConfiguration conf = (ModbusTCPSlaveConfiguration) tcpModbusSlaves.get(i);
            try {
                ModbusTCPSlaveImpl slave = new ModbusTCPSlaveImpl(conf.getDeviceId(), conf.getIpAddress(),
                        conf.getListenPort(), conf.getSlaveAddress(), this.stateManager);
                slave.open();
                tcpModbusSlaves.add(slave);
            } catch (ModbusException e) {
                log.error("Error creating modbus slave with conf {} : ", conf, e);
            }
        }
    }

    @Override
    public void close() {
        for (ModbusTCPSlaveImpl slave : tcpModbusSlaves) {
            slave.close();
        }
    }
}
