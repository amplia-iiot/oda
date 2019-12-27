package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.datastreams.adc.DatastreamsFactory;

public class DatastreamsFactoryImpl implements DatastreamsFactory {

	private final AdcService adcService;
	private final EventPublisher eventPublisher;

	public DatastreamsFactoryImpl(AdcService adcService, EventPublisher eventPublisher) {
		this.adcService = adcService;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public DatastreamsGetter createAdcDatastreamsGetter(String datastreamId, int pinIndex, double min, double max) {
		return new AdcDatastreamsGetter(datastreamId, pinIndex, adcService, min, max);
	}

	@Override
	public DatastreamsEvent createAdcDatastreamsEvent(String datastreamId, int pinIndex, double min, double max) {
		return new AdcDatastreamsEvent(datastreamId, pinIndex, adcService, eventPublisher, min, max);
	}
}
