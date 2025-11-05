package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import es.amplia.oda.core.commons.interfaces.StateManager;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class ModbusTCPSlaveImpl {

    private ModbusListenerImpl listener = null;
    private boolean isRunning;
    private final String deviceId;

    public ModbusTCPSlaveImpl(String deviceId, String ipAddress, int port, int slaveAddress, StateManager stateManager) throws ModbusException {
        log.debug("Creating modbus TCP slave listener for device {} in ipAddress {}, port {}, slaveAddress {}",
                deviceId, ipAddress, port, slaveAddress);

        this.deviceId = deviceId;

        // Create the listener
        try {
            InetAddress address = InetAddress.getLocalHost();
            listener = new ModbusListenerImpl(deviceId, address, stateManager);
            listener.setListening(true);
            listener.setAddress(address);
            listener.setPort(port);
            listener.setTimeout(0);

            // add process image (slave address)
            SimpleProcessImage spi = new SimpleProcessImage(slaveAddress);
            listener.addProcessImage(slaveAddress, spi);

        } catch (UnknownHostException e) {
            log.error("Error creating modbus slave listener");
        }
    }

    public void open() throws ModbusException {
        // Start the listener if it isn't already running
        if (!isRunning) {
            try {
                new Thread(listener).start();
                isRunning = true;
            }
            catch (Exception x) {
                if (listener != null) {
                    listener.stop();
                }
                throw new ModbusException(x.getMessage());
            }
        }
    }

    public void close() {
        if (listener != null && listener.isListening()) {
            listener.stop();
        }
        isRunning = false;
    }


}
