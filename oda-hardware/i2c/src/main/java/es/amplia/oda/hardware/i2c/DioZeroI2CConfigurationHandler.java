package es.amplia.oda.hardware.i2c;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
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

	private final DioZeroI2CService i2CService;
	private final List<I2CDevice> configuredDevices = new ArrayList<>();

	DioZeroI2CConfigurationHandler(DioZeroI2CService service) {
		this.i2CService = service;
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

				int controller = Integer.parseInt(getValueByToken(CONTROLLER_PROPERTY_NAME, tokens)
						.orElseThrow(ConfigurationException::new));
				int address = Integer.parseInt(getValueByToken(ADDRESS_PROPERTY_NAME, tokens)
						.orElseThrow(ConfigurationException::new));
				int register = Integer.parseInt(getValueByToken(REGISTER_PROPERTY_NAME, tokens)
						.orElseThrow(ConfigurationException::new));
				configuredDevices.add(new DioZeroI2CDevice(name, controller, address, register));
			} catch (Exception exception) {
				LOGGER.warn("Invalid device configuration {}: {}", entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public void applyConfiguration() {
		LOGGER.info("Applying current configuration");
		i2CService.loadConfiguration(configuredDevices);
	}
}
