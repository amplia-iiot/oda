package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.snmp.SnmpException;
import org.osgi.framework.BundleContext;

import java.util.Optional;

public class SnmpClientProxy implements SnmpClient, AutoCloseable {

    private static final String NO_SNMP_CLIENT_AVAILABLE_MESSAGE = "No Snmp Client available";

    private final OsgiServiceProxy<SnmpClient> proxy;

    public SnmpClientProxy(BundleContext bundleContext) {
        this.proxy = new OsgiServiceProxy<>(SnmpClient.class, bundleContext);
    }

    @Override
    public String getDeviceId() {
        return Optional.ofNullable(proxy.callFirst(SnmpClient::getDeviceId))
                .orElseThrow(() -> new SnmpException(NO_SNMP_CLIENT_AVAILABLE_MESSAGE));
    }

    @Override
    public String getValue(String OID) {
        return Optional.ofNullable(proxy.callFirst(snmpClient -> snmpClient.getValue(OID)))
                .orElseThrow(() -> new SnmpException(NO_SNMP_CLIENT_AVAILABLE_MESSAGE));
    }

    @Override
    public void setValue(String OID, String dataType, String newValue) {
        proxy.consumeFirst(snmpClient -> snmpClient.setValue(OID, dataType, newValue));
    }

    @Override
    public void disconnect() {
        proxy.consumeFirst(SnmpClient::disconnect);
    }

    @Override
    public void close() {
        proxy.close();
    }

}
