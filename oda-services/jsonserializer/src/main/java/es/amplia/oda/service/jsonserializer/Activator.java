package es.amplia.oda.service.jsonserializer;

import es.amplia.oda.core.commons.interfaces.Serializer;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class Activator  implements BundleActivator {

	private static final Logger logger = LoggerFactory.getLogger(Activator.class);

	private ServiceRegistration<?> registration;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		logger.info("Starting Service JSON Serializer");
		JsonSerializer jsonSerializer = new JsonSerializer();

		Dictionary<String, String> serializerProps = new Hashtable<>();
		serializerProps.put(Serializer.TYPE_PROPERTY_NAME, Serializer.SERIALIZER_TYPE.JSON.toString());
		registration = bundleContext.registerService(JsonSerializer.class.getName(), jsonSerializer, serializerProps);
		logger.info("JSON Serializer Activator started");
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		logger.info("Stopping Service JSON Serializer");
		registration.unregister();
		logger.info("JSON Serializer Activator stopped");
	}
}
