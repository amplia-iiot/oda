package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.dispatcher.opengate.EventCollector;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
    public void publish(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        List<Event> eventsToPublish = new ArrayList<>();
        for (Event event : events) {
            if (isEventFromCollectedDatastream(event)) {
                LOGGER.info("Collected event {} of datastream {}", event, event.getDatastreamId());
                collect(event);
            } else {
                eventsToPublish.add(event);
            }
        }
        eventDispatcher.publish(eventsToPublish);
    }

    @Override
    public void publishImmediately(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        eventDispatcher.publishImmediately(events);
    }

    @Override
    public void publishSameThreadNoQos(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        eventDispatcher.publishSameThreadNoQos(events);
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

        for (String datastreamId : datastreamIds) {
            List<Event> events = collectedEvents.remove(datastreamId);
            if (events != null) {
                events.forEach(event -> outputDatastreamPerDevice.merge(event.getDeviceId(),
                        eventDispatcher.parse(Collections.singletonList(event)),
                        this::mergeOutputDatastreams));
            } else {
                LOGGER.info("No events collected for {}", datastreamId);
            }
        }

        outputDatastreamPerDevice.forEach((deviceId, outputDatastream) -> eventDispatcher.send(outputDatastream, true));
    }


    private OutputDatastream mergeOutputDatastreams(OutputDatastream o1, OutputDatastream o2) {
        // <datastreamId, <feed, datapoints>>
        Map<String, Map<String, List<Datapoint>>> datastreams1 = buildMap(o1.getDatastreams());
        Map<String, Map<String, List<Datapoint>>> datastreams2 = buildMap(o2.getDatastreams());

        // initialize final map with the map from datastreams1
        Map<String, Map<String, List<Datapoint>>> datastreams = new HashMap<>(datastreams1);

        // traverse trough datastreams2 map
        datastreams2.forEach((datastreamId, feedMapDatapoints2) -> {

            // get <feed, datapoints> map from final map corresponding to this datastreamId
            Map<String, List<Datapoint>> feedMapDatapoints1 = datastreams.get(datastreamId);

            // there is data for the same datastreamId in both maps
            // now we have to check if feed is the same
            if(feedMapDatapoints1 != null)
            {
                // key is datastreamId, value is map <feed, datapoints>
                // for every value in feedMapDatapoints2 (for every different feed)
                feedMapDatapoints2.forEach((feedDatastream2, datapointsDatastream2) -> {

                    // get from datastreams1, the datapoints corresponding to that feed
                    List<Datapoint> datapointsDatastream1 = feedMapDatapoints1.get(feedDatastream2);

                    // if both maps have datapoints for the same feed, merge datapoints
                    if(datapointsDatastream1 != null)
                    {
                        feedMapDatapoints1.put(feedDatastream2, mergeDatapoints(datapointsDatastream1, datapointsDatastream2));
                    }
                    // if feedDatastream2 doesn't exist in final datastreams map, include it
                    else {
                        feedMapDatapoints1.put(feedDatastream2, datapointsDatastream2);
                    }
                });
            }
            // there is no map in final map for this datastreamId, so we include it
            else {
                datastreams.put(datastreamId, feedMapDatapoints2);
            }
        });

        return new OutputDatastream(o1.getVersion(), o1.getDevice(), o1.getPath(), translateMap(datastreams));
    }

    private Map<String, Map<String, List<Datapoint>>> buildMap(List<Datastream> datastreams) {
        Map<String, Map<String, List<Datapoint>>> datastreamsMap = new HashMap<>();
        datastreams.forEach(value -> {
            Map<String, List<Datapoint>> feedMap = datastreamsMap.get(value.getId());

            if (feedMap != null) {
                feedMap.putIfAbsent(value.getFeed(), value.getDatapoints());
            } else {
                feedMap = new HashMap<>();
                feedMap.put(value.getFeed(), value.getDatapoints());
                datastreamsMap.put(value.getId(), feedMap);
            }
        });

        return datastreamsMap;
    }

    private List<Datastream> translateMap(Map<String, Map<String, List<Datapoint>>> datastreamsMap) {
        List<Datastream> datastreamsSet = new ArrayList<>();
        datastreamsMap.forEach((datastreamId, feedMap) ->
                feedMap.forEach((feed, datapoints) ->
                        datastreamsSet.add(new Datastream(datastreamId, feed, datapoints))));

        return datastreamsSet;
    }

    private List<Datapoint> mergeDatapoints(List<Datapoint> d1, List<Datapoint> d2){
        List<Datapoint> datapoints = new ArrayList<>(d1);
        datapoints.addAll(d2);
        return datapoints;
    }
}
