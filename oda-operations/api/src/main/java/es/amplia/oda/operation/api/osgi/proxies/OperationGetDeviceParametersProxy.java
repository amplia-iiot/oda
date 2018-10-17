package es.amplia.oda.operation.api.osgi.proxies;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;

import org.osgi.framework.BundleContext;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class OperationGetDeviceParametersProxy implements OperationGetDeviceParameters, AutoCloseable {

	private final OsgiServiceProxy<OperationGetDeviceParameters> proxy;
	
	public OperationGetDeviceParametersProxy(BundleContext bundleContext) {
		proxy = new OsgiServiceProxy<>(OperationGetDeviceParameters.class, bundleContext);
	}
	
	@Override
	public CompletableFuture<Result> getDeviceParameters(String deviceId, Set<String> dataStreamIds) {
		return proxy.callFirst(op -> op.getDeviceParameters(deviceId, dataStreamIds));
	}

	@Override
	public void close() {
		proxy.close();
	}
}
