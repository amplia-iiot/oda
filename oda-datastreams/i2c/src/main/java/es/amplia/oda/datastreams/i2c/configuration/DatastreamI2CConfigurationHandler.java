package es.amplia.oda.datastreams.i2c.configuration;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.i2c.I2CDatastreamsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DatastreamI2CConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamI2CConfigurationHandler.class);

	private static final String GETTER_PROPERTY_NAME = "getter";
	private static final String SETTER_PROPERTY_NAME = "setter";

	private final I2CService i2CService;
	private final I2CDatastreamsRegistry i2cDatastreamsRegistry;
	private Map<String, I2CDatastreamConfiguration> currentConfiguration = new HashMap<>();

	public DatastreamI2CConfigurationHandler(I2CDatastreamsRegistry i2cDatastreamsRegistry, I2CService i2CService) {
		this.i2cDatastreamsRegistry = i2cDatastreamsRegistry;
		this.i2CService = i2CService;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");
		currentConfiguration.clear();

		Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);
		for (Map.Entry<String, ?> entry: mappedProperties.entrySet()) {
			try {
				String datastreamName = entry.getKey();
				String[] tokens = getTokensFromProperty((String) entry.getValue());

				I2CDatastreamConfiguration.I2CDatastreamConfigurationBuilder builder =
						I2CDatastreamConfiguration.builder();

				builder.name(datastreamName);

				getValueByToken(GETTER_PROPERTY_NAME, tokens)
						.ifPresent(getterValue -> builder.getter(Boolean.parseBoolean(getterValue)));
				getValueByToken(SETTER_PROPERTY_NAME, tokens)
						.ifPresent(setterValue -> builder.setter(Boolean.parseBoolean(setterValue)));

				currentConfiguration.put(datastreamName, builder.build());
			} catch (Exception e) {
				LOGGER.warn("Invalid configuration {}: {}", entry.getKey(), entry.getValue());
			}
		}

		LOGGER.info("Configuration loaded");
	}

	@Override
	public void loadDefaultConfiguration() {
		LOGGER.info("Loading default configuration");
		currentConfiguration.clear();

		Map<String, I2CDevice> mappedProperties = i2CService.getAllI2C().stream()
				.collect(Collectors.toMap(I2CDevice::getName, device -> device));
		for (Map.Entry<String, I2CDevice> entry: mappedProperties.entrySet()) {
			try {
				String datastreamName = entry.getKey();

				I2CDatastreamConfiguration config = I2CDatastreamConfiguration.builder()
						.getter(true)
						.setter(false)
						.build();

				currentConfiguration.put(datastreamName, config);
			} catch (Exception e) {
				LOGGER.warn("Invalid configuration {}: {}", entry.getKey(), entry.getValue());
			}
		}

		LOGGER.info("Configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		LOGGER.info("Applying actual configuration");
		i2cDatastreamsRegistry.close();

		for (Map.Entry<String, I2CDatastreamConfiguration> entry : currentConfiguration.entrySet()) {
			String name = entry.getKey();
			I2CDatastreamConfiguration configuration = entry.getValue();

			if(configuration.isGetter()) {
				createDatastreamGetter(name);
			}
			if(configuration.isSetter()) {
				createDatastreamSetter(name);
			}
		}
	}

	private void createDatastreamGetter(String name) {
		i2cDatastreamsRegistry.addDatastreamGetter(name);
	}

	private void createDatastreamSetter(String name) {
		i2cDatastreamsRegistry.addDatastreamSetter(name);
	}
}
