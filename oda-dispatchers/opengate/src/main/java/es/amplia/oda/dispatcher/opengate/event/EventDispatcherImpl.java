package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.core.commons.utils.Scheduler;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

class EventDispatcherImpl implements EventDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDispatcherImpl.class);

    private final EventParser eventParser;
    private final Serializer serializer;
    private final ContentType contentType;
    private final OpenGateConnector connector;
    private final Scheduler scheduler;


    EventDispatcherImpl(EventParser eventParser, Serializer serializer, ContentType contentType,
                        OpenGateConnector connector, Scheduler scheduler) {
        this.eventParser = eventParser;
        this.serializer = serializer;
        this.contentType = contentType;
        this.connector = connector;
        this.scheduler = scheduler;
    }

    public void publish(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        OutputDatastream outputEvent = parse(events);
        scheduler.schedule(() -> send(outputEvent), 0, 0, TimeUnit.SECONDS);
    }

    @Override
    public void publishImmediately(List<Event> events) {
        publish(events);
    }

    OutputDatastream parse(List<Event> events) {
        return eventParser.parse(events);
    }

    void send(OutputDatastream outputEvent) {
        try {
            LOGGER.info("Publishing events {}", outputEvent);
            byte[] payload = serializer.serialize(outputEvent);
            if (payload > configurado){
                kjldyaghfikadgu
            }
            connector.uplink(payload, contentType);
        } catch (IOException e) {
            LOGGER.error("Error serializing events {}. Events will not be published: ", outputEvent, e);
        }
    }
}
