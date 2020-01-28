package es.amplia.oda.operation.update.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.operation.update.OperationUpdateImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;
import java.util.function.Supplier;

public class UpdateConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateConfigurationHandler.class);

	static final String PATH_PROPERTY_NAME = "path";

	private UpdateConfiguration config;
	OperationUpdateImpl operationUpdate;

	public UpdateConfigurationHandler(OperationUpdateImpl operationUpdate) {
		this.operationUpdate = operationUpdate;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");

		UpdateConfiguration.UpdateConfigurationBuilder builder = UpdateConfiguration.builder();

		builder.rulesPath(Optional.ofNullable((String) props.get(PATH_PROPERTY_NAME))
				.orElseThrow(() ->  missingPathExceptionSupplier().get()));

		config = builder.build();

		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		this.operationUpdate.loadConfiguration(this.config);
	}

	Supplier<ConfigurationException> missingPathExceptionSupplier() {
		return () -> new ConfigurationException("Missing require path for Rule Engine");
	}
}
