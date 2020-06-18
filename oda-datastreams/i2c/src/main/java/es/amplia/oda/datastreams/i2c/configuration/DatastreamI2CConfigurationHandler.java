package es.amplia.oda.datastreams.i2c.configuration;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.i2c.I2CDatastreamsRegistry;
import es.amplia.oda.datastreams.i2c.configuration.I2CDatastreamsConfiguration.I2CDatastreamsConfigurationBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DatastreamI2CConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamI2CConfigurationHandler.class);

	private static final String GETTER_PROPERTY_NAME = "getter";
	private static final String SETTER_PROPERTY_NAME = "setter";
	private static final String DEVICE_PROPERTY_NAME = "device";
	private static final String MINIMUM_PROPERTY_NAME = "min";
	private static final String MAXIMUM_PROPERTY_NAME = "max";


	private final I2CService i2CService;
	private final I2CDatastreamsRegistry i2cDatastreamsRegistry;
	private final Map<String, I2CDatastreamsConfiguration> currentConfiguration = new HashMap<>();


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
				I2CDatastreamsConfigurationBuilder builder = I2CDatastreamsConfiguration.builder().name(datastreamName);
				getValueByToken(MINIMUM_PROPERTY_NAME, tokens).map(Long::parseLong).ifPresent(builder::min);
				getValueByToken(MAXIMUM_PROPERTY_NAME, tokens).map(Long::parseLong).ifPresent(builder::max);
				getValueByToken(DEVICE_PROPERTY_NAME, tokens).ifPresent(builder::device);
				getValueByToken(GETTER_PROPERTY_NAME, tokens).map(Boolean::parseBoolean).ifPresent(builder::getter);
				getValueByToken(SETTER_PROPERTY_NAME, tokens).map(Boolean::parseBoolean).ifPresent(builder::setter);
				currentConfiguration.put(datastreamName, builder.build());
			} catch (IllegalArgumentException e) {
				LOGGER.warn("Invalid configuration {}: {}. Module will continue without this datastream.", entry.getKey(), entry.getValue());
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
			String datastreamName = entry.getKey();
			I2CDatastreamsConfiguration config = I2CDatastreamsConfiguration.builder()
					.name(datastreamName)
					.min(I2CDatastreamsConfiguration.DEFAULT_MIN)
					.max(I2CDatastreamsConfiguration.DEFAULT_MAX)
					.device(datastreamName)
					.getter(I2CDatastreamsConfiguration.DEFAULT_GETTER)
					.setter(I2CDatastreamsConfiguration.DEFAULT_SETTER)
					.build();
			currentConfiguration.put(datastreamName, config);
		}

		LOGGER.info("Default configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		LOGGER.info("Applying actual configuration");
		i2cDatastreamsRegistry.close();

		for (Map.Entry<String, I2CDatastreamsConfiguration> entry : currentConfiguration.entrySet()) {
			String name = entry.getKey();
			I2CDatastreamsConfiguration configuration = entry.getValue();
			if(configuration.isGetter()) {
				createDatastreamGetter(name, configuration.getDevice(), configuration.getMin(), configuration.getMax());
			}
			if(configuration.isSetter()) {
				createDatastreamSetter(name);
			}
		}
	}

	private void createDatastreamGetter(String name, String device, long min, long max) {
		i2cDatastreamsRegistry.addDatastreamGetter(name, device, min, max);
	}

	private void createDatastreamSetter(String name) {
		i2cDatastreamsRegistry.addDatastreamSetter(name);
	}
}
