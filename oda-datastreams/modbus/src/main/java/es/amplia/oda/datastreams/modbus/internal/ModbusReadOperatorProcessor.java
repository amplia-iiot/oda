package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;
import es.amplia.oda.datastreams.modbus.ModbusConnectionsFinder;
import es.amplia.oda.datastreams.modbus.ModbusType;
import es.amplia.oda.datastreams.modbus.cache.ModbusCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;

class ModbusReadOperatorProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusReadOperatorProcessor.class);

    static final int ONE_REGISTER = 1;
    static final int TWO_REGISTERS = 2;
    static final int FOUR_REGISTERS = 4;
    static final int MAX_HOLDING_REGISTERS_PER_REQUEST = 120;
    static final int MAX_INPUT_REGISTER_PER_REQUEST = 120;
    static final int MAX_INPUT_DISCRETE_PER_REQUEST = 1900;
    static final int MAX_COIL_PER_REQUEST = 1900;


    private final ModbusConnectionsFinder modbusConnectionsLocator;
    private final ModbusTypeToJavaTypeConverter converter;

    // Cache used to store the data read from block requests
    // Map <deviceId, ModbusCache>
    Map<String, ModbusCache> modbusCaches;

    ModbusReadOperatorProcessor(ModbusConnectionsFinder modbusConnectionsLocator, ModbusTypeToJavaTypeConverter converter) {
        this.modbusConnectionsLocator = modbusConnectionsLocator;
        this.converter = converter;
        this.modbusCaches = new HashMap<>();
    }

    CollectedValue read(String deviceId, Type datastreamType, ModbusType dataType, int slaveAddress, int dataAddress,
                        boolean readFromCache, int numRegistersToRead) {

        // retrieve modbus connection from pool
        ModbusMaster modbusConnection = modbusConnectionsLocator.getModbusConnectionWithId(deviceId);

        if (modbusConnection == null) {
            LOGGER.error("There is no hardware modbus service available for the device {}", deviceId);
            return null;
        } else {
            return readFunctions(datastreamType, dataType, modbusConnection, slaveAddress, dataAddress, readFromCache, numRegistersToRead);
        }
    }

    private CollectedValue readFunctions(Type datastreamType, ModbusType modbusType, ModbusMaster modbusConnection,
                                         int slaveAddress, int dataAddress, boolean readFromCache, int numRegistersToRead) {

        // INPUT DISCRETE
        if (datastreamType.equals(Boolean.class) && modbusType.equals(ModbusType.INPUT_DISCRETE)) {
            return readBooleanFromInputDiscrete(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(List.class) && modbusType.equals(ModbusType.INPUT_DISCRETE)) {
            return readNFromInputDiscrete(modbusConnection, slaveAddress, dataAddress, numRegistersToRead);
        }
        // COIL
        else if (datastreamType.equals(Boolean.class) && modbusType.equals(ModbusType.COIL)) {
            return readBooleanFromCoil(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(List.class) && modbusType.equals(ModbusType.COIL)) {
            return readNFromCoil(modbusConnection, slaveAddress, dataAddress, numRegistersToRead);
        }
        // INPUT REGISTER
        else if (datastreamType.equals(Byte[].class) && modbusType.equals(ModbusType.INPUT_REGISTER)) {
            return readBytesFromInputRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(Short.class) && modbusType.equals(ModbusType.INPUT_REGISTER)) {
            return readShortFromInputRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(Integer.class) && modbusType.equals(ModbusType.INPUT_REGISTER)) {
            return readIntegerFromTwoInputRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(Float.class) && modbusType.equals(ModbusType.INPUT_REGISTER)) {
            return readFloatFromTwoInputRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(Long.class) && modbusType.equals(ModbusType.INPUT_REGISTER)) {
            return readLongFromFourInputRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(Double.class) && modbusType.equals(ModbusType.INPUT_REGISTER)) {
            return readDoubleFromFourInputRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(List.class) && modbusType.equals(ModbusType.INPUT_REGISTER)) {
            return readNFromInputRegister(modbusConnection, slaveAddress, dataAddress, numRegistersToRead);
        }
        // HOLDING REGISTER
        else if (datastreamType.equals(Byte[].class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
            return readBytesFromHoldingRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(Short.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
            return readShortFromHoldingRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(Integer.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
            return readIntegerFromTwoHoldingRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(Float.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
            return readFloatFromTwoHoldingRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(Long.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
            return readLongFromFourHoldingRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(Double.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
            return readDoubleFromFourHoldingRegister(modbusConnection, slaveAddress, dataAddress, readFromCache);
        } else if (datastreamType.equals(List.class) && modbusType.equals(ModbusType.HOLDING_REGISTER)) {
            return readNFromHoldingRegister(modbusConnection, slaveAddress, dataAddress, numRegistersToRead);
        } else {
            throwInvalidDataTypes(datastreamType, modbusType, slaveAddress, dataAddress);
            return null;
        }
    }

    private void throwInvalidDataTypes(Type datastreamType, ModbusType modbusType, int slaveAddress, int dataAddress) {
        LOGGER.error("Trying to read data type {} from invalid modbus register type {} in slave address {} and data address {}",
                datastreamType, modbusType, slaveAddress, dataAddress);
        throw new IllegalArgumentException("Invalid data type " + datastreamType + " for modbus register type " + modbusType +
                " to read from slave " + slaveAddress + " in data address " + dataAddress);
    }

    // INPUT DISCRETE

    private CollectedValue readBooleanFromInputDiscrete(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {
        boolean value;

        if (readFromCache) {
            ModbusCache cache = getCache(modbusConnection.getDeviceId());
            value = cache.getInputDiscreteValues(dataAddress, ONE_REGISTER)[0];
        } else {
            value = modbusConnection.readInputDiscrete(slaveAddress, dataAddress);
        }

        return new CollectedValue(System.currentTimeMillis(), value, null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readNFromInputDiscrete(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, int numRegisters) {
        // get cache corresponding to the deviceId of the request
        ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());

        int numRegistersToRequest = MAX_INPUT_DISCRETE_PER_REQUEST;
        int finalAddress = dataAddress + numRegisters;
        for (int i = dataAddress; i < finalAddress; i = i + MAX_INPUT_DISCRETE_PER_REQUEST) {

            if ((i + MAX_INPUT_DISCRETE_PER_REQUEST) > finalAddress) {
                numRegistersToRequest = finalAddress - i;
            }

            LOGGER.info("Reading {} input discrete from device {} starting from address {}", numRegistersToRequest,
                    modbusConnection.getDeviceId(), i);

            Boolean[] registerValues = modbusConnection.readInputDiscretes(slaveAddress, dataAddress, numRegistersToRequest);

            // save values read in cache
            modbusCache.setInputDiscreteValues(registerValues, i);
        }

        return null;
    }

    // COIL

    private CollectedValue readBooleanFromCoil(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        boolean value;

        if (readFromCache) {
            ModbusCache cache = getCache(modbusConnection.getDeviceId());
            value = cache.getCoilValues(dataAddress, ONE_REGISTER)[0];
        } else {
            value = modbusConnection.readCoil(slaveAddress, dataAddress);
        }

        return new CollectedValue(System.currentTimeMillis(), value, null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readNFromCoil(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, int numRegisters) {
        // get cache corresponding to the deviceId of the request
        ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());

        int numRegistersToRequest = MAX_COIL_PER_REQUEST;
        int finalAddress = dataAddress + numRegisters;
        for (int i = dataAddress; i < finalAddress; i = i + MAX_COIL_PER_REQUEST) {

            if ((i + MAX_COIL_PER_REQUEST) > finalAddress) {
                numRegistersToRequest = finalAddress - i;
            }

            LOGGER.info("Reading {} coil from device {} starting from address {}", numRegistersToRequest, modbusConnection.getDeviceId(), i);

            Boolean[] registerValues = modbusConnection.readCoils(slaveAddress, dataAddress, numRegistersToRequest);

            // save values read in cache
            modbusCache.setCoilValues(registerValues, i);
        }

        return null;
    }

    // INPUT REGISTERS

    private Register[] readInputRegisters(ModbusMaster modbusConnection, int slaveAddress, int dataAddress,
                                          int numRegisters, boolean readFromCache) {
        if (readFromCache) {
            ModbusCache cache = getCache(modbusConnection.getDeviceId());
            return cache.getInputRegisterValues(dataAddress, numRegisters);
        } else {
            return modbusConnection.readInputRegisters(slaveAddress, dataAddress, numRegisters);
        }
    }

    private CollectedValue readBytesFromInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, ONE_REGISTER, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToByteArray(registers[0]), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readShortFromInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, ONE_REGISTER, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToShort(registers[0]), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readIntegerFromTwoInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, TWO_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToInteger(registers), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readFloatFromTwoInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, TWO_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToFloat(registers), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readLongFromFourInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, FOUR_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToLong(registers), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readDoubleFromFourInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, FOUR_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToDouble(registers), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readNFromInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, int numRegisters) {
        // get cache corresponding to the deviceId of the request
        ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());

        int numRegistersToRequest = MAX_INPUT_REGISTER_PER_REQUEST;
        int finalAddress = dataAddress + numRegisters;
        for (int i = dataAddress; i < finalAddress; i = i + MAX_INPUT_REGISTER_PER_REQUEST) {

            if ((i + MAX_INPUT_REGISTER_PER_REQUEST) > finalAddress) {
                numRegistersToRequest = finalAddress - i;
            }

            LOGGER.info("Reading {} input register from device {} starting from address {}", numRegistersToRequest,
                    modbusConnection.getDeviceId(), i);

            Register[] registerValues = modbusConnection.readInputRegisters(slaveAddress, dataAddress, numRegistersToRequest);

            // save values read in cache
            modbusCache.setInputRegisterValues(registerValues, i);
        }

        return null;
    }

    // HOLDING REGISTERS

    private Register[] readHoldingRegisters(ModbusMaster modbusConnection, int slaveAddress, int dataAddress,
                                            int numRegisters, boolean readFromCache) {
        if (readFromCache) {
            ModbusCache cache = getCache(modbusConnection.getDeviceId());
            return cache.getHoldingRegisterValues(dataAddress, numRegisters);
        } else {
            return modbusConnection.readHoldingRegisters(slaveAddress, dataAddress, numRegisters);
        }
    }

    private CollectedValue readBytesFromHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {
        Register[] registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, ONE_REGISTER, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToByteArray(registers[0]), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readShortFromHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, ONE_REGISTER, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegisterToShort(registers[0]), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readIntegerFromTwoHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, TWO_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToInteger(registers), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readFloatFromTwoHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, TWO_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToFloat(registers), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readLongFromFourHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, FOUR_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToLong(registers), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readDoubleFromFourHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        Register[] registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, FOUR_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(System.currentTimeMillis(), converter.convertRegistersToDouble(registers), null,
                modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readNFromHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, int numRegisters) {
        // get cache corresponding to the deviceId of the request
        ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());

        // max registers holding register per request = 125
        int numRegistersToRequest = MAX_HOLDING_REGISTERS_PER_REQUEST;
        int finalAddress = dataAddress + numRegisters;
        for (int i = dataAddress; i < finalAddress; i = i + MAX_HOLDING_REGISTERS_PER_REQUEST) {

            if ((i + MAX_HOLDING_REGISTERS_PER_REQUEST) > finalAddress) {
                numRegistersToRequest = finalAddress - i;
            }

            LOGGER.info("Reading {} registers from device {} starting from address {}", numRegistersToRequest,
                    modbusConnection.getDeviceId(), i);

            Register[] registerValues = modbusConnection.readHoldingRegisters(slaveAddress, dataAddress, numRegistersToRequest);

            // save values read in cache
            modbusCache.setHoldingRegisterValues(registerValues, i);
        }

        return null;
    }

    private ModbusCache getCache(String deviceId) {
        // get cache corresponding to the deviceId of the request
        ModbusCache modbusCache = this.modbusCaches.get(deviceId);

        // if it doesn't exist a cache for the deviceId, create it
        if (modbusCache == null) {
            modbusCache = new ModbusCache();
            this.modbusCaches.put(deviceId, modbusCache);
        }

        return modbusCache;
    }
}
