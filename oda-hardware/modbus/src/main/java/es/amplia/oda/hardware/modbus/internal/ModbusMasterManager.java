package es.amplia.oda.hardware.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

import java.util.List;

public class ModbusMasterManager implements AutoCloseable {

    private final ServiceRegistrationManager<ModbusMaster> serviceRegistrationManager;

    public ModbusMasterManager(ServiceRegistrationManager<ModbusMaster> serviceRegistrationManager) {
        this.serviceRegistrationManager = serviceRegistrationManager;
    }

    public void loadConfiguration(List<ModbusMaster> modbusMaster) {
        serviceRegistrationManager.unregister();
        for (ModbusMaster master : modbusMaster) {
            serviceRegistrationManager.register(master);
        }
    }

    @Override
    public void close() {
        serviceRegistrationManager.unregister();
    }
}
