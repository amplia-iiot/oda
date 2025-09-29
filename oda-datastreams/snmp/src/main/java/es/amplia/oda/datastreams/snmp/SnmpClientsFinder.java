package es.amplia.oda.datastreams.snmp;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ServiceLocator;
import es.amplia.oda.core.commons.utils.ServiceLocatorOsgi;
import org.osgi.framework.BundleContext;

import java.util.List;

public class SnmpClientsFinder {

    private final ServiceLocator<SnmpClient> snmpClientsLocator;

    public SnmpClientsFinder(BundleContext bundleContext) {
        this.snmpClientsLocator = new ServiceLocatorOsgi<>(bundleContext, SnmpClient.class);
    }

    public SnmpClient getSnmpClient(String deviceId) {

        // retrieve all OSGI services of SnmpClient class registered in the framework
        List<SnmpClient> snmpClientsRegistered = getAllSnmpClients();

        for (SnmpClient client : snmpClientsRegistered) {
            if (client.getDeviceId().equals(deviceId)) {
                return client;
            }
        }

        return null;
    }

    public List<SnmpClient> getAllSnmpClients(){
        // retrieve all OSGI services of SnmpClient class registered in the framework
        return snmpClientsLocator.findAll();
    }

    public void close() {
        snmpClientsLocator.close();
    }

}
