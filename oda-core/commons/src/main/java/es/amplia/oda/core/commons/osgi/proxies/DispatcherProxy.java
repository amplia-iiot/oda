package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.Dispatcher;

import org.osgi.framework.BundleContext;

import java.util.concurrent.CompletableFuture;

public class DispatcherProxy implements Dispatcher, AutoCloseable {

	private final OsgiServiceProxy<Dispatcher> proxy;

	public DispatcherProxy(BundleContext bundleContext) {
		proxy = new OsgiServiceProxy<>(Dispatcher.class, bundleContext);
	}

	@Override
	public CompletableFuture<byte[]> process(byte[] input) {
		return proxy.callFirst(dispatcher -> dispatcher.process(input));
	}

	@Override
	public void close() {
		proxy.close();
	}
}
