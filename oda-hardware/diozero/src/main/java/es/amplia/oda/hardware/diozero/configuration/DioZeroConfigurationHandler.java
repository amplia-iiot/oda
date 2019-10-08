package es.amplia.oda.hardware.diozero.configuration;

import com.diozero.api.AnalogInputDevice;
import es.amplia.oda.core.commons.diozero.DeviceType;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcService;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Map;

public class DioZeroConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DioZeroConfigurationHandler.class);

	private static final String DEVICE_TYPE_PROPERTY_NAME = "deviceType";
	private static final String NAME_PROPERTY_NAME = "name";
	private static final String PIN_NUMBER_PROPERTY_NAME = "pinNumber";
	private static final String LOW_MODE_PROPERTY_NAME = "lowMode";
	private static final String PATH_PROPERTY_NAME = "path";
	private static final String DEVICE_PROPERTY_NAME = "device";

	private static final String GPIO_PIN_DEVICE_TYPE = "gpio.GPIOPin";
	private static final String ADC_CHANNEL_DEVICE_TYPE = "adc.ADCChannel";

	private Map<String, ?> gpioPinsConfiguration;
	private final DioZeroAdcService adcService;


	public DioZeroConfigurationHandler(DioZeroAdcService adcService) {
		this.adcService = adcService;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) throws Exception {
		LOGGER.info("Loading new configuration");
		gpioPinsConfiguration = Collections.dictionaryToMap(props);
	}

	@Override
	public void applyConfiguration() throws Exception {
		adcService.release();

		if(gpioPinsConfiguration != null) {
			for (Map.Entry<String, ?> entry : gpioPinsConfiguration.entrySet()) {
				try {
					int index = Integer.parseInt(entry.getKey());
					String[] tokens = getTokensFromProperty((String) entry.getValue());

					if(isAdcChannelDevice(tokens)) {
						AnalogInputDeviceBuilder builder = AnalogInputDeviceBuilder.newBuilder();
						builder.setChannelIndex(index);
						getValueByToken(NAME_PROPERTY_NAME, tokens)
								.ifPresent(builder::setName);
						/*getValueByToken(PIN_NUMBER_PROPERTY_NAME, tokens)
								.ifPresent(value -> builder.setPinNumber(Integer.parseInt(value)));*/
						getValueByToken(LOW_MODE_PROPERTY_NAME, tokens)
								.ifPresent(value -> builder.setLowMode(Boolean.valueOf(value)));
						getValueByToken(PATH_PROPERTY_NAME, tokens)
								.ifPresent(builder::setPath);
						getValueByToken(DEVICE_PROPERTY_NAME, tokens)
								.ifPresent(value -> builder.setDeviceType(DeviceType.valueOf(value)));
						AnalogInputDevice aid = builder.build();
						adcService.addConfiguredPin(new DioZeroAdcChannel(aid.getGpio(), aid));
					}
				} catch (Exception exception) {
					LOGGER.warn("Invalid device configuration {}: {}", entry.getKey(), entry.getValue());
				}
			}
		}
	}

	private boolean isAdcChannelDevice(String[] tokens) {
		String deviceType = getValueByToken(DEVICE_TYPE_PROPERTY_NAME, tokens).orElse("");
		return deviceType.equals(ADC_CHANNEL_DEVICE_TYPE);
	}
}