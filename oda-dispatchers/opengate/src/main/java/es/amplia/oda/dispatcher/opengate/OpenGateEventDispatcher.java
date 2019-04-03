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

    private static final Logger logger = LoggerFactory.getLogger(OpenGateOperationDispatcher.class);
    
    private final DeviceInfoProvider deviceInfoProvider;
    private final Serializer serializer;
    private final OpenGateConnector connector;
    private final Map<String, List<Event>> collectedValues = new HashMap<>();
    private final List<String> datastreamIdsConfigured = new ArrayList<>();

    private boolean reduceBandwidthMode = false;

    OpenGateEventDispatcher(DeviceInfoProvider deviceInfoProvider, Serializer serializer,
                            OpenGateConnector connector) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.serializer = serializer;
        this.connector = connector;
    }

    void setReduceBandwidthMode(boolean reduceBandwidthMode) {
        this.reduceBandwidthMode = reduceBandwidthMode;
    }

    @Override
    public void publish(Event event) {
        if (periodicSentConfigured(event.getDatastreamId())) {
            // If periodic sent is configured for the Datastream ID then save collected value
            saveCollectedValue(event, event.getDatastreamId());
        } else {
            // Else send the value
            OutputDatastream outputEvent = translateToOutputDatastream(event);
            byte[] payload = new byte[0];
            try {
                payload = serializer.serialize(outputEvent);
                connector.uplink(payload);
            } catch (IOException e) {
				logger.error("Event couldn't be serialize, it won't publish anything");
            }

        }
    }

    private OutputDatastream translateToOutputDatastream(Event event) {
        String deviceId = null;
        String[] path = null;
        Long at = null;

        if (!reduceBandwidthMode) {
            String hostId = deviceInfoProvider.getDeviceId();
            deviceId = event.getDeviceId();
            deviceId = deviceId.equals("") ? hostId : deviceId;
            path = getPath(hostId, deviceId, event.getPath());
            at = event.getAt();
        }

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
        logger.debug("Storing values {}", data);
        if (data == null) {
            return;
        }

        List<Event> values = collectedValues.computeIfAbsent(datastreamId, key -> new ArrayList<>());
        values.add(data);
    }
}
