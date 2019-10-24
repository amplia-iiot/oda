package es.amplia.oda.datastreams.adc.configuration;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcService;
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

	static final String CHANNEL_PIN_PROPERTY_NAME = "channelPin";
	static final String GETTER_PROPERTY_NAME = "getter";
	static final String EVENT_PROPERTY_NAME = "event";
	static final String MINIMUM_PROPERTY_NAME = "min";
	static final String MAXIMUM_PROPERTY_NAME = "max";


	private final DatastreamsRegistry adcDatastreamsRegistry;
	private final AdcService adcService;
	private final Map<String, AdcChannelDatastreamConfiguration> currentConfiguration = new HashMap<>();


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
				String datastreamId = entry.getKey();
				String[] tokens = getTokensFromProperty((String) entry.getValue());

				AdcChannelDatastreamConfiguration.AdcChannelDatastreamConfigurationBuilder builder =
						AdcChannelDatastreamConfiguration.builder().datastreamId(datastreamId);
				getValueByToken(CHANNEL_PIN_PROPERTY_NAME, tokens).map(Integer::parseInt).ifPresent(builder::channelPin);
				getValueByToken(GETTER_PROPERTY_NAME, tokens).map(Boolean::parseBoolean).ifPresent(builder::getter);
				getValueByToken(EVENT_PROPERTY_NAME, tokens).map(Boolean::parseBoolean).ifPresent(builder::event);
				getValueByToken(MINIMUM_PROPERTY_NAME, tokens).map(Double::parseDouble).ifPresent(builder::min);
				getValueByToken(MAXIMUM_PROPERTY_NAME, tokens).map(Double::parseDouble).ifPresent(builder::max);
				currentConfiguration.put(datastreamId, builder.build());
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
						.datastreamId(datastreamId)
						.channelPin(pinIndex)
						.getter(AdcChannelDatastreamConfiguration.DEFAULT_GETTER)
						.event(AdcChannelDatastreamConfiguration.DEFAULT_EVENT)
						.min(AdcChannelDatastreamConfiguration.DEFAULT_MINIMUM)
						.max(AdcChannelDatastreamConfiguration.DEFAULT_MAXIMUM)
						.build();
			currentConfiguration.put(datastreamId, adcConfig);
		}

		LOGGER.info("Default configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		adcDatastreamsRegistry.close();

		for (Map.Entry<String, AdcChannelDatastreamConfiguration> entry : currentConfiguration.entrySet()) {
			configPin(entry.getKey(), entry.getValue());
		}
	}

	private void configPin(String datastreamId, AdcChannelDatastreamConfiguration config) {
		if (config.isGetter())
			createAdcDatastreamGetter(config.getChannelPin(), datastreamId, config.getMin(), config.getMax());
		if (config.isEvent())
			createAdcDatastreamEvent(config.getChannelPin(), datastreamId);
	}

	private void createAdcDatastreamGetter(int pinIndex, String datastreamId, double min, double max) {
		adcDatastreamsRegistry.addAdcDatastreamGetter(pinIndex, datastreamId, min, max);
	}

	private void createAdcDatastreamEvent(int pinIndex, String datastreamId) {
		adcDatastreamsRegistry.addAdcDatastreamEvent(pinIndex, datastreamId);
	}
}
