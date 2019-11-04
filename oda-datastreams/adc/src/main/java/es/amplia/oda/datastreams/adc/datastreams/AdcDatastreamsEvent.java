package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.AdcEvent;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class AdcDatastreamsEvent implements DatastreamsEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdcDatastreamsEvent.class);


	private final String datastreamId;
	private final int pinIndex;
	private final AdcService adcService;
	private final EventDispatcher eventDispatcher;
	private AdcChannel channel;
	private final double min;
	private final double max;

	AdcDatastreamsEvent(String datastreamId, int pinIndex, AdcService adcService, EventDispatcher eventDispatcher, double min, double max) {
		this.datastreamId = datastreamId;
		this.eventDispatcher = eventDispatcher;
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
			LOGGER.warn("Error initializing datastreams {}: Exception adding listener to ADC pin {}. {}", datastreamId,
					pinIndex, adcDeviceException);
		}
	}

	private void publishEvent(AdcEvent event) {
		float value = (float) (((max - min) * event.getScaledValue()) + min);
		publish("", datastreamId, Collections.emptyList(), event.getEpochTime(), value);
	}

	@Override
	public void publish(String deviceId, String datastreamId, List<String> path, Long at, Object value) {
		Event event = new Event(datastreamId, deviceId, path.toArray(new String[]{}), at, value);
		eventDispatcher.publish(event);
	}

	@Override
	public void unregisterFromEventSource() {
		try {
			channel.removeAllAdcPinListener();
		} catch (AdcDeviceException adcDeviceException) {
			LOGGER.warn("Error releasing datastreams {}: Exception removing listeners from ADC Channel {}. {}",
					datastreamId, pinIndex, adcDeviceException.getMessage());
		} catch (Exception exception) {
			LOGGER.warn("Error releasing datastreams event {}: ADC channel is not available", datastreamId);
		}
	}
}
