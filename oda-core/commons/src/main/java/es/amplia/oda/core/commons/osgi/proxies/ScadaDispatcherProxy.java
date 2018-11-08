package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.ScadaDispatcher;

import org.osgi.framework.BundleContext;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ScadaDispatcherProxy implements ScadaDispatcher, AutoCloseable {

    private final OsgiServiceProxy<ScadaDispatcher> proxy;

    public ScadaDispatcherProxy(BundleContext bundleContext) {
        proxy = new OsgiServiceProxy<>(ScadaDispatcher.class, bundleContext);
    }

    @Override
    public <T, S> CompletableFuture<ScadaOperationResult> process(ScadaOperation operation, int index, T value, S type) {
        Optional<CompletableFuture<ScadaOperationResult>> future =
                Optional.ofNullable(proxy.callFirst(scadaDispatcher -> scadaDispatcher.process(operation, index, value, type)));
        return future.orElse(CompletableFuture.completedFuture(ScadaOperationResult.ERROR));
    }

    @Override
    public void close() {
        proxy.close();
    }
}
