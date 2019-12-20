package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

class EventDispatcherImpl implements EventDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcherImpl.class);

    private final EventParser eventParser;
    private final Serializer serializer;
    private final ContentType contentType;
    private final OpenGateConnector connector;


    EventDispatcherImpl(EventParser eventParser, Serializer serializer, ContentType contentType,
                        OpenGateConnector connector) {
        this.eventParser = eventParser;
        this.serializer = serializer;
        this.contentType = contentType;
        this.connector = connector;
    }

    @Override
    public void publish(Event event) {
        OutputDatastream outputEvent = parse(event);
        publish(outputEvent);
    }

    OutputDatastream parse(Event event) {
        return eventParser.parse(event);
    }

    void publish(OutputDatastream outputEvent) {
        try {
            LOGGER.info("Publishing events {}", outputEvent);
            byte[] payload = serializer.serialize(outputEvent);
            connector.uplink(payload, contentType);
        } catch (IOException e) {
            LOGGER.error("Error serializing events {}. Events will not be published: ", outputEvent, e);
        }
    }
}
