package es.amplia.oda.operation.get;

import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.statemanager.api.StateManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class OperationGetDeviceParametersImpl implements OperationGetDeviceParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationGetDeviceParametersImpl.class);

    private final StateManager stateManager;
    
    OperationGetDeviceParametersImpl(StateManager stateManager) {
        this.stateManager = stateManager;
    }
    
    @Override
    public CompletableFuture<Result> getDeviceParameters(String deviceId, Set<String> dataStreamIds) {
        LOGGER.debug("Getting values for device '{}': {}", deviceId, dataStreamIds);

        return stateManager.getDatastreamsInformation(deviceId, dataStreamIds).thenApply(this::createResult);
    }

    private Result createResult(Set<DatastreamValue> datastreamValues) {
        return new Result(getDatastreamValues(datastreamValues));
    }

    private List<GetValue> getDatastreamValues(Set<DatastreamValue> datastreamValues) {
        return datastreamValues.stream()
                .map(this::toGetValue)
                .collect(Collectors.toList());
    }

    private GetValue toGetValue(DatastreamValue datastreamValue) {
        return new GetValue(datastreamValue.getDatastreamId(),
                toGetValueStatus(datastreamValue.getStatus()),
                datastreamValue.getValue(),
                datastreamValue.getError());
    }

    private Status toGetValueStatus(DatastreamValue.Status status) {
        switch (status) {
            case OK:
                return Status.OK;
            case NOT_FOUND:
                return Status.NOT_FOUND;
            case PROCESSING_ERROR:
            default:
                return Status.PROCESSING_ERROR;
        }
    }
}
