package es.amplia.oda.service.jsonserializer;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.MapBasedDictionary;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * Registers the JSON and CBOR serializers (previously two separate bundles). Both are Jackson
 * based and share the embedded Jackson; each is published as a {@code Serializer} service tagged
 * with its {@link ContentType}, so the SerializerProvider keeps picking one by content type.
 */
public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<Serializer> jsonRegistration;
    private ServiceRegistration<Serializer> cborRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Service JSON/CBOR Serializer");

        Dictionary<String, String> jsonProps = new MapBasedDictionary<>(String.class);
        jsonProps.put(ContentType.PROPERTY_NAME, ContentType.JSON.toString());
        jsonRegistration = bundleContext.registerService(Serializer.class, new JsonSerializer(), jsonProps);

        Dictionary<String, String> cborProps = new MapBasedDictionary<>(String.class);
        cborProps.put(ContentType.PROPERTY_NAME, ContentType.CBOR.toString());
        cborRegistration = bundleContext.registerService(Serializer.class, new CborSerializer(), cborProps);

        LOGGER.info("JSON/CBOR Serializer Activator started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Service JSON/CBOR Serializer");

        jsonRegistration.unregister();
        cborRegistration.unregister();

        LOGGER.info("JSON/CBOR Serializer Activator stopped");
    }
}
