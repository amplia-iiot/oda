package es.amplia.oda.operation.api.osgi.proxies;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.operation.api.OperationDiscover;

import org.osgi.framework.BundleContext;

import java.util.concurrent.CompletableFuture;

public class OperationDiscoverProxy implements OperationDiscover, AutoCloseable{

	private final OsgiServiceProxy<OperationDiscover> proxy;

	public OperationDiscoverProxy(BundleContext bundleContext) {
		proxy = new OsgiServiceProxy<>(OperationDiscover.class, bundleContext);
	}

	@Override
	public CompletableFuture<Result> discover() {
		return proxy.callFirst(OperationDiscover::discover);
	}

	@Override
	public void close() {
		proxy.close();
	}
}
