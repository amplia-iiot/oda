package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.interfaces.SerializerProvider;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;

import org.osgi.framework.BundleContext;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SerializerProviderOsgi implements SerializerProvider, AutoCloseable {

    private final Map<ContentType, SerializerProxy> serializers;


    public SerializerProviderOsgi(BundleContext bundleContext) {
         this.serializers = Stream.of(ContentType.values())
                .map(contentType -> new SerializerProxy(bundleContext, contentType))
                .collect(Collectors.toMap(SerializerProxy::getContentType, Function.identity()));
    }

    @Override
    public Serializer getSerializer(ContentType contentType) {
        return serializers.get(contentType);
    }

    @Override
    public void close() {
        serializers.forEach((contentType, serializer) -> serializer.close());
    }
}
