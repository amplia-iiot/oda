package es.amplia.oda.datastreams.adc.configuration;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.datastreams.adc.DatastreamsRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class DatastreamsAdcConfigurationHandler implements ConfigurationUpdateHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamsAdcConfigurationHandler.class);

	static final String DATASTREAM_ID_PROPERTY_NAME = "datastreamId";
	static final String GETTER_PROPERTY_NAME = "getter";
	static final String EVENT_PROPERTY_NAME = "event";
	static final String PIN_TYPE_PROPERTY_NAME = "pinType";
	static final String ADC_CHANNEL_TYPE_NAME = "adc.ADCChannel";


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
					.orElseThrow(() -> new IllegalArgumentException("Invalid Datastream configuration "
						+ entry.getKey()
						+ ": Data stream identifier is required")));
				getValueByToken(PIN_TYPE_PROPERTY_NAME, tokens)
						.ifPresent(builder::pinType);

				getValueByToken(GETTER_PROPERTY_NAME, tokens)
						.ifPresent((getterValue -> builder.getter(Boolean.parseBoolean(getterValue))));
				getValueByToken(EVENT_PROPERTY_NAME, tokens)
						.ifPresent((eventValue -> builder.event(Boolean.parseBoolean(eventValue))));

				currentConfiguration.put(adcPin, builder.build());
			} catch (IllegalArgumentException e) {
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
						.pinType(ADC_CHANNEL_TYPE_NAME)
						.getter(true)
						.event(false)
						.build();

			currentConfiguration.put(pinIndex, adcConfig);
		}

		LOGGER.info("Default configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		adcDatastreamsRegistry.close();

		for (Map.Entry<Integer, AdcChannelDatastreamConfiguration> entry : currentConfiguration.entrySet()) {
			configPin(entry.getKey(), entry.getValue());
		}
	}

	private void configPin(int pinIndex, AdcChannelDatastreamConfiguration config) {
		if (ADC_CHANNEL_TYPE_NAME.equals(config.getPinType())) {
			if (config.isGetter())
				createAdcDatastreamGetter(pinIndex, config.getDatastreamId());
			if (config.isEvent())
				createAdcDatastreamEvent(pinIndex, config.getDatastreamId());
		} else {
			throw new ConfigurationException("Invalid Datastream configuration "
					+ pinIndex
					+ ": Pin Type must be:\n"
					+ "   - " + ADC_CHANNEL_TYPE_NAME);
		}
	}

	private void createAdcDatastreamGetter(int pinIndex, String datastreamId) {
		adcDatastreamsRegistry.addAdcDatastreamGetter(pinIndex, datastreamId);
	}

	private void createAdcDatastreamEvent(int pinIndex, String datastreamId) {
		adcDatastreamsRegistry.addAdcDatastreamEvent(pinIndex, datastreamId);
	}
}
