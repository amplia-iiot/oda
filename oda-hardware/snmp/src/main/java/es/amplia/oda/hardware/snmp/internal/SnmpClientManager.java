package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SnmpClientManager implements AutoCloseable {

    private final ServiceRegistrationManager<SnmpClient> serviceRegistrationManager;
    private List<SnmpClient> currentSnmpClients = new ArrayList<>();

    public SnmpClientManager(ServiceRegistrationManager<SnmpClient> serviceRegistrationManager) {
        this.serviceRegistrationManager = serviceRegistrationManager;
    }

    public void loadConfiguration(List<SnmpClient> snmpClients) {
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

    public void disconnectClients() {
        if (this.currentSnmpClients == null || this.currentSnmpClients.isEmpty()) {
            return;
        }

        for (SnmpClient client : this.currentSnmpClients) {
            log.info("Disconnecting snmp client with deviceId {}", client.getDeviceId());
            client.disconnect();
        }
    }
}
