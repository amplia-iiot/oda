package es.amplia.oda.hardware.diozero.configuration;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.DeviceType;
import es.amplia.oda.core.commons.gpio.GpioDirection;
import es.amplia.oda.core.commons.gpio.GpioMode;
import es.amplia.oda.core.commons.gpio.GpioTrigger;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcService;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcChannel;
import es.amplia.oda.hardware.diozero.gpio.DioZeroGpioPin;
import es.amplia.oda.hardware.diozero.gpio.DioZeroGpioService;

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

	static final String DIRECTION_PROPERTY_NAME = "direction";
	static final String MODE_PROPERTY_NAME = "mode";
	static final String TRIGGER_PROPERTY_NAME = "trigger";
	static final String ACTIVE_LOW_PROPERTY_NAME = "activeLow";
	static final String INITIAL_VALUE_PROPERTY_NAME = "initialValue";
	static final String GPIO_PIN_DEVICE_TYPE = "gpio.GPIOPin";


	private final DioZeroAdcService adcService;
	private final DioZeroGpioService gpioService;
	private final List<AdcChannel> configuredChannels = new ArrayList<>();
	private final List<DioZeroGpioPin> configuredPins = new ArrayList<>();


	public DioZeroConfigurationHandler(DioZeroAdcService adcService, DioZeroGpioService gpioService) {
		this.adcService = adcService;
		this.gpioService = gpioService;
	}

	@Override
	public void loadDefaultConfiguration() {
		LOGGER.info("Loading default configuration");
		configuredChannels.clear();
		configuredPins.clear();
		LOGGER.info("Default configuration loaded");
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");
		configuredChannels.clear();
		configuredPins.clear();
		Map<String, ?> propsMap = Collections.dictionaryToMap(props);
		for (Map.Entry<String, ?> entry : propsMap.entrySet()) {
			try {
				int index = Integer.parseInt(entry.getKey());
				String[] tokens = getTokensFromProperty((String) entry.getValue());

				if (isAdcChannelDevice(tokens)) {
					configuredChannels.add(buildAdcChannel(index, tokens));
					LOGGER.info("Added new ADC channel to the configured channels");
				} else if (isGpioPinDevice(tokens)) {
					configuredPins.add(buildGpioPin(index, tokens));
					LOGGER.info("Added new GPIO pin to the configured pins");
				}
			} catch (Exception exception) {
				LOGGER.warn("Invalid device configuration {}: {}", entry.getKey(), entry.getValue());
			}
		}
		LOGGER.info("New configuration loaded");
	}

	private AdcChannel buildAdcChannel(int index, String[] tokens) {
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
		return new DioZeroAdcChannel(aid.getGpio(), aid);
	}

	private DioZeroGpioPin buildGpioPin(int index, String[] tokens) {
		DioZeroGpioPinBuilder builder = DioZeroGpioPinBuilder.newBuilder();
		builder.setIndex(index);
		getValueByToken(NAME_PROPERTY_NAME, tokens).ifPresent(builder::setName);
		getValueByToken(DIRECTION_PROPERTY_NAME, tokens)
				.ifPresent(value -> builder.setDirection(GpioDirection.valueOf(value)));
		getValueByToken(MODE_PROPERTY_NAME, tokens)
				.ifPresent(value -> builder.setMode(GpioMode.valueOf(value)));
		getValueByToken(TRIGGER_PROPERTY_NAME, tokens)
				.ifPresent(value -> builder.setTrigger(GpioTrigger.valueOf(value)));
		getValueByToken(ACTIVE_LOW_PROPERTY_NAME, tokens)
				.ifPresent(value -> builder.setActiveLow(Boolean.parseBoolean(value)));
		getValueByToken(INITIAL_VALUE_PROPERTY_NAME, tokens)
				.ifPresent(value -> builder.setInitialValue(Boolean.parseBoolean(value)));
		return builder.build();
	}

	@Override
	public void applyConfiguration() {
		adcService.loadConfiguration(configuredChannels);
		gpioService.loadConfiguration(configuredPins);
	}

	private boolean isAdcChannelDevice(String[] tokens) {
		String deviceType = getValueByToken(DEVICE_TYPE_PROPERTY_NAME, tokens).orElse("");
		return deviceType.equals(ADC_CHANNEL_DEVICE_TYPE);
	}

	private boolean isGpioPinDevice(String[] tokens) {
		String deviceType = getValueByToken(DEVICE_TYPE_PROPERTY_NAME, tokens).orElse("");
		return deviceType.equals(GPIO_PIN_DEVICE_TYPE);
	}
}
