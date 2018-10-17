package es.amplia.oda.operation.api.osgi.proxies;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;

import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OperationSetDeviceParametersProxy implements OperationSetDeviceParameters, AutoCloseable {

	private final OsgiServiceProxy<OperationSetDeviceParameters> proxy;
	
	public OperationSetDeviceParametersProxy(BundleContext bundleContext) {
		proxy = new OsgiServiceProxy<>(OperationSetDeviceParameters.class, bundleContext);
	}
	
	@Override
	public CompletableFuture<Result> setDeviceParameters(String deviceId, List<VariableValue> values) {
		return proxy.callFirst(op -> op.setDeviceParameters(deviceId, values));
	}

	@Override
	public void close() {
		proxy.close();
	}
}
