package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.AdcEvent;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.AbstractDatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.EventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class AdcDatastreamsEvent extends AbstractDatastreamsEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdcDatastreamsEvent.class);


	private final String datastreamId;
	private final int pinIndex;
	private final AdcService adcService;
	private AdcChannel channel;
	private final double min;
	private final double max;

	AdcDatastreamsEvent(String datastreamId, int pinIndex, AdcService adcService, EventPublisher eventPublisher,
						double min, double max) {
		super(eventPublisher);
		this.datastreamId = datastreamId;
		this.pinIndex = pinIndex;
		this.adcService = adcService;
		this.min = min;
		this.max = max;
	}

	@Override
	public void registerToEventSource() {
		try {
			channel = adcService.getChannelByIndex(pinIndex);
			channel.addAdcPinListener(this::publishEvent);
		} catch (AdcDeviceException adcDeviceException) {
			LOGGER.warn("Couldn't initializing datastream event {}: Exception adding listener to ADC pin {}. {}", datastreamId,
					pinIndex, adcDeviceException.getMessage());
		}
	}

	private void publishEvent(AdcEvent event) {
		float value = (float) (((max - min) * event.getScaledValue()) + min);
		Map<String, Map<Long, Object>> events = new HashMap<>();
		Map<Long, Object> data = new HashMap<>();
		data.put(event.getEpochTime(), value);
		events.put(datastreamId, data);
		publish("", Collections.emptyList(), events);
	}

	@Override
	public void unregisterFromEventSource() {
		try {
			channel.removeAllAdcPinListener();
		} catch (AdcDeviceException adcDeviceException) {
			LOGGER.warn("Error releasing datastream event {}: Exception removing listeners from ADC Channel {}. {}",
					datastreamId, pinIndex, adcDeviceException.getMessage());
		} catch (Exception exception) {
			LOGGER.warn("Error releasing datastream event {}: ADC channel {} is not available", datastreamId, pinIndex);
		}
	}
}
