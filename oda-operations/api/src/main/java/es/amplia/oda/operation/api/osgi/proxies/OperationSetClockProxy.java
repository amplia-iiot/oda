package es.amplia.oda.operation.api.osgi.proxies;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.operation.api.OperationSetClock;

import org.osgi.framework.BundleContext;

import java.util.concurrent.CompletableFuture;

public class OperationSetClockProxy implements OperationSetClock, AutoCloseable {

	private final OsgiServiceProxy<OperationSetClock> proxy;

	public OperationSetClockProxy(BundleContext bundleContext) {
		proxy = new OsgiServiceProxy<>(OperationSetClock.class, bundleContext);
	}

	@Override
	public CompletableFuture<Result> setClock(String deviceId, long timestamp) {
		return proxy.callFirst(op -> op.setClock(deviceId, timestamp));
	}

	@Override
	public void close() {
		proxy.close();
	}
}
