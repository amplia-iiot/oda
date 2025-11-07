package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.io.AbstractModbusTransport;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.net.AbstractModbusListener;
import com.ghgande.j2mod.modbus.net.ModbusTCPListener;
import com.ghgande.j2mod.modbus.procimg.ProcessImage;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.datastreams.modbusslave.translator.ModbusToEventConverter;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModbusListenerImpl extends ModbusTCPListener{

    private final String deviceId;
    private final StateManager stateManager;
    private final Map<Integer, ProcessImage> processImages = new HashMap<>();

    public ModbusListenerImpl(String deviceId, InetAddress address, StateManager stateManager) {
        super(1, address);
        this.deviceId = deviceId;
        this.stateManager = stateManager;
    }

    public void addProcessImage(int unitId, ProcessImage processImage) {
        processImages.put(unitId, processImage);
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

        log.info("Received modbus request from device {}", this.deviceId);
        // TODO : augment counter

        // translate request to ODA events
        List<Event> eventsConverted = ModbusToEventConverter.translateEvent(this.deviceId, request);
        // pass translated request to SCADA translator and state manager
        stateManager.onReceivedEvents(eventsConverted);

        // prepare response
        ModbusResponse response;

        // Test if Process image exists and has a correct unit ID
        ProcessImage spi = processImages.get(request.getUnitID());
        if (spi == null || (spi.getUnitID() != 0 && request.getUnitID() != spi.getUnitID())) {
            response = request.createExceptionResponse(Modbus.ILLEGAL_ADDRESS_EXCEPTION);
        } else {
            response = request.createResponse(this);
        }

        // Write the response
        transport.writeMessage(response);
    }
}
