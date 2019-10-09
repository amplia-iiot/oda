package es.amplia.oda.datastreams.diozero;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.datastreams.diozero.datastreams.adc.AdcDatastreamsEvent;
import es.amplia.oda.datastreams.diozero.datastreams.adc.AdcDatastreamsGetter;
import es.amplia.oda.event.api.EventDispatcher;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DatastreamsRegistry {

	private static final int NUM_THREADS = 10;
	private static final String NOT_IMPLEMENTED_YET_EXCEPTION_MESSAGE = "Not implemented yet. Sorry for the inconvenience";

	private final Executor executor = Executors.newFixedThreadPool(NUM_THREADS);
	private final BundleContext bundleContext;
	private final AdcService adcService;
	private final EventDispatcher eventDispatcher;
	private final Map<String, AdcDatastreamsEvent> adcDatastreamsEvents = new HashMap<>();
	private final List<ServiceRegistration<?>> datastreamsServiceRegistrations = new ArrayList<>();

	DatastreamsRegistry(BundleContext bundleContext, AdcService adcService, EventDispatcher eventDispatcher) {
		this.bundleContext = bundleContext;
		this.adcService = adcService;
		this.eventDispatcher = eventDispatcher;
	}

	public void addGpioDatastreamGetter(int pinIndex, String datastreamId) {
		throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET_EXCEPTION_MESSAGE);
		/*GpioDatastreamsGetter datastreamsGetter =
				DatastreamsFactory.createGpioDatastreamsGetter(datastreamId, pinIndex, gpioService, executor);
		ServiceRegistration<DatastreamsGetter> registration =
				bundleContext.registerService(DatastreamsGetter.class, datastreamsGetter, null);
		datastreamsServiceRegistrations.add(registration);*/
	}

	public void addGpioDatastreamSetter(int pinIndex, String datastreamId) {
		throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET_EXCEPTION_MESSAGE);
		/*GpioDatastreamsSetter datastreamsSetter =
				GpioDatastreamsFactory.createGpioDatastreamsSetter(datastreamId, pinIndex, gpioService, executor);
		ServiceRegistration<DatastreamsSetter> registration =
				bundleContext.registerService(DatastreamsSetter.class, datastreamsSetter, null);
		datastreamsServiceRegistrations.add(registration);*/
	}

	public void addGpioDatastreamEvent(int pinIndex, String datastreamId) {
		throw new UnsupportedOperationException(NOT_IMPLEMENTED_YET_EXCEPTION_MESSAGE);
		/*GpioDatastreamsEvent datastreamsEventSender;

		if (datastreamsEvents.containsKey(datastreamId)) {
			datastreamsEventSender = datastreamsEvents.get(datastreamId);
			datastreamsEventSender.unregisterFromEventSource();
		}

		datastreamsEventSender =
				GpioDatastreamsFactory.createGpioDatastreamsEvent(datastreamId, pinIndex, gpioService, eventDispatcher);
		datastreamsEventSender.registerToEventSource();
		datastreamsEvents.put(datastreamId, datastreamsEventSender);*/
	}

	public void addAdcDatastreamGetter(int pinIndex, String datastreamId) {
		AdcDatastreamsGetter datastreamsGetter =
				DatastreamsFactory.createAdcDatastreamsGetter(datastreamId, pinIndex, adcService, executor);
		ServiceRegistration<DatastreamsGetter> registration =
				bundleContext.registerService(DatastreamsGetter.class, datastreamsGetter, null);
		datastreamsServiceRegistrations.add(registration);
	}

	public void addAdcDatastreamEvent(int pinIndex, String datastreamId) {
		AdcDatastreamsEvent datastreamsEventSender;

		if(adcDatastreamsEvents.containsKey(datastreamId)) {
			datastreamsEventSender = adcDatastreamsEvents.get(datastreamId);
			datastreamsEventSender.unregisterFromEventSource();
		}

		datastreamsEventSender =
				DatastreamsFactory.createAdcDatastreamsEvent(datastreamId, pinIndex, adcService, eventDispatcher);
		datastreamsEventSender.registerToEventSource();
		adcDatastreamsEvents.put(datastreamId, datastreamsEventSender);
	}

	public void close() {
		datastreamsServiceRegistrations.forEach(ServiceRegistration::unregister);
		datastreamsServiceRegistrations.clear();
		adcDatastreamsEvents.values().forEach(AdcDatastreamsEvent::unregisterFromEventSource);
		adcDatastreamsEvents.clear();
	}
}
