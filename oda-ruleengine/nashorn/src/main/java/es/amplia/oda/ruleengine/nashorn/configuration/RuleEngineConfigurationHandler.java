package es.amplia.oda.ruleengine.nashorn.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.ruleengine.nashorn.RuleEngineNashorn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;
import java.util.function.Supplier;

public class RuleEngineConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleEngineConfigurationHandler.class);

	private static final String PATH_PROPERTY_NAME = "path";

	private RuleEngineConfiguration config;
	RuleEngineNashorn ruleEngine;

	public RuleEngineConfigurationHandler(RuleEngineNashorn ruleEngine) {
		this.ruleEngine = ruleEngine;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");

		RuleEngineConfiguration.RuleEngineConfigurationBuilder builder = RuleEngineConfiguration.builder();

		builder.path(Optional.ofNullable((String) props.get(PATH_PROPERTY_NAME))
				.orElseThrow(() ->  missingPathExceptionSupplier().get()));

		config = builder.build();

		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		this.ruleEngine.loadConfiguration(this.config);
	}

	private Supplier<ConfigurationException> missingPathExceptionSupplier() {
		return () -> new ConfigurationException("Missing require path for Rule Engine");
	}
}
