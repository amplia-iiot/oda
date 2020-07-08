package es.amplia.oda.hardware.diozero.configuration;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.DeviceType;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcService;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcChannel;

import com.diozero.api.AnalogInputDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

public class DioZeroConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DioZeroConfigurationHandler.class);

	static final String DEVICE_TYPE_PROPERTY_NAME = "deviceType";
	static final String NAME_PROPERTY_NAME = "name";
	static final String LOW_MODE_PROPERTY_NAME = "lowMode";
	static final String PATH_PROPERTY_NAME = "path";
	static final String DEVICE_PROPERTY_NAME = "device";
	static final String ADC_CHANNEL_DEVICE_TYPE = "adc.ADCChannel";


	private final DioZeroAdcService adcService;
	private final List<AdcChannel> configuredChannels = new ArrayList<>();


	public DioZeroConfigurationHandler(DioZeroAdcService adcService) {
		this.adcService = adcService;
	}

	@Override
	public void loadDefaultConfiguration() {
		LOGGER.info("Loading default configuration");
		configuredChannels.clear();
		LOGGER.info("Default configuration loaded");
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");
		configuredChannels.clear();
		Map<String, ?> propsMap = Collections.dictionaryToMap(props);
		for (Map.Entry<String, ?> entry : propsMap.entrySet()) {
			try {
				int index = Integer.parseInt(entry.getKey());
				String[] tokens = getTokensFromProperty((String) entry.getValue());

				if(isAdcChannelDevice(tokens)) {
					AnalogInputDeviceBuilder builder = AnalogInputDeviceBuilder.newBuilder();
					builder.setChannelIndex(index);
					getValueByToken(NAME_PROPERTY_NAME, tokens)
							.ifPresent(builder::setName);
					getValueByToken(LOW_MODE_PROPERTY_NAME, tokens)
							.ifPresent(value -> builder.setLowMode(Boolean.parseBoolean(value)));
					getValueByToken(PATH_PROPERTY_NAME, tokens)
							.ifPresent(builder::setPath);
					getValueByToken(DEVICE_PROPERTY_NAME, tokens)
							.ifPresent(value -> builder.setDeviceType(DeviceType.typeOf(value)));
					AnalogInputDevice aid = builder.build();
					configuredChannels.add(new DioZeroAdcChannel(aid.getGpio(), aid));
					LOGGER.info("Added new ADC channel to the configured channels");
				}
			} catch (Exception exception) {
				LOGGER.warn("Invalid device configuration {}: {}", entry.getKey(), entry.getValue());
			}
		}
		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		adcService.loadConfiguration(configuredChannels);
	}

	private boolean isAdcChannelDevice(String[] tokens) {
		String deviceType = getValueByToken(DEVICE_TYPE_PROPERTY_NAME, tokens).orElse("");
		return deviceType.equals(ADC_CHANNEL_DEVICE_TYPE);
	}
}