package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;

import org.osgi.framework.BundleContext;

import java.util.Optional;

public class OpenGateConnectorProxy implements OpenGateConnector, AutoCloseable {

    private final OsgiServiceProxy<OpenGateConnector> proxy;

    public OpenGateConnectorProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(OpenGateConnector.class, bundleContext);
    }

    @Override
    public void uplink(byte[] payload, ContentType contentType) {
        proxy.consumeFirst(connector -> connector.uplink(payload, contentType));
    }

    @Override
    public boolean isConnected() {
        return Optional.ofNullable(proxy.callFirst(OpenGateConnector::isConnected)).orElse(false);
    }

    @Override
    public void close() {
        proxy.close();
    }
}
