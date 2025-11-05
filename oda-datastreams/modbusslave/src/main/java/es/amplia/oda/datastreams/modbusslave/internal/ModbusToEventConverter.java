package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.msg.*;
import es.amplia.oda.core.commons.utils.Event;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ModbusToEventConverter {

    public static List<Event> translateEvent(String deviceId, ModbusRequest request) {
        List<Event> eventsTranslated = new ArrayList<>();

        if (request == null) {
            return eventsTranslated;
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
                translateWriteCoilRequest((WriteCoilRequest) request);
                break;
            case Modbus.WRITE_SINGLE_REGISTER:
                translateWriteSingleRegisterRequest((WriteSingleRegisterRequest) request);
                break;
            case Modbus.WRITE_MULTIPLE_COILS:
                translateWriteMultipleCoilRequest((WriteMultipleCoilsRequest) request);
                break;
            case Modbus.WRITE_MULTIPLE_REGISTERS:
                translateWriteMultipleRegisterRequest((WriteMultipleRegistersRequest) request);
                break;
            default:
                log.error("Function code {} not supported", functionCode);
                break;
        }

        return eventsTranslated;
    }

    private static void translateWriteCoilRequest(WriteCoilRequest request){
        log.info("Value {} from address {}", request.getCoil(), request.getReference());
    }

    private static void translateWriteSingleRegisterRequest(WriteSingleRegisterRequest request){
        log.info("Value {} from address {}", request.getRegister(), request.getReference());
    }

    private static void translateWriteMultipleCoilRequest(WriteMultipleCoilsRequest request){
        log.info("Values {} from address {}", request.getCoils(), request.getReference());
    }

    private static void translateWriteMultipleRegisterRequest(WriteMultipleRegistersRequest request){
        log.info("Values {} from address {}", request.getRegisters(), request.getReference());
    }
}
