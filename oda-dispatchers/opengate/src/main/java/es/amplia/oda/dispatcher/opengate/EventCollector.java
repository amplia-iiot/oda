package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.event.api.Event;

import java.util.List;

public interface EventCollector {
	
	List<Event> getAndCleanCollectedValues(String id);
}
