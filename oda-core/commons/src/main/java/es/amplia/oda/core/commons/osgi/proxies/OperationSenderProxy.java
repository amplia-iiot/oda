package es.amplia.oda.core.commons.osgi.proxies;

import java.util.Optional;

import org.osgi.framework.BundleContext;

import es.amplia.oda.core.commons.interfaces.OperationSender;
import es.amplia.oda.core.commons.utils.operation.request.OperationRequest;

public class OperationSenderProxy implements OperationSender, AutoCloseable{
    
    private final OsgiServiceProxy<OperationSender> proxy;

    public OperationSenderProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(OperationSender.class, bundleContext);
    }

    @Override
    public void downlink(OperationRequest<Object> operation) {
        proxy.consumeFirst(sender -> sender.downlink(operation));
    }

    @Override
    public boolean isForNextLevel(String[] path, String deviceId) {
        return Optional.ofNullable(proxy.callFirst(sender -> sender.isForNextLevel(path, deviceId))).orElse(false);
    }

    @Override
    public void close() {
        proxy.close();
    }

}
