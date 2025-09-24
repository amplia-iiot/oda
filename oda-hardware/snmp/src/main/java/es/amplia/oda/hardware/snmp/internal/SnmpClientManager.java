package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

import java.util.ArrayList;
import java.util.List;

public class SnmpClientManager implements AutoCloseable {

    private final ServiceRegistrationManager<SnmpClient> serviceRegistrationManager;
    private List<SnmpClient> currentSnmpClients = new ArrayList<>();

    public SnmpClientManager(ServiceRegistrationManager<SnmpClient> serviceRegistrationManager) {
        this.serviceRegistrationManager = serviceRegistrationManager;
    }

    public void loadConfiguration(List<SnmpClient> snmpClients) {
        disconnectClients();
        this.currentSnmpClients.clear();
        serviceRegistrationManager.unregister();

        this.currentSnmpClients = snmpClients;
        for (SnmpClient client : this.currentSnmpClients) {
            serviceRegistrationManager.register(client);
        }
    }

    @Override
    public void close() {
        disconnectClients();
        serviceRegistrationManager.unregister();
    }

    private void disconnectClients() {
        if (this.currentSnmpClients == null) {
            return;
        }

        for (SnmpClient client : this.currentSnmpClients) {
            client.disconnect();
        }
    }
}
