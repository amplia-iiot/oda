package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

import java.util.List;

public class SnmpClientManager implements AutoCloseable {

    private final ServiceRegistrationManager<SnmpClient> serviceRegistrationManager;

    public SnmpClientManager(ServiceRegistrationManager<SnmpClient> serviceRegistrationManager) {
        this.serviceRegistrationManager = serviceRegistrationManager;
    }

    public void loadConfiguration(List<SnmpClient> snmpClients) {
        serviceRegistrationManager.unregister();
        for (SnmpClient client : snmpClients) {
            serviceRegistrationManager.register(client);
        }
    }

    @Override
    public void close() {
        serviceRegistrationManager.unregister();
    }
}
