package es.amplia.oda.hardware.diozero.analog;

import es.amplia.oda.core.commons.diozero.AdcChannel;
import es.amplia.oda.core.commons.diozero.AdcDeviceException;
import es.amplia.oda.core.commons.diozero.AdcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DioZeroAdcService implements AdcService {

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

	public void addConfiguredPin(AdcChannel channel) {
		channels.put(channel.getIndex(), channel);
	}

	public void release() {
		for (AdcChannel channel : channels.values()) {
			try {
				channel.close();
			} catch (AdcDeviceException e) {
				LOGGER.warn("Unable to release ADC channel {}", channel.getName());
			}
		}
	}
}
