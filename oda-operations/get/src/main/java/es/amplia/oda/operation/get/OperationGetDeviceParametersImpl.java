package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinder;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinder.Return;
import es.amplia.oda.core.commons.utils.DevicePattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class OperationGetDeviceParametersImpl implements OperationGetDeviceParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationGetDeviceParametersImpl.class);

    private final StateManager stateManager;
    private final DatastreamsGettersFinder datastreamsGettersFinder;
    
    OperationGetDeviceParametersImpl(StateManager stateManager, DatastreamsGettersFinder finder) {
        this.stateManager = stateManager;
        this.datastreamsGettersFinder = finder;
    }
    
    @Override
    public CompletableFuture<Result> getDeviceParameters(String deviceId, Set<String> dataStreamIds) {
        LOGGER.debug("Getting values for device '{}': {}", deviceId, dataStreamIds);

        DevicePattern deviceIdPattern = new DevicePattern(deviceId);
        Return getters = datastreamsGettersFinder.getGettersSatisfying(deviceIdPattern, dataStreamIds);
        List<Event> events = new ArrayList<>();
        List<GetValue> values = new ArrayList<>();

        getters.getNotFoundIds().forEach(ds -> values.add(new GetValue(ds, null, Status.NOT_FOUND, 0, null, "No datastream getter found for this datastream")));
        for (DatastreamsGetter getter : getters.getGetters()) {
            for (String id : getter.getDevicesIdManaged()) {
                if (deviceIdPattern.match(id)) {
                    LOGGER.debug("Getting datastream {}, for device {}", getter.getDatastreamIdSatisfied(), id);
                    CompletableFuture<CollectedValue> futureValue = getter.get(id);
                    if (futureValue != null) {
                        futureValue.thenAccept(data -> {
                            String ds = getter.getDatastreamIdSatisfied();
                            if (data.getValue() != null) events.add(new Event(ds, id, null, data.getFeed(), data.getAt(), data.getValue()));
                            values.add(toGetValue(id, ds, data));
                        });
                        LOGGER.debug("Get operation initiated in datastreamsGetter of {}", getter.getDatastreamIdSatisfied());
                    }
                    LOGGER.trace("Finish getting datastream {}, for device {}", getter.getDatastreamIdSatisfied(), deviceId);
                }
            }
        }

        if (!events.isEmpty()) stateManager.publishValues(events);

        /*CompletableFuture<Result> value = stateManager
                .getDatastreamsInformation(deviceId, dataStreamIds).thenApply(this::createResult);

        try {
            List<GetValue> result = value.get().getValues();
            for (String datastreamId : dataStreamIds) {
                for (GetValue item : result) {
                    if (item.getDatastreamId().equals(datastreamId) && item.getStatus() == Status.OK) {
                        stateManager.publishValues(Collections.singletonList(
                                new Event(item.getDatastreamId(), deviceId, null, item.getFeed(), item.getAt(), item.getValue())));
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Fail trying to create the event to send the iot data {}", e.getMessage());
        }*/

        return CompletableFuture.completedFuture(new Result(values));
    }

    /*private Result createResult(Set<DatastreamValue> datastreamValues) {
        return new Result(getDatastreamValues(datastreamValues));
    }*/

    /*private Result createResult(List<Event> events) {
        return new Result(getDatastreamValues(events));
    }*/

    /*private List<GetValue> getDatastreamValues(Set<DatastreamValue> datastreamValues) {
        return datastreamValues.stream()
                .map(this::toGetValue)
                .collect(Collectors.toList());
    }*/

    /*private List<GetValue> getDatastreamValues(List<Event> events) {
        return events.stream()
                .map(this::toGetValue)
                .collect(Collectors.toList());
    }*/

    /*private GetValue toGetValue(DatastreamValue datastreamValue) {
        return new GetValue(datastreamValue.getDatastreamId(),
                datastreamValue.getFeed(),
                toGetValueStatus(datastreamValue.getStatus()),
                datastreamValue.getAt(),
                datastreamValue.getValue(),
                datastreamValue.getError());
    }*/

    private GetValue toGetValue(String deviceId, String datastreamId, CollectedValue data) {
        String error = null;
        Status status = Status.OK;
        if (deviceId == null || datastreamId == null || data.getValue() == null) {
            error = "One of these is null : deviceId '" + deviceId + "', datastreamId '" + datastreamId + "' or value '" + data.getValue() + "'.";
            status = Status.PROCESSING_ERROR;
        }
        return new GetValue(datastreamId, data.getFeed(), status, data.getAt(), data.getValue(), error);
    }

    /*private GetValue toGetValue(Event event) {
        return new GetValue(event.getDatastreamId(),
                event.getFeed(),
                Status.OK,
                event.getAt(),
                event.getValue(),
                null);
    }*/

    /*private Status toGetValueStatus(DatastreamValue.Status status) {
        switch (status) {
            case OK:
                return Status.OK;
            case NOT_FOUND:
                return Status.NOT_FOUND;
            case PROCESSING_ERROR:
            default:
                return Status.PROCESSING_ERROR;
        }
    }*/
}
