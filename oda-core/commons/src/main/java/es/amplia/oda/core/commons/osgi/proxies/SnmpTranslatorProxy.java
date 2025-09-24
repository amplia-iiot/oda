package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.SnmpTranslator;
import es.amplia.oda.core.commons.snmp.SnmpEntry;
import org.osgi.framework.BundleContext;

import java.util.Optional;

public class SnmpTranslatorProxy implements SnmpTranslator, AutoCloseable {

    private final OsgiServiceProxy<SnmpTranslator> proxy;

    public SnmpTranslatorProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(SnmpTranslator.class, bundleContext);
    }

    @Override
    public SnmpEntry translate(String OID, String deviceId) {
        Optional<SnmpEntry> snmpEntry = Optional.ofNullable(proxy.callFirst(translator -> translator.translate(OID, deviceId)));
        return snmpEntry.orElse(null);
    }

    @Override
    public void close(){
            proxy.close();
    }
}
