package es.amplia.oda.operation.setclock.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.operation.setclock.OperationSetClockImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;
import java.util.function.Supplier;

public class SetClockConfigurationHandler implements ConfigurationUpdateHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SetClockConfigurationHandler.class);

	static final String CLOCK_DATASTREAM_PROPERTY_NAME = "clockDatastream";

	private SetClockConfiguration config;
	OperationSetClockImpl operationSetClock;

	public SetClockConfigurationHandler(OperationSetClockImpl operationSetClock) {
		this.operationSetClock = operationSetClock;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");

		SetClockConfiguration.SetClockConfigurationBuilder builder = SetClockConfiguration.builder();

		builder.clockDatastream(Optional.ofNullable((String) props.get(CLOCK_DATASTREAM_PROPERTY_NAME))
				.orElseThrow(() ->  missingPathExceptionSupplier().get()));

		config = builder.build();

		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		this.operationSetClock.loadConfiguration(this.config.getClockDatastream());
	}

	Supplier<ConfigurationException> missingPathExceptionSupplier() {
		return () -> new ConfigurationException("Missing require path for Rule Engine");
	}
}
