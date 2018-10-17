package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

class PollerImpl implements Poller {
	private static final Logger logger = LoggerFactory.getLogger(PollerImpl.class);
	
	private DatastreamsGetterFinder datastreamsGettersFinder;
	private final EventDispatcher eventDispatcher;

	PollerImpl(DatastreamsGetterFinder datastreamsGettersFinder, EventDispatcher eventDispatcher) {
		this.datastreamsGettersFinder = datastreamsGettersFinder;
		this.eventDispatcher = eventDispatcher;

	}
	
	@Override
	public void runFor(DevicePattern deviceIdPattern, Set<String> datastreamIds) {
		logger.debug("runFor({},{})", deviceIdPattern, datastreamIds);
		DatastreamsGetterFinderImpl.Return getters = datastreamsGettersFinder.getGettersSatisfying(deviceIdPattern, datastreamIds);
		if(!getters.getNotFoundIds().isEmpty()) {
			logger.warn("No datastreamsGetters found for {}", getters.getNotFoundIds());
		}
		for (DatastreamsGetter getter : getters.getGetters()) {
			for (String deviceId: getter.getDevicesIdManaged()) {
				if (deviceIdPattern.match(deviceId)) {
					logger.debug("Getting datastream {}, for device {}", getter.getDatastreamIdSatisfied(), deviceId);
					CompletableFuture<CollectedValue> futureValue = getter.get(deviceId);
					if(futureValue!=null) {
						futureValue.thenAccept((CollectedValue data) -> sendValueAsEvent(data, getter.getDatastreamIdSatisfied(), deviceId));
						logger.debug("Get operation initiated in datastreamsGetter of {}", getter.getDatastreamIdSatisfied());
					}
					logger.trace("Finish getting datastream {}, for device {}", getter.getDatastreamIdSatisfied(), deviceId);
				}
			}
		}
	}

	private void sendValueAsEvent (CollectedValue data, String wantedId, String deviceId) {
		logger.debug("Sending values {}, for device {}", data, deviceId);
		Event event = new Event(wantedId, deviceId, null, data.getAt(), data.getValue());
		
		eventDispatcher.publish(event);
	}
}
