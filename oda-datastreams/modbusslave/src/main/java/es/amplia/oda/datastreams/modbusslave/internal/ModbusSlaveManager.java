package es.amplia.oda.datastreams.modbusslave.internal;

import com.ghgande.j2mod.modbus.Modbus;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.datastreams.modbusslave.configuration.ModbusSlaveConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbusslave.configuration.ModbusTCPDeviceConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ModbusSlaveManager {

    private final StateManager stateManager;
    // <ip, requestHandler>
    Map<String, CustomModbusRequestHandler> modbusRequestHandlers = new HashMap<>();
    // <listenPort, tcpListener>
    Map<Integer, ModbusCustomTCPListener> modbusPortListeners = new HashMap<>();

    public ModbusSlaveManager(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    public void loadConfiguration(Map<String, List<Object>> modbusSlaves) {
        // clear map
        modbusRequestHandlers.clear();
        // stop listeners and clear map
        close();
        modbusPortListeners.clear();

        // load tcp devices
        List<Object> tcpModbusDevicesConf = modbusSlaves.get(ModbusSlaveConfigurationUpdateHandler.TCP_MODBUS_TYPE);
        if (tcpModbusDevicesConf == null) {
            return;
        }

        for (Object o : tcpModbusDevicesConf) {
            ModbusTCPDeviceConfiguration conf = (ModbusTCPDeviceConfiguration) o;

            // crear request handler para cada dispositivo definido
            CustomModbusRequestHandler requestHandler = new CustomModbusRequestHandler(conf.getDeviceId(), conf.getIpAddress(),
                    conf.getSlaveAddress(), this.stateManager);
            modbusRequestHandlers.put(conf.getIpAddress(), requestHandler);

            // crear listeners por cada puerto definido
            int listenPort = conf.getListenPort();
            // comprobamos si ya existe
            ModbusCustomTCPListener portListener = modbusPortListeners.get(listenPort);
            // si no existe un listener para ese puerto, se crea
            if(portListener == null){
                modbusPortListeners.put(listenPort, createCustomTcpListener(listenPort));
            }
        }

        // start listeners
        startListeners();
    }

    private ModbusCustomTCPListener createCustomTcpListener(int listenPort){
        // Crear listener de sockets personalizado
        ModbusCustomTCPListener listener = new ModbusCustomTCPListener(1, modbusRequestHandlers);

        // Configurar puerto y dirección en los que escuchar
        listener.setPort(listenPort);
        try {
            // escuchar en la dirección local
            listener.setAddress(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            log.error("Error creating listener in local address :", e);
        }

        // establecer timeout
        listener.setTimeout(Modbus.DEFAULT_TIMEOUT);

        return listener;
    }

    private void startListeners(){
        for (Map.Entry<Integer, ModbusCustomTCPListener> entry : modbusPortListeners.entrySet()) {
            log.info("Starting tcp listener in port {}", entry.getKey());
            ModbusCustomTCPListener listener = entry.getValue();

            // lanzar en un nuevo hilo el listener
            listener.setListening(true);
            Thread listenerThread = new Thread(listener);
            listenerThread.start();
        }
    }

    public void close(){
        for (Map.Entry<Integer, ModbusCustomTCPListener> entry : modbusPortListeners.entrySet()) {
            log.info("Stopping tcp listener in port {}", entry.getKey());
            ModbusCustomTCPListener listener = entry.getValue();
            // detener el listener
            listener.stop();
        }
    }
}
