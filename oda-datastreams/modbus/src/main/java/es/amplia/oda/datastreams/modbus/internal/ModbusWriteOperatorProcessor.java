package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;
import es.amplia.oda.datastreams.modbus.ModbusType;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

class ModbusWriteOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusWriteOperatorProcessor.class);

    private final ModbusMaster modbusMaster;
    private final JavaTypeToModbusTypeConverter converter;
    private final Map<OperatorSelector, TriConsumer<Integer, Integer, Object>> writeMethods;

    @Value
    private static class OperatorSelector {
        private Type type;
        private ModbusType modbusType;
    }

    @FunctionalInterface
    private interface TriConsumer<T,U,V> {
        void accept(T t, U u, V v);
    }


    ModbusWriteOperatorProcessor(ModbusMaster modbusMaster, JavaTypeToModbusTypeConverter converter) {
        this.modbusMaster = modbusMaster;
        this.converter = converter;
        this.writeMethods = generateWriteMethods();
    }

    private Map<OperatorSelector, TriConsumer<Integer, Integer, Object>> generateWriteMethods() {
        Map<OperatorSelector, TriConsumer<Integer, Integer, Object>> methods = new HashMap<>();
        methods.put(new OperatorSelector(Boolean.class, ModbusType.COIL), this::writeBooleanToCoil);
        methods.put(new OperatorSelector(Byte[].class, ModbusType.HOLDING_REGISTER), this::writeByteArrayToHoldingRegister);
        methods.put(new OperatorSelector(Short.class, ModbusType.HOLDING_REGISTER), this::writeShortToHoldingRegister);
        methods.put(new OperatorSelector(Integer.class, ModbusType.HOLDING_REGISTER), this::writeIntegerToHoldingRegister);
        methods.put(new OperatorSelector(Float.class, ModbusType.HOLDING_REGISTER), this::writeFloatToHoldingRegister);
        methods.put(new OperatorSelector(Long.class, ModbusType.HOLDING_REGISTER), this::writeLongToHoldingRegister);
        methods.put(new OperatorSelector(Double.class, ModbusType.HOLDING_REGISTER), this::writeDoubleToHoldingRegister);
        return methods;
    }

    void write(Type datastreamType, ModbusType dataType, int slaveAddress, int dataAddress, Object value) {
        writeMethods.getOrDefault(new OperatorSelector(datastreamType, dataType), this::throwInvalidDataTypes)
                .accept(slaveAddress, dataAddress, value);
    }

    private void throwInvalidDataTypes(int slaveAddress, int dataAddress, Object value) {
        LOGGER.error("Trying to write {} to an invalid modbus type in slave address {} and data address {}", value,
                slaveAddress, dataAddress);
        throw new IllegalArgumentException("Invalid data types to write " + value + " to slave " + slaveAddress +
                " in data address " + dataAddress);
    }

    private void writeBooleanToCoil(int slaveAddress, int dataAddress, Object value) {
        modbusMaster.writeCoil(slaveAddress, dataAddress, (boolean) value);
    }

    private void writeByteArrayToHoldingRegister(int slaveAddress, int dataAddress, Object value) {
        Register register = converter.convertByteArrayToRegister((byte[]) value);
        modbusMaster.writeHoldingRegister(slaveAddress, dataAddress, register);
    }

    private void writeShortToHoldingRegister(int slaveAddress, int dataAddress, Object value) {
        Register register = converter.convertShortToRegister((short) value);
        modbusMaster.writeHoldingRegister(slaveAddress, dataAddress, register);
    }

    private void writeIntegerToHoldingRegister(int slaveAddress, int dataAddress, Object value) {
        Register[] registers = converter.convertIntegerToRegisters((int) value);
        modbusMaster.writeHoldingRegisters(slaveAddress, dataAddress, registers);
    }

    private void writeFloatToHoldingRegister(int slaveAddress, int dataAddress, Object value) {
        Register[] registers = converter.convertFloatToRegisters((float) value);
        modbusMaster.writeHoldingRegisters(slaveAddress, dataAddress, registers);
    }

    private void writeLongToHoldingRegister(int slaveAddress, int dataAddress, Object value) {
        Register[] registers = converter.convertLongToRegisters((long) value);
        modbusMaster.writeHoldingRegisters(slaveAddress, dataAddress, registers);
    }

    private void writeDoubleToHoldingRegister(int slaveAddress, int dataAddress, Object value) {
        Register[] registers = converter.convertDoubleToRegisters((double) value);
        modbusMaster.writeHoldingRegisters(slaveAddress, dataAddress, registers);
    }
}
