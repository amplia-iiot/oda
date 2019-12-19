package es.amplia.oda.operation.synchronizeclock;

import es.amplia.oda.operation.api.OperationSynchronizeClock;
import es.amplia.oda.statemanager.api.StateManagerProxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private StateManagerProxy stateManager;
	private ServiceRegistration<OperationSynchronizeClock> synchronizeClockRegistration;

	@Override
	public void start(BundleContext context) {
		LOGGER.info("Starting Synchronize Clock Operation");

		stateManager = new StateManagerProxy(context);
		OperationSynchronizeClockImpl synchronizeClockOperation =
				new OperationSynchronizeClockImpl(stateManager);
		synchronizeClockRegistration =
				context.registerService(OperationSynchronizeClock.class, synchronizeClockOperation, null);

		LOGGER.info("Synchronize Clock Operation started");
	}

	@Override
	public void stop(BundleContext context) {
		LOGGER.info("Stopping Synchronize Clock Operation");

		synchronizeClockRegistration.unregister();
		stateManager.close();

		LOGGER.info("Synchronize Clock Operation stopped");
	}
}
