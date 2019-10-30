package es.amplia.oda.datastreams.adc;

import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

import java.util.HashMap;
import java.util.Map;

public class DatastreamsRegistry {

	private final DatastreamsFactory datastreamsFactory;
	private final ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager;
	private final Map<String, DatastreamsEvent> datastreamsEvents = new HashMap<>();


	DatastreamsRegistry(DatastreamsFactory datastreamsFactory,
						ServiceRegistrationManager<DatastreamsGetter> datastreamsGetterRegistrationManager) {
		this.datastreamsFactory = datastreamsFactory;
		this.datastreamsGetterRegistrationManager = datastreamsGetterRegistrationManager;

	}

	public void addAdcDatastreamGetter(int pinIndex, String datastreamId, double min, double max) {
		DatastreamsGetter datastreamsGetter = datastreamsFactory.createAdcDatastreamsGetter(datastreamId, pinIndex, min, max);
		datastreamsGetterRegistrationManager.register(datastreamsGetter);
	}

	public void addAdcDatastreamEvent(int pinIndex, String datastreamId) {
		DatastreamsEvent datastreamsEventSender;

		if(datastreamsEvents.containsKey(datastreamId)) {
			datastreamsEventSender = datastreamsEvents.get(datastreamId);
			datastreamsEventSender.unregisterFromEventSource();
		}

		datastreamsEventSender = datastreamsFactory.createAdcDatastreamsEvent(datastreamId, pinIndex);
		datastreamsEventSender.registerToEventSource();
		datastreamsEvents.put(datastreamId, datastreamsEventSender);
	}

	public void close() {
		datastreamsGetterRegistrationManager.unregister();
		datastreamsEvents.values().forEach(DatastreamsEvent::unregisterFromEventSource);
		datastreamsEvents.clear();
	}
}
