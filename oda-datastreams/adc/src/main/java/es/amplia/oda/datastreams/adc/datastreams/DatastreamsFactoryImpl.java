package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.datastreams.adc.DatastreamsFactory;
import es.amplia.oda.event.api.EventDispatcher;

public class DatastreamsFactoryImpl implements DatastreamsFactory {

	private final AdcService adcService;
	private final EventDispatcher eventDispatcher;

	public DatastreamsFactoryImpl(AdcService adcService, EventDispatcher eventDispatcher) {
		this.adcService = adcService;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public AdcDatastreamsGetter createAdcDatastreamsGetter(String datastreamId, int pinIndex) {
		return new AdcDatastreamsGetter(datastreamId, pinIndex, adcService);
	}

	@Override
	public AdcDatastreamsEvent createAdcDatastreamsEvent(String datastreamId, int pinIndex) {
		return new AdcDatastreamsEvent(datastreamId, pinIndex, adcService, eventDispatcher);
	}
}
