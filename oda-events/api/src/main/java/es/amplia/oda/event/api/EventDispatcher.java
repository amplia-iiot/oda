package es.amplia.oda.event.api;

import java.util.List;

/**
 * Interface to be implemented by all components able to dispatch events to
 * process them, cache them or send them through a connector.
 */
public interface EventDispatcher {
    /**
     * Publish an event generated from a source to the dispatcher to process it,
     * cache it or send it through a connector.
     * @param event Event to publish.
     */
    void publish(Event event);
    /**
     * Publish a list of events generated from a source to the dispatcher to process it,
     * cache it or send it through a connector.
     * @param event List of events to publish.
     */
    void publish(List<Event> event);
}
