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

	private static final String DATABASE_PATH_PROPERTY_NAME = "databasePath";

	private StateManagerInMemoryConfiguration config;
	private InMemoryStateManager stateManager;

	public StateManagerInMemoryConfigurationHandler(InMemoryStateManager stateManager) {
		this.stateManager = stateManager;
	}


	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");

		StateManagerInMemoryConfiguration.StateManagerInMemoryConfigurationBuilder builder = StateManagerInMemoryConfiguration.builder();

		builder.databasePath(Optional.ofNullable((String) props.get(DATABASE_PATH_PROPERTY_NAME))
				.orElseThrow(() ->  new ConfigurationException("Builder Path is a required Parameter")));

		config = builder.build();

		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		this.stateManager.loadConfiguration(this.config);
	}
}
