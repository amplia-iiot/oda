package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.ScadaConnector;

import org.osgi.framework.BundleContext;

import java.util.Optional;

public class ScadaConnectorProxy implements ScadaConnector, AutoCloseable {

    private final OsgiServiceProxy<ScadaConnector> proxy;

    public ScadaConnectorProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(ScadaConnector.class, bundleContext);
    }

    @Override
    public <T, S> void uplink(int index, T value, S type, long timestamp) {
        proxy.consumeFirst(connector -> connector.uplink(index, value, type, timestamp));
    }

    @Override
    public boolean isConnected() {
        return Optional.ofNullable(proxy.callFirst(ScadaConnector::isConnected)).orElse(false);
    }

    @Override
    public void close() {
        proxy.close();
    }
}
