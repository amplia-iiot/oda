package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;
import es.amplia.oda.datastreams.modbus.ModbusConnectionsFinder;
import es.amplia.oda.datastreams.modbus.ModbusType;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

class ModbusWriteOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusWriteOperatorProcessor.class);

    private final ModbusConnectionsFinder modbusConnectionsLocator;
    private final JavaTypeToModbusTypeConverter converter;
    private final Map<OperatorSelector, QuadConsumer<ModbusMaster, Integer, Integer, Object>> writeMethods;


    @Value
    private static class OperatorSelector {
        Type type;
        ModbusType modbusType;
    }

    @FunctionalInterface
    private interface QuadConsumer<T,U,V,W> {
        void accept(T t, U u, V v, W w);
    }


    ModbusWriteOperatorProcessor(ModbusConnectionsFinder modbusConnectionsLocator, JavaTypeToModbusTypeConverter converter) {
        this.modbusConnectionsLocator = modbusConnectionsLocator;
        this.converter = converter;
        this.writeMethods = generateWriteMethods();
    }


    private Map<OperatorSelector, QuadConsumer<ModbusMaster, Integer, Integer, Object>> generateWriteMethods() {
        Map<OperatorSelector, QuadConsumer<ModbusMaster, Integer, Integer, Object>> methods = new HashMap<>();
        methods.put(new OperatorSelector(Boolean.class, ModbusType.COIL), this::writeBooleanToCoil);
        methods.put(new OperatorSelector(Byte[].class, ModbusType.HOLDING_REGISTER), this::writeByteArrayToHoldingRegister);
        methods.put(new OperatorSelector(Short.class, ModbusType.HOLDING_REGISTER), this::writeShortToHoldingRegister);
        methods.put(new OperatorSelector(Integer.class, ModbusType.HOLDING_REGISTER), this::writeIntegerToHoldingRegister);
        methods.put(new OperatorSelector(Float.class, ModbusType.HOLDING_REGISTER), this::writeFloatToHoldingRegister);
        methods.put(new OperatorSelector(Long.class, ModbusType.HOLDING_REGISTER), this::writeLongToHoldingRegister);
        methods.put(new OperatorSelector(Double.class, ModbusType.HOLDING_REGISTER), this::writeDoubleToHoldingRegister);
        return methods;
    }

    void write(String deviceId, Type datastreamType, ModbusType dataType, int slaveAddress, int dataAddress, Object value) {

        // retrieve modbus connection from pool
        ModbusMaster modbusConnection = modbusConnectionsLocator.getModbusConnectionWithId(deviceId);

        if (modbusConnection == null) {
            LOGGER.error("There is no hardware modbus service available for the device {}", deviceId);
        } else {
            writeMethods.getOrDefault(new OperatorSelector(datastreamType, dataType), this::throwInvalidDataTypes)
                    .accept(modbusConnection, slaveAddress, dataAddress, value);
        }
    }

    private void throwInvalidDataTypes(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, Object value) {
        LOGGER.error("Trying to write {} to an invalid modbus type in slave address {} and data address {}", value,
                slaveAddress, dataAddress);
        throw new IllegalArgumentException("Invalid data types to write " + value + " to slave " + slaveAddress +
                " in data address " + dataAddress);
    }

    private void writeBooleanToCoil(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, Object value) {
        modbusConnection.writeCoil(slaveAddress, dataAddress, (boolean) value);
    }

    private void writeByteArrayToHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, Object value) {
        Register register = converter.convertByteArrayToRegister((byte[]) value);
        modbusConnection.writeHoldingRegister(slaveAddress, dataAddress, register);
    }

    private void writeShortToHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, Object value) {
        Register register = converter.convertShortToRegister((short) value);
        modbusConnection.writeHoldingRegister(slaveAddress, dataAddress, register);
    }

    private void writeIntegerToHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, Object value) {
        Register[] registers = converter.convertIntegerToRegisters((int) value);
        modbusConnection.writeHoldingRegisters(slaveAddress, dataAddress, registers);
    }

    private void writeFloatToHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, Object value) {
        Register[] registers = converter.convertFloatToRegisters((float) value);
        modbusConnection.writeHoldingRegisters(slaveAddress, dataAddress, registers);
    }

    private void writeLongToHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, Object value) {
        Register[] registers = converter.convertLongToRegisters((long) value);
        modbusConnection.writeHoldingRegisters(slaveAddress, dataAddress, registers);
    }

    private void writeDoubleToHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, Object value) {
        Register[] registers = converter.convertDoubleToRegisters((double) value);
        modbusConnection.writeHoldingRegisters(slaveAddress, dataAddress, registers);
    }
}
