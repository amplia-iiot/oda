package es.amplia.oda.operation.nashorn;

import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.operation.api.engine.OperationEngine;
import es.amplia.oda.operation.nashorn.configuration.OperationEngineConfigurationHandler;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private ConfigurableBundle configurableBundle;
	private OperationEngineNashorn operationEngine;
	private NashornScriptTranslator scriptTranslator;
	private ServiceRegistration<OperationEngine> operationEngineServiceRegistration;


	@Override
	public void start(BundleContext bundleContext) {
		LOGGER.info("Operation Engine is starting");

		scriptTranslator = new NashornScriptTranslator();
		operationEngine = new OperationEngineNashorn(scriptTranslator);
		OperationEngineConfigurationHandler engineConfigurationHandler = new OperationEngineConfigurationHandler(operationEngine,
				scriptTranslator);
		configurableBundle = new ConfigurableBundleImpl(bundleContext, engineConfigurationHandler);
		operationEngineServiceRegistration = bundleContext.registerService(OperationEngine.class,
				operationEngine, null);

		LOGGER.info("Operation engine started");
	}

	@Override
	public void stop(BundleContext bundleContext) {
		LOGGER.info("Operation Engine is stopping");

		operationEngineServiceRegistration.unregister();
		configurableBundle.close();
		operationEngine.stop();
		scriptTranslator.close();

		LOGGER.info("Operation Engine stopped");
	}
}
