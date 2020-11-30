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
import java.util.function.BiFunction;

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;

class ModbusReadOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusReadOperatorProcessor.class);

    static final int TWO_REGISTERS = 2;
    static final int FOUR_REGISTERS = 4;
    
    private final ModbusMaster modbusMaster;
    private final ModbusTypeToJavaTypeConverter converter;
    private final Map<OperatorSelector, BiFunction<Integer, Integer, CollectedValue>> readFunctions;

    @Value
    private static class OperatorSelector {
        Type datastreamType;
        ModbusType modbusType;
    }

    ModbusReadOperatorProcessor(ModbusMaster modbusMaster, ModbusTypeToJavaTypeConverter converter) {
        this.modbusMaster = modbusMaster;
        this.converter = converter;
        this.readFunctions = generateReadFunctions();
    }

    private Map<OperatorSelector, BiFunction<Integer, Integer, CollectedValue>> generateReadFunctions() {
        Map<OperatorSelector, BiFunction<Integer, Integer, CollectedValue>> functions = new HashMap<>();
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

    CollectedValue read(Type datastreamType, ModbusType dataType, int slaveAddress, int dataAddress) {
        return readFunctions.getOrDefault(new OperatorSelector(datastreamType, dataType), this::throwInvalidDataTypes)
                .apply(slaveAddress, dataAddress);
    }

    private CollectedValue throwInvalidDataTypes(int slaveAddress, int dataAddress) {
        LOGGER.error("Trying to read a data type from invalid modbus register in slave address {} and data address {}",
                slaveAddress, dataAddress);
        throw new IllegalArgumentException("Invalid data types to read from slave " + slaveAddress +
                " in data address " + dataAddress);
    }
    
    private CollectedValue readBooleanFromInputDiscrete(int slaveAddress, int dataAddress) {
        boolean value = modbusMaster.readInputDiscrete(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), value);
    }

    private CollectedValue readBooleanFromCoil(int slaveAddress, int dataAddress) {
        boolean value = modbusMaster.readCoil(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), value);
    }

    private CollectedValue readBytesFromInputRegister(int slaveAddress, int dataAddress) {
        Register registerValue = modbusMaster.readInputRegister(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToByteArray(registerValue));
    }

    private CollectedValue readShortFromInputRegister(int slaveAddress, int dataAddress) {
        Register registerValue = modbusMaster.readInputRegister(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToShort(registerValue));
    }

    private CollectedValue readIntegerFromTwoInputRegister(int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusMaster.readInputRegisters(slaveAddress, dataAddress, TWO_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToInteger(registerValues));
    }

    private CollectedValue readFloatFromTwoInputRegister(int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusMaster.readInputRegisters(slaveAddress, dataAddress, TWO_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToFloat(registerValues));
    }

    private CollectedValue readLongFromFourInputRegister(int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusMaster.readInputRegisters(slaveAddress, dataAddress, FOUR_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToLong(registerValues));
    }

    private CollectedValue readDoubleFromFourInputRegister(int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusMaster.readInputRegisters(slaveAddress, dataAddress, FOUR_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToDouble(registerValues));
    }

    private CollectedValue readBytesFromHoldingRegister(int slaveAddress, int dataAddress) {
        Register registerValue = modbusMaster.readHoldingRegister(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToByteArray(registerValue));
    }

    private CollectedValue readShortFromHoldingRegister(int slaveAddress, int dataAddress) {
        Register registerValue = modbusMaster.readHoldingRegister(slaveAddress, dataAddress);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToShort(registerValue));
    }

    private CollectedValue readIntegerFromTwoHoldingRegister(int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusMaster.readHoldingRegisters(slaveAddress, dataAddress, TWO_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToInteger(registerValues));
    }

    private CollectedValue readFloatFromTwoHoldingRegister(int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusMaster.readHoldingRegisters(slaveAddress, dataAddress, TWO_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToFloat(registerValues));
    }

    private CollectedValue readLongFromFourHoldingRegister(int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusMaster.readHoldingRegisters(slaveAddress, dataAddress, FOUR_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToLong(registerValues));
    }

    private CollectedValue readDoubleFromFourHoldingRegister(int slaveAddress, int dataAddress) {
        Register[] registerValues = modbusMaster.readHoldingRegisters(slaveAddress, dataAddress, FOUR_REGISTERS);
        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToDouble(registerValues));
    }
}
