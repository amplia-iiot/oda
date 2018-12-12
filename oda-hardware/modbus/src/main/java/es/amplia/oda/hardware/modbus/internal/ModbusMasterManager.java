package es.amplia.oda.hardware.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

public class ModbusMasterManager implements AutoCloseable {

    private final ServiceRegistrationManager<ModbusMaster> serviceRegistrationManager;

    public ModbusMasterManager(ServiceRegistrationManager<ModbusMaster> serviceRegistrationManager) {
        this.serviceRegistrationManager = serviceRegistrationManager;
    }

    public void loadConfiguration(ModbusMaster modbusMaster) {
        serviceRegistrationManager.unregister();
        serviceRegistrationManager.register(modbusMaster);
    }

    @Override
    public void close() {
        serviceRegistrationManager.unregister();
    }
}
