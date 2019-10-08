package es.amplia.oda.datastreams.diozero;

import es.amplia.oda.core.commons.diozero.AdcService;
import es.amplia.oda.datastreams.diozero.datastreams.adc.AdcDatastreamsEvent;
import es.amplia.oda.datastreams.diozero.datastreams.adc.AdcDatastreamsGetter;
import es.amplia.oda.event.api.EventDispatcher;

import java.util.concurrent.Executor;

public class DatastreamsFactory {

	// Hide the public constructor to avoid instantiation of this class
	private DatastreamsFactory() {

	}

	static AdcDatastreamsGetter createAdcDatastreamsGetter(String datastreamId, int pinIndex, AdcService adcService,
															Executor executor) {
		return new AdcDatastreamsGetter(datastreamId, pinIndex, adcService, executor);
	}

	static AdcDatastreamsEvent createAdcDatastreamsEvent(String datastreamId, int pinIndex, AdcService adcService,
														  EventDispatcher eventDispatcher) {
		return new AdcDatastreamsEvent(datastreamId, pinIndex, adcService, eventDispatcher);
	}

	// To add GPIO or another I/O device, add createXDatastreams(Getter/Setter/Event) as above
}
