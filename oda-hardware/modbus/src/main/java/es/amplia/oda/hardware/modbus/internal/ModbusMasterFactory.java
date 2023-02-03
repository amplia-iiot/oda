package es.amplia.oda.hardware.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.hardware.modbus.configuration.SerialModbusConfiguration;
import es.amplia.oda.hardware.modbus.configuration.TCPModbusMasterConfiguration;
import es.amplia.oda.hardware.modbus.configuration.UDPModbusMasterConfiguration;

import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.facade.ModbusUDPMaster;
import com.ghgande.j2mod.modbus.util.SerialParameters;

public class ModbusMasterFactory {

    public ModbusMaster createTCPModbusMaster(TCPModbusMasterConfiguration conf) {
        ModbusTCPMaster modbusTCPMaster =
                new ModbusTCPMaster(conf.getAddress(), conf.getPort(), conf.getTimeout(), conf.isReconnect());
        return new ModbusMasterAdapter<>(modbusTCPMaster, new ModbusTypeMapper(), conf.getDeviceId());
    }

    public ModbusMaster createUDPModbusMaster(UDPModbusMasterConfiguration conf) {
        ModbusUDPMaster modbusUDPMaster =
                new ModbusUDPMaster(conf.getAddress(), conf.getPort(), conf.getTimeout());
        return new ModbusMasterAdapter<>(modbusUDPMaster, new ModbusTypeMapper(), conf.getDeviceId());
    }

    public ModbusMaster createSerialModbusMaster(SerialModbusConfiguration conf) {
        SerialParameters serialParams =
                new SerialParameters(conf.getPortName(), conf.getBaudRate(), conf.getFlowControlIn(),
                        conf.getFlowControlOut(), conf.getDataBits(), conf.getStopBits(), conf.getParity(),
                        conf.isEcho());
        serialParams.setEncoding(conf.getEncoding());

        ModbusSerialMaster modbusSerialMaster = new ModbusSerialMaster(serialParams, conf.getTimeout());
        return new ModbusMasterAdapter<>(modbusSerialMaster, new ModbusTypeMapper(), conf.getDeviceId());
    }
}
