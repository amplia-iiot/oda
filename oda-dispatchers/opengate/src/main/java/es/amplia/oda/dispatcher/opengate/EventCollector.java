package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.event.api.EventDispatcher;

import java.util.Collection;

public interface EventCollector extends EventDispatcher  {
    void loadDatastreamIdsToCollect(Collection<String> datastreamIds);
    void publishCollectedEvents(Collection<String> datastreamIds);
}
