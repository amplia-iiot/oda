package es.amplia.oda.statemanager.inmemory.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.statemanager.inmemory.InMemoryStateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

public class StateManagerInMemoryConfigurationHandler implements ConfigurationUpdateHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(StateManagerInMemoryConfigurationHandler.class);

	static final String DATABASE_PATH_PROPERTY_NAME = "databasePath";
	static final String MAX_DATA_PROPERTY_NAME = "maxData";
	static final String TIME_TO_FORGET_OLD_DATA_PROPERTY_NAME = "forgetTime";
	static final String PERIOD_TO_FORGET_OLD_DATA_PROPERTY_NAME = "forgetPeriod";
	static final String TASKS_PROCESSING_THREADS_PROPERTY_NAME = "numProcessingThreads";
	static final String TASKS_PROCESSING_QUEUE_SIZE_PROPERTY_NAME = "tasksQueueSize";




	private StateManagerInMemoryConfiguration config;
	private final InMemoryStateManager stateManager;

	public StateManagerInMemoryConfigurationHandler(InMemoryStateManager stateManager) {
		this.stateManager = stateManager;
	}


	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new state manager configuration");

		StateManagerInMemoryConfiguration.StateManagerInMemoryConfigurationBuilder builder = StateManagerInMemoryConfiguration.builder();

		builder.databasePath(Optional.ofNullable((String) props.get(DATABASE_PATH_PROPERTY_NAME))
				.orElseThrow(() ->  new ConfigurationException("Builder Path is a required Parameter")));
		builder.maxData(Optional.of(Integer.parseInt((String) props.get(MAX_DATA_PROPERTY_NAME)))
				.orElseThrow(() ->  new ConfigurationException("Max Data is a required Parameter")));
		builder.forgetTime(Optional.of(Long.parseLong((String) props.get(TIME_TO_FORGET_OLD_DATA_PROPERTY_NAME)))
				.orElseThrow(() ->  new ConfigurationException("Forget Time is a required Parameter")));
		builder.forgetPeriod(Optional.of(Long.parseLong((String) props.get(PERIOD_TO_FORGET_OLD_DATA_PROPERTY_NAME)))
				.orElseThrow(() ->  new ConfigurationException("Forget Period is a required Parameter")));

		// optional parameters
		Optional.ofNullable((String) props.get(TASKS_PROCESSING_THREADS_PROPERTY_NAME)).map(Integer::parseInt)
				.ifPresent(builder::numThreads);
		Optional.ofNullable((String) props.get(TASKS_PROCESSING_QUEUE_SIZE_PROPERTY_NAME)).map(Integer::parseInt)
				.ifPresent(builder::taskQueueSize);

		config = builder.build();

		LOGGER.info("New configuration of state manager loaded");
	}

	@Override
	public void applyConfiguration() {
		this.stateManager.loadConfiguration(this.config);
	}
}
