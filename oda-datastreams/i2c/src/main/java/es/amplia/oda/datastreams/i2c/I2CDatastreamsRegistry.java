package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class I2CDatastreamsRegistry {

	private static final int NUM_THREADS = 10;

	private final Executor executor = Executors.newFixedThreadPool(NUM_THREADS);

	private final BundleContext bundleContext;
	private final I2CService service;

	private final List<ServiceRegistration<?>> serviceRegistrations = new ArrayList<>();

	I2CDatastreamsRegistry(BundleContext bundleContext, I2CService service) {
		this.bundleContext = bundleContext;
		this.service = service;
	}

	public void addDatastreamGetter(String name) {
		I2CDatastreamsGetter getter =
				I2CDatastreamFactory.createDatastreamsGetter(name, service, executor);
		ServiceRegistration<DatastreamsGetter> registration =
				bundleContext.registerService(DatastreamsGetter.class, getter, null);
		serviceRegistrations.add(registration);
	}

	public void addDatastreamSetter(String name) {
		I2CDatastreamsSetter setter =
				I2CDatastreamFactory.createDatastreamsSetter(name, service, executor);
		ServiceRegistration<DatastreamsSetter> registration =
				bundleContext.registerService(DatastreamsSetter.class, setter, null);
		serviceRegistrations.add(registration);
	}

	public void close() {
		serviceRegistrations.forEach(ServiceRegistration::unregister);
		serviceRegistrations.clear();
	}
}
