package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ServiceLocator;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModbusConnectionsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModbusConnectionsManager.class);

    private final Map<String, ModbusMaster> modbusConnections = new ConcurrentHashMap<>();

    private final ServiceLocator<ModbusMaster> modbusConnectionsLocator;

    ModbusConnectionsManager(BundleContext bundleContext)
    {
        // initiate locator
        this.modbusConnectionsLocator = new ServiceLocatorOsgi<>(bundleContext, ModbusMaster.class);
    }

    public ModbusMaster getModbusConnectionWithId(String id) {
        return modbusConnections.get(id);
    }

    public List<ModbusMaster> getNewModbusConnections() {
        // retrieve all OSGI services of ModbusMaster class registered in the framework
        return modbusConnectionsLocator.findAll();
    }

    public void addModbusConnection(ModbusMaster modbusConnection) {
        modbusConnections.put(modbusConnection.getDeviceId(), modbusConnection);
    }

    public void connectAll() {
        for (Map.Entry<String, ModbusMaster> entry : modbusConnections.entrySet()) {
            LOGGER.info("Connecting with Modbus device {}", entry.getKey());
            entry.getValue().connect();
        }
    }

    public void addAllModbusConnection() {
        // remove all connections that were stored before
        modbusConnections.clear();

        // retrieve all OSGI services of ModbusMaster class registered in the framework
        List<ModbusMaster> listModbusMaster = getNewModbusConnections();

        // add modbus connections to map
        listModbusMaster.forEach(this::addModbusConnection);

        // establish connection with devices
        connectAll();
    }

    public void close() {
        modbusConnectionsLocator.close();
    }

}
