package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class AdcDatastreamsGetter implements DatastreamsGetter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdcDatastreamsGetter.class);

	private final String datastreamId;
	private final int pinIndex;
	private final AdcService adcService;
	private final double min;
	private final double max;


	AdcDatastreamsGetter(String datastreamId, int pinIndex, AdcService adcService, double min, double max) {
		this.datastreamId = datastreamId;
		this.pinIndex = pinIndex;
		this.adcService = adcService;
		this.min = min;
		this.max = max;
	}

	@Override
	public String getDatastreamIdSatisfied() {
		return datastreamId;
	}

	@Override
	public List<String> getDevicesIdManaged() {
		return Collections.singletonList("");
	}

	@Override
	public CompletableFuture<CollectedValue> get(String device) {
		return CompletableFuture.supplyAsync(this::getDatastreamIdValuesForDevicePattern);
	}

	private DatastreamsGetter.CollectedValue getDatastreamIdValuesForDevicePattern() {
		try {
			AdcChannel channel = adcService.getChannelByIndex(pinIndex);

			long at = System.currentTimeMillis();
			Float value = (float) (((max - min) * channel.getScaledValue()) + min);
			LOGGER.debug("Getting value {} from ADC pin {} at {}", value, pinIndex, at);
			return new DatastreamsGetter.CollectedValue(at , value );
		} catch (AdcDeviceException e) {
			throw new DataNotFoundException("Error getting value from ADC channel " + pinIndex +
					", corresponding to datastream " + datastreamId);
		}
	}
}
