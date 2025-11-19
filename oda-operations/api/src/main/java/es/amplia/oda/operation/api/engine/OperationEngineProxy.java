package es.amplia.oda.operation.api.engine;

import java.util.Map;

import org.osgi.framework.BundleContext;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.core.commons.utils.OsgiContext;
import es.amplia.oda.core.commons.utils.operation.response.Response;

public class OperationEngineProxy implements OperationEngine, AutoCloseable {

    private final OsgiServiceProxy<OperationEngine> proxy;

    public OperationEngineProxy (BundleContext bundleContext) {
        this.proxy = new OsgiServiceProxy<>(OperationEngine.class, bundleContext);
    }

    @Override
    public void close() {
        proxy.close();
    }

    @Override
    public Response engine(String operationName, String deviceId, String operationId, Map<String, Object> params,
            OsgiContext ctx) throws OperationNotFound {
        return proxy.callFirst(opEngine -> opEngine.engine(operationName, deviceId, operationId, params, ctx));
    }

    @Override
    public void reloadAllOperations() {
        proxy.consumeFirst(OperationEngine::reloadAllOperations);
    }

    @Override
    public void stop() {
        proxy.consumeFirst(OperationEngine::stop);
    }

}
