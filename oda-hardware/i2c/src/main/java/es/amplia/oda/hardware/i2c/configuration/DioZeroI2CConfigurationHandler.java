package es.amplia.oda.hardware.i2c.configuration;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.i2c.DioZeroI2CDevice;
import es.amplia.oda.hardware.i2c.DioZeroI2CService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

public class DioZeroI2CConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DioZeroI2CConfigurationHandler.class);

	private static final String CONTROLLER_PROPERTY_NAME = "controller";
	private static final String ADDRESS_PROPERTY_NAME = "address";
	private static final String REGISTER_PROPERTY_NAME = "register";
	private static final String MINIMUM_MEASURE_PROPERTY_NAME = "min";
	private static final String MAXIMUM_MEASURE_PROPERTY_NAME = "max";

	private final DioZeroI2CService i2cService;
	private final List<I2CDevice> configuredDevices = new ArrayList<>();

	public DioZeroI2CConfigurationHandler(DioZeroI2CService service) {
		this.i2cService = service;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");
		configuredDevices.clear();
		Map<String, ?> propsMap = Collections.dictionaryToMap(props);
		for (Map.Entry<String, ?> entry : propsMap.entrySet()) {
			try {
				String name = entry.getKey();
				String[] tokens = getTokensFromProperty((String) entry.getValue());

				DioZeroI2CConfiguration.DioZeroI2CConfigurationBuilder builder = DioZeroI2CConfiguration.builder();

				builder.controller(Integer.parseInt(getValueByToken(CONTROLLER_PROPERTY_NAME, tokens)
						.orElseThrow(ConfigurationException::new)));
				builder.address(Integer.parseInt(getValueByToken(ADDRESS_PROPERTY_NAME, tokens)
						.orElseThrow(ConfigurationException::new)));
				builder.register(Integer.parseInt(getValueByToken(REGISTER_PROPERTY_NAME, tokens)
						.orElseThrow(ConfigurationException::new)));
				builder.min(Long.parseLong(getValueByToken(MINIMUM_MEASURE_PROPERTY_NAME, tokens)
						.orElseThrow(ConfigurationException::new)));
				builder.max(Long.parseLong(getValueByToken(MAXIMUM_MEASURE_PROPERTY_NAME, tokens)
						.orElseThrow(ConfigurationException::new)));
				configuredDevices.add(new DioZeroI2CDevice(name, builder.build()));
			} catch (Exception exception) {
				LOGGER.warn("Invalid device configuration {}: {}", entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public void applyConfiguration() {
		LOGGER.info("Applying current configuration");
		i2cService.loadConfiguration(configuredDevices);
	}
}
