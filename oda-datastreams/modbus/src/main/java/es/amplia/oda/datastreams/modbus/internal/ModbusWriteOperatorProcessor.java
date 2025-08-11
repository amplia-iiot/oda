package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;
import es.amplia.oda.datastreams.modbus.ModbusConnectionsFinder;
import es.amplia.oda.hardware.modbus.ModbusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

class ModbusWriteOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusWriteOperatorProcessor.class);

    private final ModbusConnectionsFinder modbusConnectionsLocator;
    private final JavaTypeToModbusTypeConverter converter;


    ModbusWriteOperatorProcessor(ModbusConnectionsFinder modbusConnectionsLocator, JavaTypeToModbusTypeConverter converter) {
        this.modbusConnectionsLocator = modbusConnectionsLocator;
        this.converter = converter;
    }


    void write(String deviceId, Type datastreamType, ModbusType dataType, int slaveAddress, int dataAddress, Object value) {

        // retrieve modbus connection from pool
        ModbusMaster modbusConnection = modbusConnectionsLocator.getModbusConnectionWithId(deviceId);

        if (modbusConnection == null) {
            LOGGER.error("There is no hardware modbus service available for the device {}", deviceId);
        } else {
            writeFunctions(datastreamType, dataType, modbusConnection, slaveAddress, dataAddress, value);
        }
    }


    private void writeFunctions(Type datastreamType, ModbusType modbusType, ModbusMaster modbusConnection,
                                int slaveAddress, int dataAddress, Object value) {
        try {
            // COIL
            if (datastreamType.equals(Boolean.class) && modbusType.equals(ModbusType.COIL)) {
                writeBooleanToCoil(modbusConnection, slaveAddress, dataAddress, value);
            }
            // HOLDING REGISTER
            else if (datastreamType.equals(Byte[].class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
                writeByteArrayToHoldingRegister(modbusConnection, slaveAddress, dataAddress, value);
            } else if (datastreamType.equals(Short.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
                writeShortToHoldingRegister(modbusConnection, slaveAddress, dataAddress, value);
            } else if (datastreamType.equals(Integer.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
                writeIntegerToHoldingRegister(modbusConnection, slaveAddress, dataAddress, value);
            } else if (datastreamType.equals(Float.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
                writeFloatToHoldingRegister(modbusConnection, slaveAddress, dataAddress, value);
            } else if (datastreamType.equals(Long.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
                writeLongToHoldingRegister(modbusConnection, slaveAddress, dataAddress, value);
            } else if (datastreamType.equals(Double.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
                writeDoubleToHoldingRegister(modbusConnection, slaveAddress, dataAddress, value);
            } else {
                throwInvalidDataTypes(slaveAddress, dataAddress, value);
            }
        } catch (Exception e) {
            LOGGER.error("Error writing to modbus : ", e);
            throw e;
        }
    }

    private void throwInvalidDataTypes(int slaveAddress, int dataAddress, Object value) {
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
