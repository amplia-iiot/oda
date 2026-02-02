package es.amplia.oda.datastreams.modbusslave.translator;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.BitVector;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.datastreams.modbusslave.ModbusSlaveCounters;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
public class ModbusToEventConverter {

    public static List<Event> translateEvent(String deviceId, ModbusRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }

        // retrieve data from request
        int slaveAddress = request.getUnitID();
        byte[] message = request.getMessage();
        int functionCode = request.getFunctionCode();

        log.debug("Received modbus request from deviceId {}, slaveAddress {}, functionCode {}, message {}", deviceId,
                slaveAddress, functionCode, message);

        switch (functionCode) {
            case Modbus.WRITE_COIL:
                return translateWriteCoilRequest(deviceId, (WriteCoilRequest) request);
            case Modbus.WRITE_SINGLE_REGISTER:
                return translateWriteSingleRegisterRequest(deviceId, (WriteSingleRegisterRequest) request);
            case Modbus.WRITE_MULTIPLE_COILS:
                return translateWriteMultipleCoilRequest(deviceId, (WriteMultipleCoilsRequest) request);
            case Modbus.WRITE_MULTIPLE_REGISTERS:
                return translateWriteMultipleRegisterRequest(deviceId, (WriteMultipleRegistersRequest) request);
            default:
                log.error("Function code {} not supported", functionCode);
                break;
        }

