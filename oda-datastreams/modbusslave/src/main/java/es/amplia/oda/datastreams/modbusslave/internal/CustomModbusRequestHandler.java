package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.net.ModbusTCPListener;
import com.ghgande.j2mod.modbus.procimg.*;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.datastreams.modbusslave.translator.ModbusToEventConverter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CustomModbusRequestHandler extends ModbusTCPListener {

    private final String deviceId;
    private final String deviceIp;
    private final StateManager stateManager;
    private final Map<Integer, ProcessImage> processImages = new HashMap<>();

    public CustomModbusRequestHandler(String deviceId, String deviceIp, int slaveAddress,  StateManager stateManager) {
        super(1);
        this.deviceId = deviceId;
        this.deviceIp = deviceIp;
        this.stateManager = stateManager;
        processImages.put(slaveAddress, iniProcessImage());
    }

    @Override
    public ProcessImage getProcessImage(int unitId) {
        return processImages.get(unitId);
    }

    @Override
    public void handleRequest(AbstractModbusTransport transport, AbstractModbusListener listener) throws ModbusIOException {
        if (transport == null) {
            throw new ModbusIOException("No transport specified");
        }
        ModbusRequest request = transport.readRequest(listener);
        if (request == null) {
            throw new ModbusIOException("Request for transport %s is invalid (null)", transport.getClass().getSimpleName());
        }
        log.info("Received modbus request from device {} ({}) and slave address {}", this.deviceId, this.deviceIp,
                request.getUnitID());

        // prepare response
        ModbusResponse response;

        // Test if Process image exists and has a correct unit ID (slave address)
        // UnitId = 0 equals broadcast
        ProcessImage spi = processImages.get(request.getUnitID());
        // check if we have an image to write registers associated to the request unitId
        if (spi == null ) {
            log.warn("There is no image to write data associated to slaveAddress {}", request.getUnitID());
            response = request.createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        }
        // check that the unitId is not broadcast and the one from the request equals the one configured
        else if (spi.getUnitID() != 0 && request.getUnitID() != spi.getUnitID()) {
            log.warn("SlaveAddress = {} is different than request slaveAddress = {}", spi.getUnitID(), request.getUnitID());
            response = request.createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        } else {
            response = request.createResponse(listener);
        }

        // Write the response
        transport.writeMessage(response);

        // translate request to ODA events
        List<Event> eventsConverted = ModbusToEventConverter.translateEvent(this.deviceId, request);
        // pass translated request to state manager
        stateManager.onReceivedEvents(eventsConverted);
    }

    private SimpleProcessImage iniProcessImage(){
        SimpleProcessImage processImage = new SimpleProcessImage();

        // max datapoints according to protocol is 65536
        // Coils (00001-065536) for boolean flags
        // Discrete Inputs (100001-165536) for read-only bools
        // Input Registers (300001-365536) for read-only words
        // Holding Registers (400001-465536) for read/write words
        int maxDatapoints = 65536;
        for (int i = 0; i < maxDatapoints; i++) {
            processImage.addRegister(new SimpleRegister(0));
            processImage.addInputRegister(new SimpleInputRegister(0));
            processImage.addDigitalOut(new SimpleDigitalOut(false));
            processImage.addDigitalIn(new SimpleDigitalIn(false));
        }

        return processImage;
    }
}
