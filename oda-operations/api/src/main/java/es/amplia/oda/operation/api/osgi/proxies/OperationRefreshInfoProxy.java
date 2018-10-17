package es.amplia.oda.operation.api.osgi.proxies;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.operation.api.OperationRefreshInfo;

import org.osgi.framework.BundleContext;

import java.util.concurrent.CompletableFuture;

public class OperationRefreshInfoProxy implements OperationRefreshInfo, AutoCloseable {

	private final OsgiServiceProxy<OperationRefreshInfo> proxy;

	public OperationRefreshInfoProxy(BundleContext bundleContext) {
		proxy = new OsgiServiceProxy<>(OperationRefreshInfo.class, bundleContext);
	}

	@Override
	public CompletableFuture<Result> refreshInfo(String deviceId) {
		return proxy.callFirst(op -> op.refreshInfo(deviceId));
	}

	@Override
	public void close() {
		proxy.close();
	}
}
