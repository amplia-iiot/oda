package es.amplia.oda.operation.refreshinfo;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.operation.api.OperationRefreshInfo;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

class OperationRefreshInfoImpl implements OperationRefreshInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationRefreshInfoImpl.class);

    private final StateManager stateManager;

    OperationRefreshInfoImpl(StateManager stateManager) {
        this.stateManager = stateManager;
    }
    
    @Override
    public CompletableFuture<Result> refreshInfo(String deviceId) {
        LOGGER.debug("Refreshing information for all datastreams registered in ODA");

        CompletableFuture<Result> value = stateManager.getDeviceInformation(deviceId).thenApply(this::createResult);

        try{
            Map<String, List<RefreshInfoValue>> result = value.get().getValues();
            for (Map.Entry<String, List<RefreshInfoValue>> entry: result.entrySet()){
                List<Event> events = new ArrayList<>();
                for (RefreshInfoValue item : entry.getValue()) {
                    Event event = new Event(item.getDatastreamId(), deviceId, null, item.getFeed(), item.getAt(), item.getValue());
                    events.add(event);
                }
                stateManager.publishValues(events);
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Fail trying to create the event to send the iot data {}", e.getMessage());
        }

        return value;
    }

    private OperationRefreshInfo.Result createResult(Set<DatastreamValue> datastreamValues) {
        return new Result(getDatastreamValues(datastreamValues));
    }

    private Map<String, List<RefreshInfoValue>> getDatastreamValues(Set<DatastreamValue> datastreamValues) {
        return datastreamValues.stream()
                .filter(datastreamValue -> DatastreamValue.Status.OK.equals(datastreamValue.getStatus()))
                .collect(Collectors.groupingBy(DatastreamValue::getDatastreamId,
                        Collectors.mapping(datastreamValue ->
                                        new RefreshInfoValue(
                                                datastreamValue.getDatastreamId(),
                                                datastreamValue.getFeed(),
                                                Status.OK,
                                                datastreamValue.getAt(),
                                                datastreamValue.getValue(),
                                                datastreamValue.getError()),
                                Collectors.toList())));
    }
}
