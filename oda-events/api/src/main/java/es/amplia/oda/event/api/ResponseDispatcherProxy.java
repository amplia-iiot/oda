package es.amplia.oda.event.api;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.core.commons.utils.operation.response.OperationResponse;

import org.osgi.framework.BundleContext;

public class ResponseDispatcherProxy implements ResponseDispatcher, AutoCloseable {

    private final OsgiServiceProxy<ResponseDispatcher> proxy;

    public ResponseDispatcherProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(ResponseDispatcher.class, bundleContext);
    }

    @Override
    public void publishResponse(OperationResponse response) {
        proxy.consumeAll(responseDispatcher -> responseDispatcher.publishResponse(response));
    }

    @Override
    public void close() {
        proxy.close();
    }
}
