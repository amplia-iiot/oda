package es.amplia.oda.operation.setclock;

import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinderImpl;
import es.amplia.oda.core.commons.utils.DatastreamsSettersLocator;
import es.amplia.oda.core.commons.utils.DatastreamsSettersLocatorOsgi;
import es.amplia.oda.operation.api.OperationSetClock;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private ServiceRegistration<OperationSetClock> registration;

	@Override
	public void start(BundleContext context) throws Exception {
		LOGGER.info("Starting Set Clock Operation");

		DatastreamsSettersLocator datastreamsSettersLocator = new DatastreamsSettersLocatorOsgi(context);
		DatastreamsSettersFinder datastreamsSettersFinder = new DatastreamsSettersFinderImpl(datastreamsSettersLocator);
		OperationSetClockImpl setClockOperation = new OperationSetClockImpl(datastreamsSettersFinder);
		registration = context.registerService(OperationSetClock.class, setClockOperation, null);

		LOGGER.info("Set Clock Operation started");
	}

	@Override
	public void stop(BundleContext context) {
		LOGGER.info("Stopping Set Clock Operation");

		registration.unregister();

		LOGGER.info("Set Clock Operation stopped");
	}
}
