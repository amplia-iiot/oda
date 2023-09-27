package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;

import java.util.*;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

class EventParserImpl implements EventParser {

    private final DeviceInfoProvider deviceInfoProvider;


    EventParserImpl(DeviceInfoProvider deviceInfoProvider) {
        this.deviceInfoProvider = deviceInfoProvider;
    }

    @Override
    public OutputDatastream parse(List<Event> events) {
        String hostId = deviceInfoProvider.getDeviceId();
        String deviceId = events.get(0).getDeviceId();
        deviceId = deviceId.isEmpty() ? hostId : deviceId;
        String[] path = getPath(hostId, deviceId, events.get(0).getPath());
        Set<Datastream> datastreams = new HashSet<>();

        for(Event event: events) {
            Long at = event.getAt();

            Datapoint datapoint = new Datapoint(at, event.getValue());
            Datastream datastream = new Datastream(event.getDatastreamId(), event.getFeed(), Collections.singleton(datapoint));

            datastreams.add(datastream);
        }

        return new OutputDatastream(OPENGATE_VERSION, deviceId, path, datastreams);
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
}
