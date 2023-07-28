package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinder;
import es.amplia.oda.core.commons.utils.DevicePattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static es.amplia.oda.core.commons.utils.DatastreamsGettersFinder.Return;

class PollerImpl implements Poller {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollerImpl.class);


    private final DatastreamsGettersFinder datastreamsGettersFinder;
    private final DatastreamsEvent datastreamsEvent;


    PollerImpl(DatastreamsGettersFinder datastreamsGettersFinder, DatastreamsEvent datastreamsEvent) {
        this.datastreamsGettersFinder = datastreamsGettersFinder;
        this.datastreamsEvent = datastreamsEvent;
    }

    @Override
    public void poll(DevicePattern deviceIdPattern, Set<String> datastreamIds) {
        LOGGER.info("Polling({},{})", deviceIdPattern, datastreamIds);
        Return getters =
                datastreamsGettersFinder.getGettersSatisfying(deviceIdPattern, datastreamIds);
        if(!getters.getNotFoundIds().isEmpty()) {
            LOGGER.warn("No datastreamsGetters found for {}", getters.getNotFoundIds());
        }

        for (DatastreamsGetter getter : getters.getGetters()) {
            for (String deviceId: getter.getDevicesIdManaged()) {
                if (deviceIdPattern.match(deviceId)) {
                    LOGGER.debug("Getting datastream {}, for device {}", getter.getDatastreamIdSatisfied(), deviceId);
                    CompletableFuture<CollectedValue> futureValue = getter.get(deviceId);
                    if(futureValue!=null) {
                        futureValue.thenAccept(data -> sendValueAsEvent(deviceId, getter.getDatastreamIdSatisfied(), data));
                        LOGGER.debug("Get operation initiated in datastreamsGetter of {}", getter.getDatastreamIdSatisfied());
                    }
                    LOGGER.trace("Finish getting datastream {}, for device {}", getter.getDatastreamIdSatisfied(), deviceId);
                }
            }
        }
    }

    private void sendValueAsEvent (String deviceId, String datastreamId, CollectedValue data) {
        LOGGER.debug("Sending event {}, for device {}", data, deviceId);
        Map<String, Map<Long, Object>> event = new HashMap<>();
        Map<Long, Object> eventData = new HashMap<>();
        eventData.put(data.getAt(), data.getValue());
        event.put(datastreamId, eventData);
        Optional.ofNullable(datastreamsEvent)
                .ifPresent(value -> value.publish(deviceId, null, event));
    }
}
