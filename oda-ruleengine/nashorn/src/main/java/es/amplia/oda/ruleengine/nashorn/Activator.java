package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.ruleengine.api.ScriptTranslator;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfigurationHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private ConfigurableBundle configurableBundle;
	private RuleEngineNashorn ruleEngine;
	private ScriptTranslator scriptTranslator;
	private ServiceRegistration<es.amplia.oda.ruleengine.api.RuleEngine> ruleEngineServiceRegistration;


	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Rule Engine is starting");

		scriptTranslator = new NashornScriptTranslator();
		ruleEngine = new RuleEngineNashorn(scriptTranslator);
		RuleEngineConfigurationHandler engineConfigurationHandler = new RuleEngineConfigurationHandler(ruleEngine);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, engineConfigurationHandler);
		ruleEngineServiceRegistration = bundleContext.registerService(es.amplia.oda.ruleengine.api.RuleEngine.class, ruleEngine, null);

		LOGGER.info("Rule engine started");
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Rule Engine is stopping");

		ruleEngineServiceRegistration.unregister();
		configurableBundle.close();
		ruleEngine.stop();
		scriptTranslator.close();

		LOGGER.info("Rule Engine stopped");
	}
}
