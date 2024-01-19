package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;

import java.util.*;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

class EventParserReducedOutputImpl implements EventParser {

    private final DeviceInfoProvider deviceInfoProvider;


    EventParserReducedOutputImpl(DeviceInfoProvider deviceInfoProvider) {
        this.deviceInfoProvider = deviceInfoProvider;
    }

    @Override
    public List<OutputDatastream> parse(List<Event> events) {
        List<OutputDatastream> datastreamsParsedList = new ArrayList<>();

        // separate events grouping by deviceId
        Map<String, List<Event>> eventsPerDevice = separateEventsByDeviceId(events);

        // parse events by deviceId
        for (Map.Entry<String, List<Event>> entry : eventsPerDevice.entrySet()) {

            String deviceId = getDeviceId(entry.getKey());

            // merge together values with same datastreamId and feed
            List<Datastream> datastreams = mergeDatastreams(entry.getValue());
            datastreamsParsedList.add(new OutputDatastream(OPENGATE_VERSION, deviceId, null, datastreams));
        }

        return datastreamsParsedList;
    }

    private String getDeviceId(String deviceId) {
        String hostId = deviceInfoProvider.getDeviceId();
        String inferredDeviceId = deviceId.isEmpty() ? hostId : deviceId;
        return inferredDeviceId.equals(hostId) ? null : inferredDeviceId;
    }

    private List<Datastream> mergeDatastreams(List<Event> events)
    {
        List<Datastream> mergedDatastreams = new ArrayList<>();

        // group events with the same datastreamId
        Map<String, List<Event>> eventsByDatastreamId = separateEventsByDatastreamId(events);

        for (Map.Entry<String, List<Event>> entryDatastreamId : eventsByDatastreamId.entrySet()) {

            // every list of events have the same datastreamId
            // group events by feed
            Map<String, List<Event>> eventsByFeed = separateEventsByFeed(entryDatastreamId.getValue());

            for (Map.Entry<String, List<Event>> entryFeed : eventsByFeed.entrySet()) {

                // at this point events have the same datastreamId and feed, so we can merge datapoints
                String datastreamId = entryDatastreamId.getKey();
                String feed = entryFeed.getKey();

                List<Datapoint> mergedDatapoints = new ArrayList<>();
                for (Event event : entryFeed.getValue()) {
                    mergedDatapoints.add(new Datapoint(null, event.getValue()));
                }

                // create datastream
                Datastream datastream = new Datastream(datastreamId, feed, mergedDatapoints);
                mergedDatastreams.add(datastream);
            }
        }

        return mergedDatastreams;
    }

    private Map<String, List<Event>> separateEventsByDeviceId(List<Event> events)
    {
        Map<String, List<Event>> eventsPerDevice = new HashMap<>();
        for (Event event : events) {

            // check if it already exists a list for the deviceId in map
            List<Event> eventsFromDeviceI = eventsPerDevice.get(event.getDeviceId());

            if (eventsFromDeviceI == null) {
                eventsPerDevice.put(event.getDeviceId(), new ArrayList<>(Collections.singleton(event)));
            } else {
                eventsFromDeviceI.add(event);
            }
        }
        return eventsPerDevice;
    }

    private Map<String, List<Event>> separateEventsByDatastreamId(List<Event> events)
    {
        Map<String, List<Event>> eventsPerDatastreamId = new HashMap<>();
        for (Event event : events) {

            // check if it already exists a list for the feed in map
            List<Event> eventsFromDatastreamIdI = eventsPerDatastreamId.get(event.getDatastreamId());

            if (eventsFromDatastreamIdI == null) {
                eventsPerDatastreamId.put(event.getDatastreamId(), new ArrayList<>(Collections.singleton(event)));
            } else {
                eventsFromDatastreamIdI.add(event);
            }
        }
        return eventsPerDatastreamId;
    }

    private Map<String, List<Event>> separateEventsByFeed(List<Event> events)
    {
        Map<String, List<Event>> eventsPerFeed = new HashMap<>();
        for (Event event : events) {

            // check if it already exists a list for the feed in map
            List<Event> eventsFromFeedI = eventsPerFeed.get(event.getFeed());

            if (eventsFromFeedI == null) {
                eventsPerFeed.put(event.getFeed(), new ArrayList<>(Collections.singleton(event)));
            } else {
                eventsFromFeedI.add(event);
            }
        }
        return eventsPerFeed;
    }
}
