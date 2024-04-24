package es.amplia.oda.hardware.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusException;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;

import com.ghgande.j2mod.modbus.facade.AbstractModbusMaster;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ModbusMasterAdapter<T extends AbstractModbusMaster> implements ModbusMaster {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusMasterAdapter.class);

    private static final int ONE_ENTRY = 1;
    private static final int FIRST_INDEX = 0;
    private static final String READING_OPERATION = "reading";
    private static final String WRITING_OPERATION = "writing";
    private static final String INPUT_DISCRETE_REG_TYPE = "input discrete";
    private static final String COIL_REG_TYPE = "coil";
    private static final String INPUT_REGISTER_REG_TYPE = "input register";
    private static final String HOLDING_REGISTER_REG_TYPE = "holding register";

    private final T modbusMaster;
    private final ModbusTypeMapper modbusTypeMapper;
    private final String deviceId;
    private final String deviceManufacturer;

    ModbusMasterAdapter(T modbusMasterToAdapt, ModbusTypeMapper modbusTypeMapper, String deviceId,
                        String deviceManufacturer) {
        this.modbusMaster = modbusMasterToAdapt;
        this.modbusTypeMapper = modbusTypeMapper;
        this.deviceId = deviceId;
        this.deviceManufacturer = deviceManufacturer;
    }

    @Override
    public void connect() {
        try {
            modbusMaster.connect();
        } catch (Exception exception) {
            LOGGER.error("Error connecting modbus master: ", exception);
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
            throw createModbusException(READING_OPERATION, INPUT_DISCRETE_REG_TYPE, unitId, ref, count, exception);
        }
    }

    private ModbusException createModbusException(String operation, String registerType, int unitId, int ref, int count,
                                      Exception exception) {
        LOGGER.error("Error {} {} {}s from {} of slave {} from device {}: ", operation, count, registerType, ref, unitId, this.deviceId, exception);
        return new ModbusException("Error " + operation + " " + count + " " + registerType + "s from " + ref +
                " of slave " + unitId, exception);
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
            throw createModbusException(READING_OPERATION, COIL_REG_TYPE, unitId, ref, count, exception);
        }
    }

    @Override
    public void writeCoil(int unitId, int ref, boolean value) {
        try {
            modbusMaster.writeCoil(unitId, ref, value);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            throw createModbusException(WRITING_OPERATION, COIL_REG_TYPE, unitId, ref, ONE_ENTRY, exception);
        }
    }

    @Override
    public void writeCoils(int unitId, int ref, Boolean[] values) {
        try {
            BitVector bitVector = modbusTypeMapper.mapValuesToBitVector(values);
            modbusMaster.writeMultipleCoils(unitId, ref, bitVector);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            throw createModbusException(WRITING_OPERATION, COIL_REG_TYPE, unitId, ref, values.length, exception);
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
            return modbusTypeMapper.mapInputRegisters(inputRegisters);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            throw createModbusException(READING_OPERATION, INPUT_REGISTER_REG_TYPE, unitId, ref, count, exception);
        }
    }

    @Override
    public Register readHoldingRegister(int unitId, int ref) {
        return readHoldingRegisters(unitId, ref, ONE_ENTRY)[FIRST_INDEX];
    }

    @Override
    public Register[] readHoldingRegisters(int unitId, int ref, int count) {
        try {
            InputRegister[] inputRegisters =  modbusMaster.readMultipleRegisters(unitId, ref, count);
            return modbusTypeMapper.mapInputRegisters(inputRegisters);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            throw createModbusException(READING_OPERATION, HOLDING_REGISTER_REG_TYPE, unitId, ref, count, exception);
        }
    }

    @Override
    public void writeHoldingRegister(int unitId, int ref, Register register) {
        try {
            com.ghgande.j2mod.modbus.procimg.Register j2Register = modbusTypeMapper.mapToJ2ModbusRegister(register);
            modbusMaster.writeSingleRegister(unitId, ref, j2Register);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            throw createModbusException(WRITING_OPERATION, HOLDING_REGISTER_REG_TYPE, unitId, ref, ONE_ENTRY, exception);
        }
    }

    @Override
    public void writeHoldingRegisters(int unitId, int ref, Register[] registers) {
        try {
            com.ghgande.j2mod.modbus.procimg.Register[] j2Registers = modbusTypeMapper.mapToJ2ModbusRegisters(registers);
            modbusMaster.writeMultipleRegisters(unitId, ref, j2Registers);
        } catch (com.ghgande.j2mod.modbus.ModbusException exception) {
            throw createModbusException(WRITING_OPERATION, HOLDING_REGISTER_REG_TYPE, unitId, ref, registers.length,
                    exception);
        }
    }

    @Override
    public void disconnect() {
        modbusMaster.disconnect();
    }

    @Override
    public String getDeviceId() {
        return this.deviceId;
    }

    @Override
    public String getDeviceManufacturer() {
        return this.deviceManufacturer;
    }
}
