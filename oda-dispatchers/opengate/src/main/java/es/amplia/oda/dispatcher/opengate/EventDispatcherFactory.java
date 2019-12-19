package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.entities.ContentType;

public interface EventDispatcherFactory {
    EventCollector createEventCollector(boolean reducedOutput, ContentType contentType);
}
