package es.amplia.oda.operation.nashorn.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.ruleengine.nashorn.NashornScriptTranslator;
import es.amplia.oda.operation.nashorn.OperationEngineNashorn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

public class OperationEngineConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(OperationEngineConfigurationHandler.class);

	private static final String PATH_PROPERTY_NAME = "path";
	private static final String UTILS_PATH_PROPERTY_NAME = "utilsPath";

	private OperationEngineConfiguration config;
	OperationEngineNashorn operationEngine;
	NashornScriptTranslator scriptTranslator;

	public OperationEngineConfigurationHandler(OperationEngineNashorn operationEngine, NashornScriptTranslator scriptTranslator) {
		this.operationEngine = operationEngine;
		this.scriptTranslator = scriptTranslator;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");

		OperationEngineConfiguration.OperationEngineConfigurationBuilder builder = OperationEngineConfiguration.builder();

		builder.path(Optional.ofNullable((String) props.get(PATH_PROPERTY_NAME))
				.orElseThrow(() ->  new ConfigurationException("Operations path is a required parameter")));
		builder.utilsPath(Optional.ofNullable((String) props.get(UTILS_PATH_PROPERTY_NAME))
				.orElseThrow(() ->  new ConfigurationException("Operations utils path is a required parameter")));

		config = builder.build();

		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		this.scriptTranslator.loadConfiguration(this.config);
		this.operationEngine.loadConfiguration(this.config);
	}
}
