package es.amplia.oda.operation.setclock;

import es.amplia.oda.operation.api.OperationSetClock;
import es.amplia.oda.statemanager.api.StateManagerProxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private StateManagerProxy stateManager;
	private ServiceRegistration<OperationSetClock> setClockRegistration;

	@Override
	public void start(BundleContext context) {
		LOGGER.info("Starting Set Clock Operation");

		stateManager = new StateManagerProxy(context);
		OperationSetClockImpl setClockOperation = new OperationSetClockImpl(stateManager);
		setClockRegistration = context.registerService(OperationSetClock.class, setClockOperation, null);

		LOGGER.info("Set Clock Operation started");
	}

	@Override
	public void stop(BundleContext context) {
		LOGGER.info("Stopping Set Clock Operation");

		setClockRegistration.unregister();
		stateManager.close();

		LOGGER.info("Set Clock Operation stopped");
	}
}
