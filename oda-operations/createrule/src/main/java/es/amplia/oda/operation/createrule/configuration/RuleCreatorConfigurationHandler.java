package es.amplia.oda.operation.createrule.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.operation.createrule.OperationCreateRuleImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;
import java.util.function.Supplier;

public class RuleCreatorConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleCreatorConfiguration.class);

	private static final String PATH_PROPERTY_NAME = "path";

	private RuleCreatorConfiguration config;
	OperationCreateRuleImpl createRule;

	public void RuleCreatorConfiguration(OperationCreateRuleImpl createRule) {
		this.createRule = createRule;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");

		RuleCreatorConfiguration.RuleCreatorConfigurationBuilder builder = RuleCreatorConfiguration.builder();

		builder.path(Optional.ofNullable((String) props.get(PATH_PROPERTY_NAME))
				.orElseThrow(() ->  missingPathExceptionSupplier().get()));

		config = builder.build();

		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		this.createRule.loadConfiguration(this.config);
	}

	private Supplier<ConfigurationException> missingPathExceptionSupplier() {
		return () -> new ConfigurationException("Missing require path for Rule Engine");
	}
}
