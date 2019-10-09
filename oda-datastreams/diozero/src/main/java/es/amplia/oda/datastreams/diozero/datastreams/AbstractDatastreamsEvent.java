package es.amplia.oda.datastreams.diozero.datastreams;

import es.amplia.oda.core.commons.adc.AdcEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import java.util.Collections;
import java.util.List;

public abstract class AbstractDatastreamsEvent implements DatastreamsEvent {

	private final String datastreamId;
	private final EventDispatcher eventDispatcher;

	public AbstractDatastreamsEvent(String datastreamId, EventDispatcher eventDispatcher) {
		this.datastreamId = datastreamId;
		this.eventDispatcher = eventDispatcher;
	}

	protected String getDatastreamId() {
		return this.datastreamId;
	}

	protected void publishEvent(AdcEvent value) {
		publish("", datastreamId, Collections.emptyList(), System.currentTimeMillis(), value);
	}

	@Override
	public void publish(String deviceId, String datastreamId, List<String> path, Long at, Object value) {
		Event event = new Event(datastreamId, deviceId, path.toArray(new String[0]), at, value);
		eventDispatcher.publish(event);
	}
}
