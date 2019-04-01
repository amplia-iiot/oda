package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.Serializer;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

public class SerializerProxy implements Serializer, AutoCloseable {

	private final OsgiServiceProxy<Serializer> proxy;
	private String errMsg;

	public SerializerProxy(BundleContext bundleContext, Serializer.SERIALIZER_TYPE type) {
		proxy = new OsgiServiceProxy<>(Serializer.class, bundleContext);
	}

	@Override
	public byte[] serialize(Object value) throws IOException {
		Optional<byte[]> serialValue =
				Optional.ofNullable(proxy.callFirst(serializer -> {
					try {
						return serializer.serialize(value);
					} catch (IOException e) {
						errMsg = e.getMessage();
						return null;
					}
				}));
		return serialValue.orElseThrow(() ->
				new IOException(this.errMsg));
	}

	@Override
	public <T> T deserialize(byte[] value, Class<T> type) throws IOException {
		Optional<T> serialValue =
				Optional.ofNullable(proxy.callFirst(serializer -> {
					try {
						return serializer.deserialize(value, type);
					} catch (IOException e) {
						errMsg = e.getMessage();
						return null;
					}
				}));
		return serialValue.orElseThrow(() ->
				new IOException(this.errMsg));
	}

	@Override
	public void close() throws Exception {
		proxy.close();
	}
}
