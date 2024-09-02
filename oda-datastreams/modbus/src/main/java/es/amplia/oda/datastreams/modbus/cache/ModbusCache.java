package es.amplia.oda.datastreams.modbus.cache;

import es.amplia.oda.core.commons.modbus.Register;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ModbusCache {

    // Map <modbusAddress, modbusRegister>
    Map<Integer, Register> holdingRegisterCache = new HashMap<>();
    Map<Integer, Boolean> coilCache = new HashMap<>();
    Map<Integer, Register> inputRegisterCache = new HashMap<>();
    Map<Integer, Boolean> inputDiscreteCache = new HashMap<>();

    public void setHoldingRegisterValues(Register[] values, int startAddress){
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                //log.info("Writing value {} in address {}", values[i].getValue(), i + startAddress);
                this.holdingRegisterCache.put(i + startAddress, values[i]);
            }
        }
    }

    public void setInputDiscreteValues(Boolean[] values, int startAddress){
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                //log.info("Writing value {} in address {}", values[i].getValue(), i + startAddress);
                this.inputDiscreteCache.put(i + startAddress, values[i]);
            }
        }
    }

    public void setCoilValues(Boolean[] values, int startAddress){
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                //log.info("Writing value {} in address {}", values[i].getValue(), i + startAddress);
                this.coilCache.put(i + startAddress, values[i]);
            }
        }
    }

    public void setInputRegisterValues(Register[] values, int startAddress){
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                //log.info("Writing value {} in address {}", values[i].getValue(), i + startAddress);
                this.inputRegisterCache.put(i + startAddress, values[i]);
            }
        }
    }

    public Register[] getHoldingRegisterValues(int modbusAddress, int numValues) {
        Register[] registers = new Register[numValues];
        for (int i = 0; i < numValues; i++) {
            registers[i] = this.holdingRegisterCache.get(modbusAddress + i);
        }
        return registers;
    }

    public Boolean[] getInputDiscreteValues(int modbusAddress, int numValues) {
        Boolean[] registers = new Boolean[numValues];
        for (int i = 0; i < numValues; i++) {
            registers[i] = this.inputDiscreteCache.get(modbusAddress + i);
        }
        return registers;
    }

    public Boolean[] getCoilValues(int modbusAddress, int numValues) {
        Boolean[] registers = new Boolean[numValues];
        for (int i = 0; i < numValues; i++) {
            registers[i] = this.coilCache.get(modbusAddress + i);
        }
        return registers;
    }

    public Register[] getInputRegisterValues(int modbusAddress, int numValues) {
        Register[] registers = new Register[numValues];
        for (int i = 0; i < numValues; i++) {
            registers[i] = this.inputRegisterCache.get(modbusAddress + i);
        }
        return registers;
    }
}
