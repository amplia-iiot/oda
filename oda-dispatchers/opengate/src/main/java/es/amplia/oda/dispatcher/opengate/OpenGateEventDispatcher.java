package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

class OpenGateEventDispatcher implements EventDispatcher, EventCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGateOperationDispatcher.class);
    
    private final DeviceInfoProvider deviceInfoProvider;
    private final Serializer serializer;
    private final OpenGateConnector connector;
    private final Map<String, List<Event>> collectedValues = new HashMap<>();
    private final List<String> datastreamIdsConfigured = new ArrayList<>();

    OpenGateEventDispatcher(DeviceInfoProvider deviceInfoProvider, Serializer serializer,
                            OpenGateConnector connector) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.serializer = serializer;
        this.connector = connector;
    }

    @Override
    public void publish(Event event) {
        if (periodicSentConfigured(event.getDatastreamId())) {
            // If periodic sent is configured for the Datastream ID then save collected value
            saveCollectedValue(event, event.getDatastreamId());
        } else {
            // Else send the value
            OutputDatastream outputEvent = translateToOutputDatastream(event);
            try {
                byte[] payload = serializer.serialize(outputEvent);
                connector.uplink(payload);
            } catch (IOException e) {
                LOGGER.error("Error serializing event. Event will not be published: ", e);
            }

        }
    }

    private OutputDatastream translateToOutputDatastream(Event event) {
        String hostId = deviceInfoProvider.getDeviceId();
        String deviceId = event.getDeviceId();
        deviceId = "".equals(deviceId) ? hostId : deviceId;
        String[] path = getPath(hostId, deviceId, event.getPath());
        Long at = event.getAt();

        Datapoint datapoint = new Datapoint(at, event.getValue());
        Datastream datastream = new Datastream(event.getDatastreamId(), Collections.singleton(datapoint));

        return new OutputDatastream(OPENGATE_VERSION, deviceId, path, Collections.singleton(datastream));
    }

    private String[] getPath(String hostId, String deviceId, String[] path) {
        if (hostId == null) {
            return null;
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
    
    void setDatastreamIdsConfigured (Collection<Set<String>> config) {
        if (!datastreamIdsConfigured.isEmpty()) {
            datastreamIdsConfigured.clear();
        }
        config.forEach(datastreamIdsConfigured::addAll);
    }
    
    private boolean periodicSentConfigured (String datastreamId) {
        return datastreamIdsConfigured.contains(datastreamId);
    }

    @Override
    public List<Event> getAndCleanCollectedValues(String id) {
        List<Event> values = collectedValues.get(id);
        if(values!=null) {
            collectedValues.remove(id);
        }
        return values;
    }
    
    private void saveCollectedValue(Event data, String datastreamId) {
        LOGGER.debug("Storing values {}", data);
        List<Event> values = collectedValues.computeIfAbsent(datastreamId, key -> new ArrayList<>());
        values.add(data);
    }
}
