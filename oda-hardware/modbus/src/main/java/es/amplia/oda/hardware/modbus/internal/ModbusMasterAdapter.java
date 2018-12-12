package es.amplia.oda.hardware.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusException;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;

import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ModbusMasterAdapter<T extends AbstractModbusMaster> implements ModbusMaster {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusMasterAdapter.class);

    private static final int ONE_ENTRY = 1;
    private static final int FIRST_INDEX = 0;

    private final T modbusMaster;
    private final ModbusTypeMapper modbusTypeMapper;

    ModbusMasterAdapter(T modbusMasterToAdapt, ModbusTypeMapper modbusTypeMapper) {
        this.modbusMaster = modbusMasterToAdapt;
        this.modbusTypeMapper = modbusTypeMapper;
    }

    @Override
    public void connect() {
        try {
            modbusMaster.connect();
        } catch (Exception exception) {
            LOGGER.error("Error connecting modbus master: {}", exception);
            throw new ModbusException("Error connecting modbus master", exception);
        }
    }

    @Override
    public boolean readInputDiscrete(int unitId, int ref) {
        return readInputDiscretes(unitId, ref, ONE_ENTRY)[FIRST_INDEX];
    }

    @Override
    public Boolean[] readInputDiscretes(int unitId, int ref, int count) {
        try {
            BitVector bitVector =  modbusMaster.readInputDiscretes(unitId, ref, count);
            return modbusTypeMapper.mapBitVectorValues(bitVector);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            LOGGER.error("Error reading {} input discretes from {} of slave {}: {}", count, ref, unitId, exception);
            throw new ModbusException("Error reading " + count + " input discretes from " + ref + " of slave " + unitId, exception);
        }
    }

    @Override
    public boolean readCoil(int unitId, int ref) {
        return readCoils(unitId, ref, ONE_ENTRY)[FIRST_INDEX];
    }

    @Override
    public Boolean[] readCoils(int unitId, int ref, int count) {
        try {
            BitVector bitVector =  modbusMaster.readCoils(unitId, ref, count);
            return modbusTypeMapper.mapBitVectorValues(bitVector);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            LOGGER.error("Error reading {} coils from {} of slave {}: {}", count, ref, unitId, exception);
            throw new ModbusException("Error reading " + count + " coils from " + ref + " of slave " + unitId, exception);
        }
    }

    @Override
    public void writeCoil(int unitId, int ref, boolean value) {
        try {
            modbusMaster.writeCoil(unitId, ref, value);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            LOGGER.error("Error writing coil {} of slave {} with value {}: {}", unitId, ref, value, exception);
            throw new ModbusException("Error writing coil " + ref + " of slave " + unitId + " with value " + value, exception);
        }
    }

    @Override
    public void writeCoils(int unitId, int ref, Boolean[] values) {
        try {
            BitVector bitVector = modbusTypeMapper.mapValuesToBitVector(values);
            modbusMaster.writeMultipleCoils(unitId, ref, bitVector);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            LOGGER.error("Error writing {} coils from {} of slave {} with values {}: {}", values.length, ref, unitId, Arrays.toString(values), exception);
            throw new ModbusException("Error writing " + values.length + " coils from " + ref + " of slave " + unitId + " with value " + Arrays.toString(values), exception);
        }
    }

    @Override
    public Register readInputRegister(int unitId, int ref) {
        return readInputRegisters(unitId, ref, ONE_ENTRY)[FIRST_INDEX];
    }

    @Override
    public Register[] readInputRegisters(int unitId, int ref, int count){
        try {
            InputRegister[] inputRegisters =  modbusMaster.readInputRegisters(unitId, ref, count);
            return modbusTypeMapper.mapInputRegister(inputRegisters);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            LOGGER.error("Error reading {} input registers from {} of slave {}: {}", count, ref, unitId, exception);
            throw new ModbusException("Error reading " + count + " input registers from " + ref + " of slave " + unitId, exception);
        }
    }

    @Override
    public Register readHoldingRegister(int unitId, int ref) {
        return readInputRegisters(unitId, ref, ONE_ENTRY)[FIRST_INDEX];
    }

    @Override
    public Register[] readHoldingRegisters(int unitId, int ref, int count) {
        try {
            InputRegister[] inputRegisters =  modbusMaster.readMultipleRegisters(unitId, ref, count);
            return modbusTypeMapper.mapInputRegister(inputRegisters);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            LOGGER.error("Error reading {} holding registers from {} of slave {}: {}", count, ref, unitId, exception);
            throw new ModbusException("Error reading " + count + " holding registers from " + ref + " of slave " + unitId, exception);
        }
    }

    @Override
    public void writeHoldingRegister(int unitId, int ref, Register register) {
        try {
            com.ghgande.j2mod.modbus.procimg.Register j2Register = modbusTypeMapper.mapToJ2ModbusRegister(register);
            modbusMaster.writeSingleRegister(unitId, ref, j2Register);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            LOGGER.error("Error writing holding register {} of slave {} with value {}: {}", ref, unitId, register, exception);
            throw new ModbusException("Error writing holding register " + ref + " of slave " + unitId +  " with value " + register, exception);
        }
    }

    @Override
    public void writeHoldingRegisters(int unitId, int ref, Register[] registers) {
        try {
            com.ghgande.j2mod.modbus.procimg.Register[] j2Registers = modbusTypeMapper.mapToJ2ModbusRegisters(registers);
            modbusMaster.writeMultipleRegisters(unitId, ref, j2Registers);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            LOGGER.error("Error writing holding registers from {} of slave {} with values {}: {}", ref, unitId, Arrays.toString(registers), exception);
            throw new ModbusException("Error writing holding registers from " + ref + " of slave " + unitId +  " with values " + Arrays.toString(registers), exception);
        }
    }

    @Override
    public void disconnect() {
        modbusMaster.disconnect();
    }
}
