package es.amplia.oda.operation.api.osgi.proxies;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.operation.api.OperationUpdate;

import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OperationUpdateProxy implements OperationUpdate, AutoCloseable {

    private final OsgiServiceProxy<OperationUpdate> proxy;
    
    public OperationUpdateProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(OperationUpdate.class, bundleContext);
    }

    @Override
    public CompletableFuture<Result> update(String bundleName, String bundleVersion, List<DeploymentElement> deploymentElements) {
        return proxy.callFirst(op -> op.update(bundleName, bundleVersion, deploymentElements));
    }
    
    @Override
    public void close() {
        proxy.close();
    }
}
