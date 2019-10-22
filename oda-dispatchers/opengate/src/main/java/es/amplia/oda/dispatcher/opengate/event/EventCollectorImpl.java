package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.dispatcher.opengate.EventCollector;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EventCollectorImpl implements EventCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventCollectorImpl.class);


    private final EventDispatcherImpl eventDispatcher;
    private final List<String> datastreamIdsToCollect = new ArrayList<>();
    private final Map<String, List<Event>> collectedEvents = new HashMap<>();


    EventCollectorImpl(EventDispatcherImpl eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void loadDatastreamIdsToCollect(Collection<String> datastreamIds) {
        datastreamIdsToCollect.clear();
        datastreamIdsToCollect.addAll(datastreamIds);
    }

    @Override
    public void publish(Event event) {
        if (isEventFromCollectedDatastream(event)) {
            LOGGER.info("Collected event {} of datastream {}", event, event.getDatastreamId());
            collect(event);
        } else {
            eventDispatcher.publish(event);
        }
    }

    private boolean isEventFromCollectedDatastream(Event event) {
        return datastreamIdsToCollect.contains(event.getDatastreamId());
    }

    private void collect(Event event) {
        collectedEvents.merge(event.getDatastreamId(), Collections.singletonList(event), this::joinLists);
    }

    private List<Event> joinLists(List<Event> list1, List<Event> list2) {
        ArrayList<Event> jointList = new ArrayList<>(list1);
        jointList.addAll(list2);
        return jointList;
    }

    @Override
    public void publishCollectedEvents(Collection<String> datastreamIds) {
        Map<String, OutputDatastream> outputDatastreamPerDevice = new HashMap<>();

        for(String datastreamId : datastreamIds) {
            List<Event> events = collectedEvents.remove(datastreamId);
            if (events != null) {
                events.forEach(event -> outputDatastreamPerDevice.merge(event.getDeviceId(), eventDispatcher.parse(event),
                        this::mergeOutputDatastreams));
            } else {
                LOGGER.info("No events collected for {}", datastreamId);
            }
        }

        outputDatastreamPerDevice.forEach((deviceId, outputDatastream) -> eventDispatcher.publish(outputDatastream));
    }

    private OutputDatastream mergeOutputDatastreams(OutputDatastream o1, OutputDatastream o2) {
        Map<String, Datastream> datastreams1 = o1.getDatastreams().stream()
                .collect(Collectors.toMap(Datastream::getId, Function.identity()));
        Map<String, Datastream> datastreams2 = o2.getDatastreams().stream()
                .collect(Collectors.toMap(Datastream::getId, Function.identity()));

        Map<String, Datastream> datastreams = new HashMap<>(datastreams1);
        datastreams2.forEach((key,value) ->
                datastreams.merge(key, value, this::mergeDatastreams));

        return new OutputDatastream(o1.getVersion(), o1.getDevice(), o1.getPath(), new HashSet<>(datastreams.values()));
    }

    private Datastream mergeDatastreams(Datastream d1, Datastream d2) {
        Set<Datapoint> datapoints = new HashSet<>(d1.getDatapoints());
        datapoints.addAll(d2.getDatapoints());
        return new Datastream(d1.getId(), datapoints);
    }
}
