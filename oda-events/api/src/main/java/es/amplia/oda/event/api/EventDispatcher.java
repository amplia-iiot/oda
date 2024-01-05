package es.amplia.oda.event.api;

import es.amplia.oda.core.commons.utils.Event;

import java.util.List;

/**
 * Interface to be implemented by all components able to dispatch events to
 * process them, cache them or send them through a connector.
 */
public interface EventDispatcher {
    /**
     * Publish a list of events generated from a source to the dispatcher to process it,
     * cache it or send it through a connector.
     * @param events List of events to publish.
     */
    void publish(List<Event> events);

    /**
     * Publish a list of events generated from a source to the dispatcher to immediately send it through a connector.
     * @param events List of events to publish.
     */
    void publishImmediately(List<Event> events);

    /**
     * Publish a list of events generated from a source to the dispatcher to send it through a connector.
     * The publication is done in the same thread that calls this function and with qos = 0 in the connector
     * @param events List of events to publish.
     */
    void publishSameThreadNoQos(List<Event> events);
}
