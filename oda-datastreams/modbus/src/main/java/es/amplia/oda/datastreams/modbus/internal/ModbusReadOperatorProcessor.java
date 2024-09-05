package es.amplia.oda.datastreams.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusException;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.modbus.Register;
import es.amplia.oda.datastreams.modbus.ModbusConnectionsFinder;
import es.amplia.oda.datastreams.modbus.ModbusType;
import es.amplia.oda.datastreams.modbus.cache.ModbusCache;
import es.amplia.oda.datastreams.modbus.cache.ModbusCacheRegister;
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
    Map<String, ModbusCache> modbusCaches = new HashMap<>();

    ModbusReadOperatorProcessor(ModbusConnectionsFinder modbusConnectionsLocator, ModbusTypeToJavaTypeConverter converter) {
        this.modbusConnectionsLocator = modbusConnectionsLocator;
        this.converter = converter;
        updateModbusCaches(modbusConnectionsLocator);
    }

    public void updateModbusCaches(ModbusConnectionsFinder modbusConnectionsLocator) {
        // clear all existing caches
        this.modbusCaches.clear();

        // get all existing devices
        List<ModbusMaster> modbusConnections = modbusConnectionsLocator.getAllModbusConnections();

        for (ModbusMaster modbusConn : modbusConnections) {
            // create cache for every deviceId
            this.modbusCaches.put(modbusConn.getDeviceId(), new ModbusCache());
        }
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

        if (readFromCache) {
            // get cache corresponding to the deviceId of the request
            ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());
            if (modbusCache == null) {
                return null;
            }

            List<ModbusCacheRegister> registersFromCache = modbusCache.getInputDiscreteValues(dataAddress, ONE_REGISTER);
            if (registersFromCache == null) {
                return null;
            }

            Boolean[] registerValues = new Boolean[registersFromCache.size()];
            for (int i = 0; i < registersFromCache.size(); i++) {
                registerValues[i] = (Boolean) registersFromCache.get(i).getRegister();
            }

            return new CollectedValue(registersFromCache.get(0).getAt(), registerValues[0], null,
                    modbusConnection.getDeviceManufacturer());

        } else {
            boolean registerValue = modbusConnection.readInputDiscrete(slaveAddress, dataAddress);
            return new CollectedValue(System.currentTimeMillis(), registerValue, null, modbusConnection.getDeviceManufacturer());
        }
    }

    private CollectedValue readNFromInputDiscrete(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, int numRegisters) {
        // get cache corresponding to the deviceId of the request
        ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());
        if (modbusCache == null) {
            return null;
        }

        // all registers will be saved with the same datetime
        long at = System.currentTimeMillis();

        int finalAddress = dataAddress + numRegisters;
        int numRegistersToRequest;
        int i = dataAddress;
        do {
            // get number of registers to request in this iteration
            numRegistersToRequest = getNumRegistersToRequest(numRegisters, MAX_INPUT_DISCRETE_PER_REQUEST, i, finalAddress);

            try {
                Boolean[] registerValues = modbusConnection.readInputDiscretes(slaveAddress, i, numRegistersToRequest);
                // save values read in cache
                modbusCache.setInputDiscreteValues(registerValues, i, at);
            } catch (ModbusException e) {
                // error reading registers, set all values in cache of that block to null
                modbusCache.emptyInputDiscreteValues(i, numRegistersToRequest);
            }

            // update start address
            i = i + numRegistersToRequest;

        } while (numRegistersToRequest == MAX_INPUT_DISCRETE_PER_REQUEST);

        return null;
    }

    // COIL

    private CollectedValue readBooleanFromCoil(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        if (readFromCache) {
            // get cache corresponding to the deviceId of the request
            ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());
            if (modbusCache == null) {
                return null;
            }

            List<ModbusCacheRegister> registersFromCache = modbusCache.getCoilValues(dataAddress, ONE_REGISTER);
            if (registersFromCache == null) {
                return null;
            }

            Boolean[] registerValues = new Boolean[registersFromCache.size()];
            for (int i = 0; i < registersFromCache.size(); i++) {
                registerValues[i] = (Boolean) registersFromCache.get(i).getRegister();
            }

            return new CollectedValue(registersFromCache.get(0).getAt(), registerValues[0], null,
                    modbusConnection.getDeviceManufacturer());

        } else {
            boolean registerValue = modbusConnection.readCoil(slaveAddress, dataAddress);
            return new CollectedValue(System.currentTimeMillis(), registerValue, null, modbusConnection.getDeviceManufacturer());
        }
    }

    private CollectedValue readNFromCoil(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, int numRegisters) {
        // get cache corresponding to the deviceId of the request
        ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());
        if (modbusCache == null) {
            return null;
        }

        // all registers will be saved with the same datetime
        long at = System.currentTimeMillis();

        int finalAddress = dataAddress + numRegisters;
        int numRegistersToRequest;
        int i = dataAddress;
        do {
            // get number of registers to request in this iteration
            numRegistersToRequest = getNumRegistersToRequest(numRegisters, MAX_COIL_PER_REQUEST, i, finalAddress);

            try {
                Boolean[] registerValues = modbusConnection.readCoils(slaveAddress, i, numRegistersToRequest);
                // save values read in cache
                modbusCache.setCoilValues(registerValues, i, at);
            } catch (ModbusException e) {
                // error reading registers, set all values in cache of that block to null
                modbusCache.emptyCoilValues(i, numRegistersToRequest);
            }

            // update start address
            i = i + numRegistersToRequest;

        } while (numRegistersToRequest == MAX_COIL_PER_REQUEST);

        return null;
    }

    // INPUT REGISTERS

    private ModbusReadRegister readInputRegisters(ModbusMaster modbusConnection, int slaveAddress, int dataAddress,
                                                  int numRegisters, boolean readFromCache) {
        if (readFromCache) {
            // get cache corresponding to the deviceId of the request
            ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());
            if (modbusCache == null) {
                return null;
            }

            List<ModbusCacheRegister> registersFromCache = modbusCache.getInputRegisterValues(dataAddress, numRegisters);
            if (registersFromCache == null) {
                return null;
            }

            Register[] registerValues = new Register[registersFromCache.size()];
            for (int i = 0; i < registersFromCache.size(); i++) {
                registerValues[i] = (Register) registersFromCache.get(i).getRegister();
            }

            return new ModbusReadRegister(registersFromCache.get(0).getAt(), registerValues);

        } else {
            Register[] registerValues = modbusConnection.readInputRegisters(slaveAddress, dataAddress, numRegisters);
            return new ModbusReadRegister(System.currentTimeMillis(), registerValues);
        }
    }

    private CollectedValue readBytesFromInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, ONE_REGISTER, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegisterToByteArray(((Register[]) registers.getValue())[0]),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readShortFromInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, ONE_REGISTER, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegisterToShort(((Register[]) registers.getValue())[0]),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readIntegerFromTwoInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, TWO_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegistersToInteger((Register[]) registers.getValue()),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readFloatFromTwoInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, TWO_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegistersToFloat((Register[]) registers.getValue()),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readLongFromFourInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, FOUR_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegistersToLong((Register[]) registers.getValue()),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readDoubleFromFourInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readInputRegisters(modbusConnection, slaveAddress, dataAddress, FOUR_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegistersToDouble((Register[]) registers.getValue()),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readNFromInputRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, int numRegisters) {
        // get cache corresponding to the deviceId of the request
        ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());
        if (modbusCache == null) {
            return null;
        }

        // all registers will be saved with the same datetime
        long at = System.currentTimeMillis();

        int finalAddress = dataAddress + numRegisters;
        int numRegistersToRequest;
        int i = dataAddress;
        do {
            // get number of registers to request in this iteration
            numRegistersToRequest = getNumRegistersToRequest(numRegisters, MAX_INPUT_REGISTER_PER_REQUEST, i, finalAddress);

            try {
                Register[] registerValues = modbusConnection.readInputRegisters(slaveAddress, i, numRegistersToRequest);
                // save values read in cache
                modbusCache.setInputRegisterValues(registerValues, i, at);
            } catch (ModbusException e) {
                // error reading registers, set all values in cache of that block to null
                modbusCache.emptyInputRegisterValues(i, numRegistersToRequest);
            }

            // update start address
            i = i + numRegistersToRequest;

        } while (numRegistersToRequest == MAX_INPUT_REGISTER_PER_REQUEST);

        return null;
    }

    // HOLDING REGISTERS

    private ModbusReadRegister readHoldingRegisters(ModbusMaster modbusConnection, int slaveAddress, int dataAddress,
                                                    int numRegisters, boolean readFromCache) {
        if (readFromCache) {
            // get cache corresponding to the deviceId of the request
            ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());
            if (modbusCache == null) {
                return null;
            }

            List<ModbusCacheRegister> registersFromCache = modbusCache.getHoldingRegisterValues(dataAddress, numRegisters);
            if (registersFromCache == null) {
                return null;
            }

            Register[] registerValues = new Register[registersFromCache.size()];
            for (int i = 0; i < registersFromCache.size(); i++) {
                registerValues[i] = (Register) registersFromCache.get(i).getRegister();
            }

            return new ModbusReadRegister(registersFromCache.get(0).getAt(), registerValues);

        } else {
            Register[] registerValues = modbusConnection.readHoldingRegisters(slaveAddress, dataAddress, numRegisters);
            return new ModbusReadRegister(System.currentTimeMillis(), registerValues);
        }
    }

    private CollectedValue readBytesFromHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, ONE_REGISTER, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegisterToByteArray(((Register[]) registers.getValue())[0]),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readShortFromHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, ONE_REGISTER, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegisterToShort(((Register[]) registers.getValue())[0]),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readIntegerFromTwoHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, TWO_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegistersToInteger((Register[]) registers.getValue()),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readFloatFromTwoHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, TWO_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegistersToFloat((Register[]) registers.getValue()),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readLongFromFourHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, FOUR_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegistersToLong((Register[]) registers.getValue()),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readDoubleFromFourHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress
            , boolean readFromCache) {

        ModbusReadRegister registers = readHoldingRegisters(modbusConnection, slaveAddress, dataAddress, FOUR_REGISTERS, readFromCache);

        if (registers == null) {
            return null;
        }

        return new CollectedValue(registers.getAt(), converter.convertRegistersToDouble((Register[]) registers.getValue()),
                null, modbusConnection.getDeviceManufacturer());
    }

    private CollectedValue readNFromHoldingRegister(ModbusMaster modbusConnection, int slaveAddress, int dataAddress, int numRegisters) {
        // get cache corresponding to the deviceId of the request
        ModbusCache modbusCache = getCache(modbusConnection.getDeviceId());
        if (modbusCache == null) {
            return null;
        }

        // all registers will be saved with the same datetime
        long at = System.currentTimeMillis();

        int finalAddress = dataAddress + numRegisters;
        int numRegistersToRequest;
        int i = dataAddress;
        do {
            // get number of registers to request in this iteration
            numRegistersToRequest = getNumRegistersToRequest(numRegisters, MAX_HOLDING_REGISTERS_PER_REQUEST, i, finalAddress);

            try {
                Register[] registerValues = modbusConnection.readHoldingRegisters(slaveAddress, i, numRegistersToRequest);
                // save values read in cache
                modbusCache.setHoldingRegisterValues(registerValues, i, at);
            } catch (ModbusException e) {
                // error reading registers, set all values in cache of that block to null
                modbusCache.emptyHoldingRegisterValues(i, numRegistersToRequest);
            }

            // update start address
            i = i + numRegistersToRequest;

        } while (numRegistersToRequest == MAX_HOLDING_REGISTERS_PER_REQUEST);

        return null;
    }

    private ModbusCache getCache(String deviceId) {
        // get cache corresponding to the deviceId of the request
        ModbusCache modbusCache = this.modbusCaches.get(deviceId);

        // if cache for this device doesn't exist
        if (modbusCache == null) {
            LOGGER.error("Cache for deviceId {} doesn't exist", deviceId);
            return null;
        }

        return modbusCache;
    }

    private int getNumRegistersToRequest(int numRegistersTotal, int maxRegistersPerRequest, int currentStartAddress, int finalAddress) {
        if (numRegistersTotal <= maxRegistersPerRequest) {
            return numRegistersTotal;
        }

        if ((currentStartAddress + maxRegistersPerRequest) > finalAddress) {
            return finalAddress - currentStartAddress;
        }

        return maxRegistersPerRequest;
    }
}
