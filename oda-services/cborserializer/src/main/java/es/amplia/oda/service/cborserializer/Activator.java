package es.amplia.oda.service.cborserializer;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.Serializers;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<Serializer> registration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Service CBOR Serializer");
        CborSerializer cborSerializer = new CborSerializer();

        Dictionary<String, String> serializerProps = new Hashtable<>();
        serializerProps.put(Serializers.TYPE_PROPERTY_NAME, Serializers.SerializerType.CBOR.toString());
        registration = bundleContext.registerService(Serializer.class, cborSerializer, serializerProps);

        LOGGER.info("CBOR Serializer Activator started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Service CBOR Serializer");

        registration.unregister();

        LOGGER.info("CBOR Serializer Activator stopped");
    }
}
