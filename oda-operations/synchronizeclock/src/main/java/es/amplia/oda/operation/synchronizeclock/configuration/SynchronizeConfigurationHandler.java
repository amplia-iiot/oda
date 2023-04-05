package es.amplia.oda.operation.synchronizeclock.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.operation.synchronizeclock.OperationSynchronizeClockImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

public class SynchronizeConfigurationHandler implements ConfigurationUpdateHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizeConfigurationHandler.class);

	static final String CLOCK_DATASTREAM_PROPERTY_NAME = "clockDatastream";

	private SynchronizeConfiguration config;
	OperationSynchronizeClockImpl operationSync;

	public SynchronizeConfigurationHandler(OperationSynchronizeClockImpl operationSync) {
		this.operationSync = operationSync;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");

		SynchronizeConfiguration.SynchronizeConfigurationBuilder builder = SynchronizeConfiguration.builder();

		builder.clockDatastream(Optional.ofNullable((String) props.get(CLOCK_DATASTREAM_PROPERTY_NAME))
				.orElseThrow(() ->  new ConfigurationException("Clock datastream is a required parameter")));

		config = builder.build();

		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		this.operationSync.loadConfiguration(this.config.clockDatastream);
	}
}
