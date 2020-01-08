package es.amplia.oda.operation.createrule;

import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.operation.api.OperationCreateRule;
import es.amplia.oda.operation.createrule.configuration.RuleCreatorConfigurationHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class Activator implements BundleActivator {
	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private OperationCreateRuleImpl createRule;
	private ConfigurableBundle configurableBundle;
	private ServiceRegistration<OperationCreateRule> registration;

	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Starting Operation Create Rule Activator");

		createRule = new OperationCreateRuleImpl();
		RuleCreatorConfigurationHandler configHandler = new RuleCreatorConfigurationHandler(createRule);
		registration = bundleContext.registerService(OperationCreateRule.class, createRule, null);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler,
				Collections.singletonList(registration));

		LOGGER.info("Operation Create Rule started");
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Stopping Operation Create Rule Activator");

		registration.unregister();
		configurableBundle.close();

		LOGGER.info("Operation Create Rule stopped");
	}
}
