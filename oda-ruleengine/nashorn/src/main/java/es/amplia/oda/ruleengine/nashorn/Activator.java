package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.operation.api.engine.OperationEngine;
import es.amplia.oda.operation.nashorn.OperationEngineNashorn;
import es.amplia.oda.operation.nashorn.configuration.OperationEngineConfigurationHandler;
import es.amplia.oda.ruleengine.api.RuleEngine;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfigurationHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Activator for the merged nashorn engines bundle. Registers both the rule engine and the
 * operation engine (previously two separate bundles sharing the same nashorn boilerplate).
 * Each engine keeps its own configuration PID so the deployed {@code *.cfg} files still apply.
 */
public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	// PIDs preserved from the two original bundles so the deployed *.cfg files keep applying
	static final String RULE_ENGINE_PID = "es.amplia.oda.ruleengine.nashorn";
	static final String OPERATION_ENGINE_PID = "es.amplia.oda.operation.nashorn";

	private RuleEngineNashorn ruleEngine;
	private NashornScriptTranslator ruleScriptTranslator;
	private ConfigurableBundle ruleConfigurableBundle;
	private ServiceRegistration<RuleEngine> ruleEngineServiceRegistration;

	private OperationEngineNashorn operationEngine;
	private NashornScriptTranslator operationScriptTranslator;
	private ConfigurableBundle operationConfigurableBundle;
	private ServiceRegistration<OperationEngine> operationEngineServiceRegistration;


	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Nashorn engines bundle is starting");

		// Rule engine (preloads utils.js into every script)
		ruleScriptTranslator = new NashornScriptTranslator(true);
		ruleEngine = new RuleEngineNashorn(ruleScriptTranslator);
		RuleEngineConfigurationHandler ruleConfigHandler =
				new RuleEngineConfigurationHandler(ruleEngine, ruleScriptTranslator);
		ruleConfigurableBundle = new ConfigurableBundleImpl(bundleContext, ruleConfigHandler,
				Collections.emptyList(), RULE_ENGINE_PID);
		ruleEngineServiceRegistration = bundleContext.registerService(RuleEngine.class, ruleEngine, null);

		// Operation engine (does not preload utils.js, legacy behaviour)
		operationScriptTranslator = new NashornScriptTranslator(false);
		operationEngine = new OperationEngineNashorn(operationScriptTranslator);
		OperationEngineConfigurationHandler operationConfigHandler =
				new OperationEngineConfigurationHandler(operationEngine, operationScriptTranslator);
		operationConfigurableBundle = new ConfigurableBundleImpl(bundleContext, operationConfigHandler,
				Collections.emptyList(), OPERATION_ENGINE_PID);
		operationEngineServiceRegistration = bundleContext.registerService(OperationEngine.class, operationEngine, null);

		LOGGER.info("Nashorn engines bundle started");
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Nashorn engines bundle is stopping");

		ruleEngineServiceRegistration.unregister();
		ruleConfigurableBundle.close();
		ruleEngine.stop();
		ruleScriptTranslator.close();

		operationEngineServiceRegistration.unregister();
		operationConfigurableBundle.close();
		operationEngine.stop();
		operationScriptTranslator.close();

		LOGGER.info("Nashorn engines bundle stopped");
	}
}
