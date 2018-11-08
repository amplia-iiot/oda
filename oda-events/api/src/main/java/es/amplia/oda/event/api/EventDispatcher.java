package es.amplia.oda.event.api;

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
}
