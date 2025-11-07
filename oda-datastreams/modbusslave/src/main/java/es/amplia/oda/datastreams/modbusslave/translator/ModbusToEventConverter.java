package es.amplia.oda.datastreams.modbusslave.translator;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.Register;
import es.amplia.oda.core.commons.utils.Event;
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
        String hexMessage = request.getHexMessage();
        int functionCode = request.getFunctionCode();

        int dataLength = request.getDataLength();
        int outputLength = request.getResponse().getOutputLength();

        log.info("Received modbus request from deviceId {}, slaveAddress {}, functionCode {}, message string {}," +
                        "message bytes {}, dataLength {}, outputLength {}", deviceId, slaveAddress, functionCode,
                hexMessage, message, dataLength, outputLength);

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

        int modbusAddress = request.getReference();
        boolean modbusValue = request.getCoil();
        log.info("Value {} from address {}", modbusValue, modbusAddress);

        // get translation info associated to this modbus address and deviceId
        TranslationEntry entry = ModbusEventTranslator.translate(modbusAddress, deviceId);
        if (entry == null) {
            log.info("There is no translation info for address {} and device {}", modbusAddress, deviceId);
            return eventsToReturn;
        }

        eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(), null, modbusValue));
        return eventsToReturn;
    }

    private static List<Event> translateWriteSingleRegisterRequest(String deviceId, WriteSingleRegisterRequest request) {
        List<Event> eventsToReturn = new ArrayList<>();

        int modbusAddress = request.getReference();
        Register modbusValue = request.getRegister();
        log.info("Value {} from address {}", modbusValue, modbusAddress);

        // get translation info associated to this modbus address and deviceId
        TranslationEntry entry = ModbusEventTranslator.translate(modbusAddress, deviceId);
        if (entry == null) {
            log.info("There is no translation info for address {} and device {}", modbusAddress, deviceId);
            return eventsToReturn;
        }

        Object valueConverted = ModbusTypeToJavaTypeConverter.convertRegister(modbusValue.toBytes(), entry.getDataType());
        if (valueConverted == null) {
            log.error("Error converting value {} from modbus to {} ", modbusValue, entry.getDataType());
        } else {
            eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(), null, valueConverted));
        }

        return eventsToReturn;
    }

    private static List<Event> translateWriteMultipleCoilRequest(String deviceId, WriteMultipleCoilsRequest request) {
        List<Event> eventsToReturn = new ArrayList<>();

        int modbusAddress = request.getReference();
        byte[] modbusValue = request.getCoils().getBytes();
        log.info("Value {} from starting address {}", modbusValue, modbusAddress);

        // TODO : devolver el valor como un byte y que se usen las reglas para separarlo en bits o meter en
        //  configuracion un parametro mas que indique el bit que es y generar N eventos

/*        for (byte value : modbusValue) {
            // get translation info associated to this modbus address and deviceId
            TranslationEntry entry = ModbusEventTranslator.translate(modbusAddress, deviceId);
            if (entry != null) {
                // translate each byte to an array of bits
                BitSet bitset = BitSet.valueOf(modbusValue);
                for (int i = 0; i < bitset.size(); i++) {
                    eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(), null, bitset.get(i)));
                }
                eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(), null, value));
            }

            // prepare for next address (assuming one address per byte)
            modbusAddress = modbusAddress + 1;
        }*/

        // return as a byte
        for (byte value : modbusValue) {
            // get translation info associated to this modbus address and deviceId
            TranslationEntry entry = ModbusEventTranslator.translate(modbusAddress, deviceId);
            if (entry != null) {
                eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(), null, value));
            }

            // prepare for next address (assuming one address per byte)
            modbusAddress = modbusAddress + 1;
        }
        return eventsToReturn;
    }

    private static List<Event> translateWriteMultipleRegisterRequest(String deviceId, WriteMultipleRegistersRequest request) {
        List<Event> eventsToReturn = new ArrayList<>();

        int startingModbusAddress = request.getReference();
        Register[] modbusValue = request.getRegisters();
        int numWords = request.getWordCount();
        log.info("Value {} from starting address {}. Num words {}", modbusValue, startingModbusAddress, numWords);

        // get translations from start address to start address + numRegisters
        List<TranslationEntry> entries = new ArrayList<>();
        int modbusAddress = startingModbusAddress;
        for (int i = 0; i < numWords; i++) {
            TranslationEntry entry = ModbusEventTranslator.translate(modbusAddress, deviceId);
            if (entry != null) {
                entries.add(entry);
            }
            modbusAddress = modbusAddress + 1;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (TranslationEntry entry : entries) {
            int startingRegister = entry.getModbusAddress() - startingModbusAddress;
            int numRegistersToGet = ModbusTypeToJavaTypeConverter.getNumRegisters(entry.getDataType());

            // clear byte array
            outputStream.reset();
            for (int i = startingRegister; i < startingRegister + numRegistersToGet; i++) {
                try {
                    outputStream.write(modbusValue[i].toBytes());
                } catch (IOException e) {
                    log.error("Error converting value {} from modbus to {} : ", modbusValue, entry.getDataType(), e);
                }
            }

            Object valueConverted = ModbusTypeToJavaTypeConverter.convertRegister(outputStream.toByteArray(), entry.getDataType());
            if (valueConverted == null) {
                log.error("Error converting value {} from modbus to {} ", modbusValue, entry.getDataType());
            } else {
                eventsToReturn.add(new Event(entry.getDatastreamId(), entry.getDeviceId(), null, entry.getFeed(), null, valueConverted));
            }
        }

        return eventsToReturn;
    }
}
