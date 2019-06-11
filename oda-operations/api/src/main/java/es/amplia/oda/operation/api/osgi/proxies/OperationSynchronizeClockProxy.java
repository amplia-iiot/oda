package es.amplia.oda.operation.api.osgi.proxies;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.operation.api.OperationSynchronizeClock;

import org.osgi.framework.BundleContext;

import java.util.concurrent.CompletableFuture;

public class OperationSynchronizeClockProxy implements OperationSynchronizeClock, AutoCloseable {

	private final OsgiServiceProxy<OperationSynchronizeClock> proxy;

	public OperationSynchronizeClockProxy(BundleContext bundleContext) {
		proxy = new OsgiServiceProxy<>(OperationSynchronizeClock.class, bundleContext);
	}

	@Override
	public CompletableFuture<Result> synchronizeClock(String deviceId, String source) {
		return proxy.callFirst(op -> op.synchronizeClock(deviceId, source));
	}

	@Override
	public void close() {
		proxy.close();
	}
}
