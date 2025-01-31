package es.amplia.oda.datastreams.modbus.cache;

import es.amplia.oda.core.commons.modbus.Register;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModbusCache {

    // Map <modbusAddress, ModbusCacheRegister>
    Map<Integer, ModbusCacheRegister> holdingRegisterCache = new HashMap<>();
    Map<Integer, ModbusCacheRegister> coilCache = new HashMap<>();
    Map<Integer, ModbusCacheRegister> inputRegisterCache = new HashMap<>();
    Map<Integer, ModbusCacheRegister> inputDiscreteCache = new HashMap<>();

    String deviceId;

    public ModbusCache(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setHoldingRegisterValues(Register[] values, int startAddress, long at){
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                //log.info("Writing value {} in address {}", values[i].getValue(), i + startAddress);
                this.holdingRegisterCache.put(i + startAddress, new ModbusCacheRegister(at, values[i]));
            }
        }
    }

    public void emptyHoldingRegisterValues(int startAddress, int numRegisters) {
        for (int i = 0; i < numRegisters; i++) {
            this.holdingRegisterCache.put(i + startAddress, null);
        }
    }

    public void setInputDiscreteValues(Boolean[] values, int startAddress, long at){
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                //log.info("Writing value {} in address {}", values[i].getValue(), i + startAddress);
                this.inputDiscreteCache.put(i + startAddress, new ModbusCacheRegister(at, values[i]));
            }
        }
    }

    public void emptyInputDiscreteValues(int startAddress, int numRegisters) {
        for (int i = 0; i < numRegisters; i++) {
            this.inputDiscreteCache.put(i + startAddress, null);
        }
    }

    public void setCoilValues(Boolean[] values, int startAddress, long at){
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                //log.info("Writing value {} in address {}", values[i].getValue(), i + startAddress);
                this.coilCache.put(i + startAddress, new ModbusCacheRegister(at, values[i]));
            }
        }
    }

    public void emptyCoilValues(int startAddress, int numRegisters) {
        for (int i = 0; i < numRegisters; i++) {
            this.coilCache.put(i + startAddress, null);
        }
    }

    public void setInputRegisterValues(Register[] values, int startAddress, long at){
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                //log.info("Writing value {} in address {}", values[i].getValue(), i + startAddress);
                this.inputRegisterCache.put(i + startAddress, new ModbusCacheRegister(at, values[i]));
            }
        }
    }

    public void emptyInputRegisterValues(int startAddress, int numRegisters) {
        for (int i = 0; i < numRegisters; i++) {
            this.inputRegisterCache.put(i + startAddress, null);
        }
    }

    public List<ModbusCacheRegister> getHoldingRegisterValues(int modbusAddress, int numValues) {
        return getValuesFromCache(this.holdingRegisterCache, modbusAddress, numValues);
    }

    public List<ModbusCacheRegister> getInputDiscreteValues(int modbusAddress, int numValues) {
        return getValuesFromCache(this.inputDiscreteCache, modbusAddress, numValues);
    }

    public List<ModbusCacheRegister> getCoilValues(int modbusAddress, int numValues) {
       return getValuesFromCache(this.coilCache, modbusAddress, numValues);
    }

    public List<ModbusCacheRegister> getInputRegisterValues(int modbusAddress, int numValues) {
        return getValuesFromCache(this.inputRegisterCache, modbusAddress, numValues);
    }

    private List<ModbusCacheRegister> getValuesFromCache(Map<Integer, ModbusCacheRegister> modbusCache,
                                                         int modbusAddress, int numValues) {
        List<ModbusCacheRegister> modbusRegistersFromCache = new ArrayList<>();

        // fill list with the registers needed
        for (int i = 0; i < numValues; i++) {
            modbusRegistersFromCache.add(modbusCache.get(modbusAddress + i));
        }

        if(modbusRegistersFromCache.isEmpty()){
            return null;
        }

        // check if any of the registers is null
        if (modbusRegistersFromCache.contains(null)) {
            log.error("Can't retrieve value from cache of device {} : at least one of the {} registers starting " +
                    "from address {} is null", this.deviceId, numValues, modbusAddress);
            return null;
        }

        // check all registers have the same time
        long at = modbusRegistersFromCache.get(0).getAt();
        for (ModbusCacheRegister register : modbusRegistersFromCache) {
            if (register.getAt() != at) {
                log.error("Can't retrieve value from cache of device {} : at least one of the {} registers starting " +
                        "from address {} has a different date time than the rest", this.deviceId, numValues, modbusAddress);
                return null;
            }
        }

        return modbusRegistersFromCache;
    }
}
