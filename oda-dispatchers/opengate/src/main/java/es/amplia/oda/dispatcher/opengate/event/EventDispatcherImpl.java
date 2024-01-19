package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.core.commons.utils.Scheduler;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.EventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
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

        List<OutputDatastream> outputEvents = parse(events);
        outputEvents.forEach(outputEvent -> scheduler.schedule(() -> send(outputEvent, true),
                0, 0, TimeUnit.SECONDS));
    }

    @Override
    public void publishImmediately(List<Event> events) {
        publish(events);
    }

    @Override
    public void publishSameThreadNoQos(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        List<OutputDatastream> outputEvents = parse(events);
        outputEvents.forEach( outputEvent ->  send(outputEvent, false));
    }

    List<OutputDatastream> parse(List<Event> events) {
        return eventParser.parse(events);
    }


    void send(OutputDatastream outputEvent, boolean useQos) {
        try {
            LOGGER.info("Publishing events {}", outputEvent);
            int maxLength = connector.getMaxLength();
            byte[] payload = serializer.serialize(outputEvent);
            if ( (connector.hasMaxlength()) && (payload.length > maxLength) ) {
                recalcualtePayload(outputEvent, maxLength, useQos);
            } else
                uplinkToConnector(payload, useQos);
        } catch (IOException e) {
            LOGGER.error("Error serializing events {}. Events will not be published: ", outputEvent, e);
        }
    }

    private void recalcualtePayload(OutputDatastream event, int maxLength, boolean useQos) throws IOException {
        List<OutputDatastream> events = splitMessage(event);
        
        try {
            byte[] payload1 = serializer.serialize(events.get(0));
            if (payload1.length > maxLength) {
                recalcualtePayload(events.get(0), maxLength, useQos);
            } else
                uplinkToConnector(payload1, useQos);
        } catch (IOException e) {
            LOGGER.error("Error serializing events {}. Events will not be published: ", events.get(0), e);
        }

        try {
            byte[] payload2 = serializer.serialize(events.get(1));
            if (payload2.length > maxLength)  {
                recalcualtePayload(events.get(1), maxLength, useQos);
            } else
                uplinkToConnector(payload2, useQos);
        } catch (IOException e) {
            LOGGER.error("Error serializing events {}. Events will not be published: ", events.get(1), e);
        }
    }

    private void uplinkToConnector(byte[] payload, boolean useQos) {
        if (useQos) {
            connector.uplink(payload, contentType);
        } else {
            connector.uplinkNoQos(payload, contentType);
        }
    }

    private List<OutputDatastream> splitMessage(OutputDatastream event) throws IOException {
        ArrayList<OutputDatastream> ret = new ArrayList<>();
        List<Datastream> datastreams = event.getDatastreams();
        List<Datastream> ds1 = new ArrayList<>();
        List<Datastream> ds2 = new ArrayList<>();
        if (datastreams.size() == 1) {
            Datastream datastream = datastreams.iterator().next();
            List<Datapoint> datapoints = datastream.getDatapoints();
            if (datapoints.size() == 1) throw new IOException("Cannot split message, only 1 datapoint for datastream " + datastreams.iterator().next().getId());
            else splitDatapoints(datastream, ds1, ds2);
        } else {
            List<Datastream> actualDatastream = ds1;
            Iterator<Datastream> dsit = datastreams.iterator();
            // Ponemos la mitad de los datapoints de cada datastream en cada SET y si solo hay 1 vamos metiendo cada datapoint en cada SET
            while (dsit.hasNext()) {
                Datastream datastream = dsit.next();
                List<Datapoint> datapoints = datastream.getDatapoints();
                int size = datapoints.size();
                if (size == 1) {
                    actualDatastream.add(datastream);
                    actualDatastream = actualDatastream==ds1?ds2:ds1;
                } else splitDatapoints(datastream, ds1, ds2);
            }
        }
        ret.add(new OutputDatastream(event.getVersion(), event.getDevice(), event.getPath(), ds1));
        ret.add(new OutputDatastream(event.getVersion(), event.getDevice(), event.getPath(), ds2));
        return ret;
    }

    private void splitDatapoints(Datastream ds, List<Datastream> ds1, List<Datastream> ds2) {
        List<Datapoint> datapoints = ds.getDatapoints();
        Iterator<Datapoint> dpit = datapoints.iterator();
        int dp1size = datapoints.size()/2;
        int i = 0;
        List<Datapoint> dp1 = new ArrayList<>();
        List<Datapoint> dp2 = new ArrayList<>();

        for (;i < dp1size; i++) {dp1.add(dpit.next());}
        for (;i < datapoints.size(); i++) {dp2.add(dpit.next());}

        ds1.add(new Datastream(ds.getId(), ds.getFeed(), dp1));
        ds2.add(new Datastream(ds.getId(), ds.getFeed(), dp2));
    }

}