        return Collections.emptyList();
    }

    private static List<Event> translateWriteCoilRequest(String deviceId, WriteCoilRequest request) {
        List<Event> eventsToReturn = new ArrayList<>();

        // increment counter
        ModbusSlaveCounters.incrCounter(ModbusSlaveCounters.ModbusCounterType.MODBUS_RECEIVED_WRITE_COIL, deviceId, 1);

        int modbusAddress = request.getReference();
        boolean modbusValue = request.getCoil();
        log.debug("Value {} from address {}", modbusValue, modbusAddress);

        // get translation info associated to this modbus address and deviceId
        List<TranslationEntry> entries = ModbusEventTranslator.getExistingNonBlockTranslations(modbusAddress, deviceId);
        for(TranslationEntry entry : entries){
            eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(), null, modbusValue));
        }

        // get block translations
        List<TranslationEntry> entriesBlocks = ModbusEventTranslator.getExistingBlockTranslations(modbusAddress, modbusAddress, deviceId);
        for (TranslationEntry entry : entriesBlocks) {
            eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(), null, modbusValue));
        }

        if (eventsToReturn.isEmpty()) {
            log.debug("There are no translations info for address {} and device {}", modbusAddress, deviceId);
            return eventsToReturn;
        }

        return eventsToReturn;
    }

    private static List<Event> translateWriteSingleRegisterRequest(String deviceId, WriteSingleRegisterRequest request) {
        List<Event> eventsToReturn = new ArrayList<>();

        // increment counter
        ModbusSlaveCounters.incrCounter(ModbusSlaveCounters.ModbusCounterType.MODBUS_RECEIVED_WRITE_REGISTER, deviceId, 1);

        int modbusAddress = request.getReference();
        Register modbusValue = request.getRegister();
        log.debug("Value {} from address {}", modbusValue, modbusAddress);

        // get translation info associated to this modbus address and deviceId
        List<TranslationEntry> entries = ModbusEventTranslator.getExistingNonBlockTranslations(modbusAddress, deviceId);
        for (TranslationEntry entry : entries) {
            Object valueConverted = ModbusToJavaTypeConverter.convertRegister(modbusValue.toBytes(), entry.getDataType());
            if (valueConverted == null) {
                log.error("Error converting value {} from modbus to {} ", modbusValue, entry.getDataType());
            } else {
                eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(), null, valueConverted));
            }
        }

        // get block translations
        List<TranslationEntry> entriesBlocks = ModbusEventTranslator.getExistingBlockTranslations(modbusAddress, modbusAddress, deviceId);
        for (TranslationEntry entry : entriesBlocks) {
            Object valueConverted = ModbusToJavaTypeConverter.convertRegister(modbusValue.toBytes(), entry.getDataType());
            if (valueConverted == null) {
                log.error("Error converting value {} from modbus to {} ", modbusValue, entry.getDataType());
            } else {
                eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(), null, valueConverted));
            }
        }

        if (eventsToReturn.isEmpty()) {
            log.debug("There are no translations info for address {} and device {}", modbusAddress, deviceId);
            return eventsToReturn;
        }

        return eventsToReturn;
    }

    private static List<Event> translateWriteMultipleCoilRequest(String deviceId, WriteMultipleCoilsRequest request) {
        List<Event> eventsToReturn = new ArrayList<>();

        // increment counter
        ModbusSlaveCounters.incrCounter(ModbusSlaveCounters.ModbusCounterType.MODBUS_RECEIVED_WRITE_COILS, deviceId, 1);

        int startingModbusAddress = request.getReference();
        BitVector coilsArray = request.getCoils();
        int numCoils = coilsArray.size();
        log.debug("Value {} from starting address {}", coilsArray, startingModbusAddress);

        List<TranslationEntry> entries = new ArrayList<>();
        int modbusAddress = startingModbusAddress;
        for (int i = 0; i < numCoils; i++) {
            List<TranslationEntry> entriesForAddressI = ModbusEventTranslator.getExistingNonBlockTranslations(modbusAddress, deviceId);
            if (entriesForAddressI != null && !entriesForAddressI.isEmpty()) {
                entries.addAll(entriesForAddressI);
            }
            modbusAddress = modbusAddress + 1;
        }

        // for every single translation, parse events
        for (TranslationEntry entry : entries) {
            int bitToRetrieve = entry.getStartModbusAddress() - startingModbusAddress;
            try {
                Boolean valueConverted = coilsArray.getBit(bitToRetrieve);
                eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(),
                        null, valueConverted));
            } catch (Exception e) {
                log.error("Error converting value {} from modbus to {} : ", coilsArray, entry.getDataType(), e);
            }
        }

        // get block translations
        List<TranslationEntry> entriesBlocks = ModbusEventTranslator.getExistingBlockTranslations(startingModbusAddress,
                startingModbusAddress, deviceId);
        for (TranslationEntry entry : entriesBlocks) {
            eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(),
                    null, coilsArray.getBytes()));
        }

        if (eventsToReturn.isEmpty()) {
            log.debug("There are no translations info for address {} and device {}", modbusAddress, deviceId);
            return eventsToReturn;
        }

        return eventsToReturn;
    }

    private static List<Event> translateWriteMultipleRegisterRequest(String deviceId, WriteMultipleRegistersRequest request) {
        List<Event> eventsToReturn = new ArrayList<>();

        // increment counter
        ModbusSlaveCounters.incrCounter(ModbusSlaveCounters.ModbusCounterType.MODBUS_RECEIVED_WRITE_REGISTERS, deviceId, 1);

        int startingModbusAddress = request.getReference();
        Register[] modbusValue = request.getRegisters();
        int numWords = request.getWordCount();
        log.debug("Value {} from starting address {}. Num words {}", modbusValue, startingModbusAddress, numWords);

        // get single translations from start address to start address + numRegisters
        List<TranslationEntry> entries = new ArrayList<>();
        int modbusAddress = startingModbusAddress;
        for (int i = 0; i < numWords; i++) {
            List<TranslationEntry> entriesForAddressI = ModbusEventTranslator.getExistingNonBlockTranslations(modbusAddress, deviceId);
            if (entriesForAddressI != null && !entriesForAddressI.isEmpty()) {
                entries.addAll(entriesForAddressI);
            }
            modbusAddress = modbusAddress + 1;
        }

        // for every single translation, parse events
        for (TranslationEntry entry : entries) {
            int startingRegister = entry.getStartModbusAddress() - startingModbusAddress;
            int numRegistersToGet = ModbusToJavaTypeConverter.getNumRegisters(entry.getDataType(), numWords);
            try {
                byte[] convertedValue = convertRegistersToByteArray(Arrays.copyOfRange(modbusValue, startingRegister, startingRegister + numRegistersToGet));
                Object valueConverted = ModbusToJavaTypeConverter.convertRegister(convertedValue, entry.getDataType());
                if (valueConverted == null) {
                    log.error("Error converting value {} from modbus to {} ", modbusValue, entry.getDataType());
                } else {
                    eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(),
                            null, valueConverted));
                }
            } catch (IOException e) {
                log.error("Error converting value {} from modbus to {} : ", modbusValue, entry.getDataType(), e);
            }
        }

        // get block translations
        List<TranslationEntry> entriesBlocks = ModbusEventTranslator.getExistingBlockTranslations(startingModbusAddress,
                startingModbusAddress, deviceId);
        for (TranslationEntry entry : entriesBlocks) {
            byte[] convertedValue = null;
            try {
                int startingRegister = (entry.getStartModbusAddress() < startingModbusAddress) ? 0 : (entry.getStartModbusAddress() - startingModbusAddress);
                convertedValue = convertRegistersToByteArray(Arrays.copyOfRange(modbusValue, startingRegister, startingRegister + numWords));
            } catch (IOException e) {
                log.error("Error converting value {} from modbus to {} : ", modbusValue, entry.getDataType(), e);
            }

            if (convertedValue != null) {
                eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(),
                        null, convertedValue));
            }
        }

        if (eventsToReturn.isEmpty()) {
            log.debug("There are no translations info for address {} and device {}", startingModbusAddress, deviceId);
            return eventsToReturn;
        }

        return eventsToReturn;
    }

    private static byte[] convertRegistersToByteArray(Register[] registers) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (Register register : registers) {
            outputStream.write(register.toBytes());
        }
        return outputStream.toByteArray();
    }
}
