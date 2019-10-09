package es.amplia.oda.datastreams.diozero.configuration;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.diozero.Activator;
import es.amplia.oda.datastreams.diozero.DatastreamsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class DatastreamsAdcConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

	private static final String DATASTREAM_ID_PROPERTY_NAME = "datastreamId";
	private static final String GETTER_PROPERTY_NAME = "getter";
	private static final String SETTER_PROPERTY_NAME = "setter";
	private static final String EVENT_PROPERTY_NAME = "event";
	private static final String PIN_TYPE_PROPERTY_NAME = "pinType";

	private static final String GPIO_PIN_TYPE_NAME = "gpio.GPIOPin";
	private static final String ADC_CHANNEL_TYPE_NAME = "adc.ADCChannel";

	private final DatastreamsRegistry adcDatastreamsRegistry;
	private final AdcService adcService;
	private final Map<Integer, AdcChannelDatastreamConfiguration> currentConfiguration = new HashMap<>();

	public DatastreamsAdcConfigurationHandler(DatastreamsRegistry adcDatastreamsRegistry, AdcService adcService) {
		this.adcDatastreamsRegistry = adcDatastreamsRegistry;
		this.adcService = adcService;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");
		currentConfiguration.clear();

		Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);
		for (Map.Entry<String, ?> entry: mappedProperties.entrySet()) {
			try {
				int adcPin = Integer.parseInt((entry.getKey()));
				String[] tokens = getTokensFromProperty((String) entry.getValue());

				AdcChannelDatastreamConfiguration.AdcChannelDatastreamConfigurationBuilder builder =
						AdcChannelDatastreamConfiguration.builder();

				builder.channelPin(adcPin);
				builder.datastreamId(getValueByToken(DATASTREAM_ID_PROPERTY_NAME, tokens)
					.orElseThrow(() -> new ConfigurationException("Invalid Datastream configuration "
						+ entry.getKey()
						+ ": Data stream identifier is required")));
				getValueByToken(PIN_TYPE_PROPERTY_NAME, tokens)
						.ifPresent(builder::pinType);

				getValueByToken(GETTER_PROPERTY_NAME, tokens)
						.ifPresent((getterValue -> builder.getter(Boolean.parseBoolean(getterValue))));
				getValueByToken(SETTER_PROPERTY_NAME, tokens)
						.ifPresent((setterValue -> builder.setter(Boolean.parseBoolean(setterValue))));
				getValueByToken(EVENT_PROPERTY_NAME, tokens)
						.ifPresent((eventValue -> builder.event(Boolean.parseBoolean(eventValue))));

				currentConfiguration.put(adcPin, builder.build());
			} catch (NumberFormatException e) {
				LOGGER.warn("Invalid configuration {}: {}", entry.getKey(), entry.getValue());
			}
		}
		LOGGER.info("Configuration loaded");
	}

	@Override
	public void loadDefaultConfiguration() {
		LOGGER.info("Loading default configuration");
		currentConfiguration.clear();

		Map<Integer, AdcChannel> availablePins = adcService.getAvailableChannels();
		for (Map.Entry<Integer, AdcChannel> availablePinEntry: availablePins.entrySet()) {
			int pinIndex = availablePinEntry.getKey();
			AdcChannel channel = availablePinEntry.getValue();
			String datastreamId = channel.getName();

			AdcChannelDatastreamConfiguration adcConfig =
					AdcChannelDatastreamConfiguration
						.builder()
						.channelPin(pinIndex)
						.datastreamId(datastreamId)
						.pinType(PIN_TYPE_PROPERTY_NAME)
						.getter(true)
						.setter(false)
						.event(false)
						.build();

			currentConfiguration.put(pinIndex, adcConfig);
		}

		LOGGER.info("Default configuration loaded");
	}

	@Override
	public void applyConfiguration() throws Exception {
		adcDatastreamsRegistry.close();

		for (Map.Entry<Integer, AdcChannelDatastreamConfiguration> entry : currentConfiguration.entrySet()) {
			configPin(entry.getKey(), entry.getValue());
		}
	}

	private void configPin(int pinIndex, AdcChannelDatastreamConfiguration config) {
		switch(config.getPinType()) {
			case GPIO_PIN_TYPE_NAME:
				if(config.isGetter())
					createGpioDatastreamGetter(pinIndex, config.getDatastreamId());
				if(config.isSetter())
					createGpioDatastreamSetter(pinIndex, config.getDatastreamId());
				if(config.isEvent())
					createGpioDatastreamEvent(pinIndex, config.getDatastreamId());
				break;
			case ADC_CHANNEL_TYPE_NAME:
				if(config.isGetter())
					createAdcDatastreamGetter(pinIndex, config.getDatastreamId());
				if(config.isSetter())
					createAdcDatastreamSetter(pinIndex, config.getDatastreamId());
				if(config.isEvent())
					createAdcDatastreamEvent(pinIndex, config.getDatastreamId());
				break;
			default:
				throw new ConfigurationException("Invalid Datastream configuration "
						+ pinIndex
						+ ": Pin Type must be:\n"
						+ "   - " + PIN_TYPE_PROPERTY_NAME
						+ "   - " + ADC_CHANNEL_TYPE_NAME);
		}
	}

	private void createAdcDatastreamGetter(int pinIndex, String datastreamId) {
		adcDatastreamsRegistry.addAdcDatastreamGetter(pinIndex, datastreamId);
	}

	private void createAdcDatastreamSetter(int pinIndex, String datastreamId) {
		throw new ConfigurationException("Impossible add Setter to ADC Channel " + pinIndex + ". Analog data cant be setted, " +
				"please change datastream " + datastreamId);
	}

	private void createAdcDatastreamEvent(int pinIndex, String datastreamId) {
		adcDatastreamsRegistry.addAdcDatastreamEvent(pinIndex, datastreamId);
	}

	private void createGpioDatastreamGetter(int pinIndex, String datastreamId) {
		adcDatastreamsRegistry.addGpioDatastreamGetter(pinIndex, datastreamId);
	}

	private void createGpioDatastreamSetter(int pinIndex, String datastreamId) {
		adcDatastreamsRegistry.addGpioDatastreamSetter(pinIndex, datastreamId);
	}

	private void createGpioDatastreamEvent(int pinIndex, String datastreamId) {
		adcDatastreamsRegistry.addGpioDatastreamEvent(pinIndex, datastreamId);
	}
}
