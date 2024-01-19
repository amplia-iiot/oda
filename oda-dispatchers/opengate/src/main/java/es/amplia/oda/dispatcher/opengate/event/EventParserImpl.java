package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

@Slf4j
class EventParserImpl implements EventParser {

    private final DeviceInfoProvider deviceInfoProvider;


    EventParserImpl(DeviceInfoProvider deviceInfoProvider) {
        this.deviceInfoProvider = deviceInfoProvider;
    }

    @Override
    public List<OutputDatastream> parse(List<Event> events) {
        List<OutputDatastream> datastreamsParsedList = new ArrayList<>();

        // separate events grouping by deviceId
        // use LinkedHashMap to maintain insertion order
        LinkedHashMap<String, List<Event>> eventsPerDevice = separateEventsByDeviceId(events);

        String hostId = deviceInfoProvider.getDeviceId();

        // parse events by deviceId
        for (Map.Entry<String, List<Event>> entry : eventsPerDevice.entrySet()) {

            String deviceId = entry.getKey();
            deviceId = deviceId.isEmpty() ? hostId : deviceId;
            String[] path = getPath(hostId, deviceId, entry.getValue().get(0).getPath());

            // merge together values with same datastreamId and feed
            List<Datastream> datastreams = mergeDatastreams(entry.getValue());
            datastreamsParsedList.add(new OutputDatastream(OPENGATE_VERSION, deviceId, path, datastreams));
        }

        return datastreamsParsedList;
    }

    private String[] getPath(String hostId, String deviceId, String[] path) {
        if (hostId == null) {
            return path;
        } else if (hostId.equals(deviceId)) {
            return path;
        } else if (path == null) {
            return new String[] { hostId };
        } else {
            List<String> pathDevices = Arrays.stream(path).collect(Collectors.toList());
            pathDevices.add(0, hostId);
            return pathDevices.toArray(new String[0]);
        }
    }

    private List<Datastream> mergeDatastreams(List<Event> events)
    {
        List<Datastream> mergedDatastreams = new ArrayList<>();

        // group events with the same datastreamId
        LinkedHashMap<String, List<Event>> eventsByDatastreamId = separateEventsByDatastreamId(events);

        for (Map.Entry<String, List<Event>> entryDatastreamId : eventsByDatastreamId.entrySet()) {

            // every list of events have the same datastreamId
            // group events by feed
            LinkedHashMap<String, List<Event>> eventsByFeed = separateEventsByFeed(entryDatastreamId.getValue());

            for (Map.Entry<String, List<Event>> entryFeed : eventsByFeed.entrySet()) {

                // at this point events have the same datastreamId and feed, so we can merge datapoints
                String datastreamId = entryDatastreamId.getKey();
                String feed = entryFeed.getKey();

                List<Datapoint> mergedDatapoints = new ArrayList<>();
                for (Event event : entryFeed.getValue()) {
                    mergedDatapoints.add(new Datapoint(event.getAt(), event.getValue()));
                }

                // create datastream
                Datastream datastream = new Datastream(datastreamId, feed, mergedDatapoints);
                mergedDatastreams.add(datastream);
            }
        }

        return mergedDatastreams;
    }

    private LinkedHashMap<String, List<Event>> separateEventsByDeviceId(List<Event> events)
    {
        // use LinkedHashMap to maintain insertion order
        LinkedHashMap<String, List<Event>> eventsPerDevice = new LinkedHashMap<>();
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

    private LinkedHashMap<String, List<Event>> separateEventsByDatastreamId(List<Event> events)
    {
        // use LinkedHashMap to maintain insertion order
        LinkedHashMap<String, List<Event>> eventsPerDatastreamId = new LinkedHashMap<>();
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

    private LinkedHashMap<String, List<Event>> separateEventsByFeed(List<Event> events)
    {
        // use LinkedHashMap to maintain insertion order
        LinkedHashMap<String, List<Event>> eventsPerFeed = new LinkedHashMap<>();
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
