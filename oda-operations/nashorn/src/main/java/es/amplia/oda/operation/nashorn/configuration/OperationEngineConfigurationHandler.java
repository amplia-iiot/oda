package es.amplia.oda.operation.nashorn.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.operation.nashorn.NashornScriptTranslator;
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
	OperationEngineNashorn ruleEngine;
	NashornScriptTranslator scriptTranslator;

	public OperationEngineConfigurationHandler(OperationEngineNashorn ruleEngine, NashornScriptTranslator scriptTranslator) {
		this.ruleEngine = ruleEngine;
		this.scriptTranslator = scriptTranslator;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");

		OperationEngineConfiguration.OperationEngineConfigurationBuilder builder = OperationEngineConfiguration.builder();

		builder.path(Optional.ofNullable((String) props.get(PATH_PROPERTY_NAME))
				.orElseThrow(() ->  new ConfigurationException("Rules path is a required parameter")));
		builder.utilsPath(Optional.ofNullable((String) props.get(UTILS_PATH_PROPERTY_NAME))
				.orElseThrow(() ->  new ConfigurationException("Rules utils path is a required parameter")));

		config = builder.build();

		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		this.scriptTranslator.loadConfiguration(this.config);
		this.ruleEngine.loadConfiguration(this.config);
	}
}
