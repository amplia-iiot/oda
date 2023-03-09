package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ServiceLocator;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import org.osgi.framework.BundleContext;

import java.util.List;

public class ModbusConnectionsFinder {

    private final ServiceLocator<ModbusMaster> modbusConnectionsLocator;

    ModbusConnectionsFinder(BundleContext bundleContext)
    {
        // initiate locator
        this.modbusConnectionsLocator = new ServiceLocatorOsgi<>(bundleContext, ModbusMaster.class);
    }

    public ModbusMaster getModbusConnectionWithId(String id) {

        // retrieve all OSGI services of ModbusMaster class registered in the framework
        List<ModbusMaster> modbusConnectionsRegistered = getAllModbusConnections();

        for(ModbusMaster modbusConnection : modbusConnectionsRegistered)
        {
            if(modbusConnection.getDeviceId().equals(id)){
                return modbusConnection;
            }
        }

        return null;
    }

    public List<ModbusMaster> getAllModbusConnections(){
        // retrieve all OSGI services of ModbusMaster class registered in the framework
        return modbusConnectionsLocator.findAll();
    }

    public void connect() {

        // retrieve all OSGI services of ModbusMaster class registered in the framework
        List<ModbusMaster> modbusConnectionsRegistered = getAllModbusConnections();

        for (ModbusMaster modbusConnection : modbusConnectionsRegistered) {
            modbusConnection.connect();
        }
    }

    public void disconnect() {

        // retrieve all OSGI services of ModbusMaster class registered in the framework
        List<ModbusMaster> modbusConnectionsRegistered = getAllModbusConnections();

        for (ModbusMaster modbusConnection : modbusConnectionsRegistered) {
            modbusConnection.disconnect();
        }
    }

    public void close() {
        modbusConnectionsLocator.close();
    }

}
