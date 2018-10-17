package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datapoint;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.Datastream;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

class OgJsonEventDispatcher implements EventDispatcher, EventCollector {

	private static final Logger logger = LoggerFactory.getLogger(OgJsonDispatcher.class);
	
    private final DeviceInfoProvider deviceInfoProvider;
    private final JsonWriter jsonWriter;
    private final OpenGateConnector connector;
    private Map<String, List<Event>> collectedValues = new HashMap<>();
    private List<String> datastreamIdsConfigured = new ArrayList<>();

    private boolean reduceBandwidthMode = false;

    OgJsonEventDispatcher(DeviceInfoProvider deviceInfoProvider, JsonWriter jsonWriter,
                          OpenGateConnector connector) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.jsonWriter = jsonWriter;
        this.connector = connector;
    }

    public void setReduceBandwidthMode(boolean reduceBandwidthMode) {
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
	        byte[] payload = jsonWriter.dumpOutput(outputEvent);
	        connector.uplink(payload);
    	}
    }

    private OutputDatastream translateToOutputDatastream(Event event) {
		String deviceId = null;
		String[] path = null;
		Long at = null;

		if (!reduceBandwidthMode) {
			deviceId = event.getDeviceId().equals("") ? deviceInfoProvider.getDeviceId() : event.getDeviceId();
			path = event.getPath();
			at = event.getAt();
		}

		Datapoint datapoint = new Datapoint(at, event.getValue());
		Datastream datastream = new Datastream(event.getDatastreamId(), Collections.singleton(datapoint));

		return new OutputDatastream(OPENGATE_VERSION, deviceId, path, Collections.singleton(datastream));
    }
    
    public void setDatastreamIdsConfigured (Collection<Set<String>> config) {
    	if (!datastreamIdsConfigured.isEmpty()) datastreamIdsConfigured.clear();
    	config.forEach((ids)->datastreamIdsConfigured.addAll(ids));
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
		if (data == null) return;
			
		List<Event> values = collectedValues.get(datastreamId);
		if (values == null) {
			values = new ArrayList<Event>();
			collectedValues.put(datastreamId, values);
		}
		values.add(data);
	}
}
