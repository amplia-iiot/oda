package es.amplia.oda.datastreams.diozero.datastreams.adc;

import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.datastreams.diozero.datastreams.AbstractDatastreamsEvent;
import es.amplia.oda.event.api.EventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdcDatastreamsEvent extends AbstractDatastreamsEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdcDatastreamsEvent.class);

	private final int pinIndex;
	private final AdcService adcService;

	private AdcChannel channel;

	public AdcDatastreamsEvent(String datastreamId, int pinIndex, AdcService adcService, EventDispatcher eventDispatcher) {
		super(datastreamId, eventDispatcher);
		this.pinIndex = pinIndex;
		this.adcService = adcService;
	}

	@Override
	public void registerToEventSource() {
		try {
			channel = adcService.getChannelByIndex(pinIndex);

			channel.addAdcPinListener(this::publishEvent);
		} catch (AdcDeviceException adcDeviceException) {
			LOGGER.warn("Error initializing datastreams {}: Exception adding listener to ADC pin {}. {}", getDatastreamId(),
					pinIndex, adcDeviceException);
		}
	}

	@Override
	public void unregisterFromEventSource() {
		try {
			channel.removeAllAdcPinListener();
		} catch (AdcDeviceException adcDeviceException) {
			LOGGER.warn("Error releasing datastreams {}: Exception removing listeners from ADC Channel {}. {}",
					getDatastreamId(), pinIndex, adcDeviceException.getMessage());
		} catch (Exception exception) {
			LOGGER.warn("Error releasing datastreams event {}: ADC channel is not available", getDatastreamId());
		}
	}
}
