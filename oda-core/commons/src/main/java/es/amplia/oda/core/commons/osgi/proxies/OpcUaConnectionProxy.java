package es.amplia.oda.core.commons.osgi.proxies;

import java.util.Optional;

import org.osgi.framework.BundleContext;

import es.amplia.oda.core.commons.opcua.OpcUaConnection;
import es.amplia.oda.core.commons.opcua.OpcUaException;

public class OpcUaConnectionProxy implements AutoCloseable, OpcUaConnection {
    private final OsgiServiceProxy<OpcUaConnection> proxy;
    private final String NO_OPC_UA_SERVICE = "No OPC-UA service available";

    public OpcUaConnectionProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(OpcUaConnection.class, bundleContext);
    }

    @Override
    public void connect() {
        proxy.consumeFirst(OpcUaConnection::connect);
    }

    @Override
    public String browse(String tab, String nodeId) {
        return Optional.ofNullable(proxy.callFirst(opcUaConn -> opcUaConn.browse(tab, nodeId)))
                .orElseThrow(() -> new OpcUaException(NO_OPC_UA_SERVICE));
    }

    @Override
    public String readNode(String nodeId) {
        return Optional.ofNullable(proxy.callFirst(opcUaConn -> opcUaConn.readNode(nodeId)))
                .orElseThrow(() -> new OpcUaException(NO_OPC_UA_SERVICE));
    }

    @Override
    public Object readVariable(String nodeId) {
        return Optional.ofNullable(proxy.callFirst(opcUaConn -> opcUaConn.readVariable(nodeId)))
                .orElseThrow(() -> new OpcUaException(NO_OPC_UA_SERVICE));
    }

    @Override
    public void writeVariable(String nodeId, Object value) {
        proxy.consumeFirst(opcUaConn -> opcUaConn.writeVariable(nodeId, value));
    }

    @Override
    public Object[] method(String objectId, String methodId, Object[] params) {
        return Optional.ofNullable(proxy.callFirst(opcUaConn -> opcUaConn.method(objectId, methodId, params)))
                .orElseThrow(() -> new OpcUaException(NO_OPC_UA_SERVICE));
    }

    @Override
    public void disconnect() {
        proxy.consumeFirst(OpcUaConnection::disconnect);
    }

    @Override
    public void close() {
        proxy.close();
    }
}
