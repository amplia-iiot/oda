package es.amplia.oda.operation.createrule;

import es.amplia.oda.operation.api.OperationCreateRule;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private ServiceRegistration<OperationCreateRule> registration;

	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting Operation Create Rule Activator");

		OperationCreateRule createRule = new OperationCreateRuleImpl();
		registration = bundleContext.registerService(OperationCreateRule.class, createRule, null);

		LOGGER.info("Operation Create Rule started");
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping Operation Create Rule Activator");

		registration.unregister();

		LOGGER.info("Operation Create Rule stopped");
	}
}
