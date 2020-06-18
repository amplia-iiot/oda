package es.amplia.oda.hardware.diozero.analog;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.AdcService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DioZeroAdcService implements AdcService, AutoCloseable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DioZeroAdcService.class);


	private final Map<Integer, AdcChannel> channels = new HashMap<>();


	@Override
	public AdcChannel getChannelByName(String name) {
		return channels.values().stream()
				.filter(channel -> name.equals(channel.getName()))
				.findFirst()
				.orElseThrow(() -> new AdcDeviceException("No ADC channel found with name " + name));
	}

	@Override
	public AdcChannel getChannelByIndex(int index) {
		return Optional.ofNullable(channels.get(index)).orElseThrow(() ->
				new AdcDeviceException("No ADC channel found with index " + index));
	}

	@Override
	public Map<Integer, AdcChannel> getAvailableChannels() {
		return new HashMap<>(channels);
	}

	public void loadConfiguration(List<AdcChannel> configuredChannels) {
		LOGGER.info("Loading new configuration for ADC Hardware bundle. Closing previous configuration");
		close();
		configuredChannels.forEach(channel -> this.channels.put(channel.getIndex(), channel));
		LOGGER.info("New configuration for ADC Hardware bundle loaded");
	}

	@Override
	public void close() {
		for (AdcChannel channel : channels.values()) {
			try {
				channel.close();
			} catch (AdcDeviceException e) {
				LOGGER.warn("Unable to release ADC channel {}", channel.getName());
			}
		}
		channels.clear();
	}
}
