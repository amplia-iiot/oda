package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.Serializer;

import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SerializerProxy implements Serializer, AutoCloseable {

    private final OsgiServiceProxy<Serializer> proxy;
    private final ContentType contentType;
    private String errorMessage;

    public SerializerProxy(BundleContext bundleContext, ContentType contentType) {
        Map<String, String> filterProps = new HashMap<>();
        filterProps.put(ContentType.PROPERTY_NAME, contentType.toString());
        proxy = new OsgiServiceProxy<>(Serializer.class, filterProps, bundleContext);
        this.contentType = contentType;
    }

    @Override
    public byte[] serialize(Object value) throws IOException {
        Optional<byte[]> serialValue =
                Optional.ofNullable(proxy.callFirst(serializer -> {
                    try {
                        return serializer.serialize(value);
                    } catch (IOException e) {
                        errorMessage = e.getMessage();
                        return null;
                    }
                }));
        return serialValue.orElseThrow(() ->
                new IOException(this.errorMessage));
    }

    @Override
    public <T> T deserialize(byte[] value, Class<T> type) throws IOException {
        Optional<T> serialValue =
                Optional.ofNullable(proxy.callFirst(serializer -> {
                    try {
                        return serializer.deserialize(value, type);
                    } catch (IOException e) {
                        errorMessage = e.getMessage();
                        return null;
                    }
                }));
        return serialValue.orElseThrow(() ->
                new IOException(this.errorMessage));
    }

    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public void close() {
        proxy.close();
    }
}
