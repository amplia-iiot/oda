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

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;

class ModbusReadOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusReadOperatorProcessor.class);

    static final int TWO_REGISTERS = 2;
    static final int FOUR_REGISTERS = 4;
    
    private final ModbusConnectionsFinder modbusConnectionsLocator;
    private final ModbusTypeToJavaTypeConverter converter;
    private final Map<OperatorSelector, TriFunction<ModbusMaster, Integer, Integer, CollectedValue>> readFunctions;

    @Value
    private static class OperatorSelector {
        Type datastreamType;
        ModbusType modbusType;
    }

    @FunctionalInterface
    interface TriFunction<A,B,C,R> {

        R apply(A a, B b, C c);
    }

    ModbusReadOperatorProcessor(ModbusConnectionsFinder modbusConnectionsLocator, ModbusTypeToJavaTypeConverter converter) {
        this.modbusConnectionsLocator = modbusConnectionsLocator;
        this.converter = converter;
        this.readFunctions = generateReadFunctions();
    }

    private Map<OperatorSelector, TriFunction<ModbusMaster, Integer, Integer, CollectedValue>> generateReadFunctions() {
        Map<OperatorSelector, TriFunction<ModbusMaster, Integer, Integer, CollectedValue>> functions = new HashMap<>();
        functions.put(new OperatorSelector(Boolean.class, ModbusType.INPUT_DISCRETE), this::readBooleanFromInputDiscrete);
        functions.put(new OperatorSelector(Boolean.class, ModbusType.COIL), this::readBooleanFromCoil);
        functions.put(new OperatorSelector(Byte[].class, ModbusType.INPUT_REGISTER), this::readBytesFromInputRegister);
        functions.put(new OperatorSelector(Byte[].class, ModbusType.HOLDING_REGISTER),this::readBytesFromHoldingRegister);
        functions.put(new OperatorSelector(Short.class, ModbusType.INPUT_REGISTER), this::readShortFromInputRegister);
        functions.put(new OperatorSelector(Short.class, ModbusType.HOLDING_REGISTER), this::readShortFromHoldingRegister);
        functions.put(new OperatorSelector(Integer.class, ModbusType.INPUT_REGISTER), this::readIntegerFromTwoInputRegister);
        functions.put(new OperatorSelector(Integer.class, ModbusType.HOLDING_REGISTER), this::readIntegerFromTwoHoldingRegister);
        functions.put(new OperatorSelector(Float.class, ModbusType.INPUT_REGISTER), this::readFloatFromTwoInputRegister);
        functions.put(new OperatorSelector(Float.class, ModbusType.HOLDING_REGISTER), this::readFloatFromTwoHoldingRegister);
        functions.put(new OperatorSelector(Long.class, ModbusType.INPUT_REGISTER), this::readLongFromFourInputRegister);
        functions.put(new OperatorSelector(Long.class, ModbusType.HOLDING_REGISTER), this::readLongFromFourHoldingRegister);
        functions.put(new OperatorSelector(Double.class, ModbusType.INPUT_REGISTER), this::readDoubleFromFourInputRegister);
        functions.put(new OperatorSelector(Double.class, ModbusType.HOLDING_REGISTER), this::readDoubleFromFourHoldingRegister);
        return functions;
    }

    CollectedValue read(String deviceId, Type datastreamType, ModbusType dataType, int slaveAddress, int dataAddress) {

        // retrieve modbus connection from pool
        ModbusMaster modbusConnection = modbusConnectionsLocator.getModbusConnectionWithId(deviceId);

        if (modbusConnection == null) {
            LOGGER.error("There is no hardware modbus service available for the device {}", deviceId);
            return null;
        } else {
            return readFunctions.getOrDefault(new OperatorSelector(datastreamType, dataType), this::throwInvalidDataTypes)
                    .apply(modbusConnection, slaveAddress, dataAddress);
        }
    }

    private CollectedValue throwInvalidDataTypes(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        LOGGER.error("Trying to read a data type from invalid modbus register in slave address {} and data address {}",
                slaveAddress, dataAddress);
        throw new IllegalArgumentException("Invalid data types to read from slave " + slaveAddress +
                " in data address " + dataAddress);
    }
    
    private CollectedValue readBooleanFromInputDiscrete(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        boolean value = modbusConnection.readInputDiscrete(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), value);
    }

    private CollectedValue readBooleanFromCoil(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        boolean value = modbusConnection.readCoil(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), value);
    }

    private CollectedValue readBytesFromInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register registerValue = modbusConnection.readInputRegister(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToByteArray(registerValue));
    }

    private CollectedValue readShortFromInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register registerValue = modbusConnection.readInputRegister(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToShort(registerValue));
    }

    private CollectedValue readIntegerFromTwoInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusConnection.readInputRegisters(slaveAddress, dataAddress, TWO_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToInteger(registerValues));
    }

    private CollectedValue readFloatFromTwoInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusConnection.readInputRegisters(slaveAddress, dataAddress, TWO_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToFloat(registerValues));
    }

    private CollectedValue readLongFromFourInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusConnection.readInputRegisters(slaveAddress, dataAddress, FOUR_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToLong(registerValues));
    }

    private CollectedValue readDoubleFromFourInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusConnection.readInputRegisters(slaveAddress, dataAddress, FOUR_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToDouble(registerValues));
    }

    private CollectedValue readBytesFromHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register registerValue = modbusConnection.readHoldingRegister(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToByteArray(registerValue));
    }

    private CollectedValue readShortFromHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register registerValue = modbusConnection.readHoldingRegister(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToShort(registerValue));
    }

    private CollectedValue readIntegerFromTwoHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusConnection.readHoldingRegisters(slaveAddress, dataAddress, TWO_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToInteger(registerValues));
    }

    private CollectedValue readFloatFromTwoHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusConnection.readHoldingRegisters(slaveAddress, dataAddress, TWO_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToFloat(registerValues));
    }

    private CollectedValue readLongFromFourHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusConnection.readHoldingRegisters(slaveAddress, dataAddress, FOUR_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToLong(registerValues));
    }

    private CollectedValue readDoubleFromFourHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusConnection.readHoldingRegisters(slaveAddress, dataAddress, FOUR_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToDouble(registerValues));
    }
}
