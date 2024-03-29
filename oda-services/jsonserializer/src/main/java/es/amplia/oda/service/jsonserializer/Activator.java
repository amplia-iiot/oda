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

public class Activator  implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<Serializer> registration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting Service JSON Serializer");

        JsonSerializer jsonSerializer = new JsonSerializer();
        Dictionary<String, String> serializerProps = new MapBasedDictionary<>(String.class);
        serializerProps.put(ContentType.PROPERTY_NAME, ContentType.JSON.toString());
        registration = bundleContext.registerService(Serializer.class, jsonSerializer, serializerProps);

        LOGGER.info("JSON Serializer Activator started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping Service JSON Serializer");
        
        registration.unregister();
        
        LOGGER.info("JSON Serializer Activator stopped");
    }
}
