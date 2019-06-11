package es.amplia.oda.operation.synchronizeclock;

import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinderImpl;
import es.amplia.oda.core.commons.utils.DatastreamsSettersLocator;
import es.amplia.oda.core.commons.utils.DatastreamsSettersLocatorOsgi;
import es.amplia.oda.operation.api.OperationSynchronizeClock;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private ServiceRegistration<OperationSynchronizeClock> registration;

	@Override
	public void start(BundleContext context) throws Exception {
		LOGGER.info("Starting Synchronize Clock Operation");

		DatastreamsSettersLocator datastreamsSettersLocator = new DatastreamsSettersLocatorOsgi(context);
		DatastreamsSettersFinder datastreamsSettersFinder = new DatastreamsSettersFinderImpl(datastreamsSettersLocator);
		OperationSynchronizeClockImpl synchronizeClockOperation =
				new OperationSynchronizeClockImpl(datastreamsSettersFinder);
		registration = context.registerService(OperationSynchronizeClock.class, synchronizeClockOperation, null);

		LOGGER.info("Synchronize Clock Operation started");
	}

	@Override
	public void stop(BundleContext context) {
		LOGGER.info("Stopping Synchronize Clock Operation");

		registration.unregister();

		LOGGER.info("Synchronize Clock Operation stopped");
	}
}
