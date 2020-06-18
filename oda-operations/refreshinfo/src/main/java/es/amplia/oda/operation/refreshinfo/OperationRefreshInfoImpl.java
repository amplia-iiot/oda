package es.amplia.oda.operation.refreshinfo;

import es.amplia.oda.operation.api.OperationRefreshInfo;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.statemanager.api.StateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class OperationRefreshInfoImpl implements OperationRefreshInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationRefreshInfoImpl.class);

    private final StateManager stateManager;

    OperationRefreshInfoImpl(StateManager stateManager) {
        this.stateManager = stateManager;
    }
    
    @Override
    public CompletableFuture<Result> refreshInfo(String deviceId) {
        LOGGER.info("Refreshing information for all datastreams registered in ODA");
        return stateManager.getDeviceInformation(deviceId).thenApply(this::createResult);
    }

    private OperationRefreshInfo.Result createResult(Set<DatastreamValue> datastreamValues) {
        return new Result(getDatastreamValues(datastreamValues));
    }

    private Map<String, Object> getDatastreamValues(Set<DatastreamValue> datastreamValues) {
        return datastreamValues.stream()
                .filter(datastreamValue -> DatastreamValue.Status.OK.equals(datastreamValue.getStatus()))
                .collect(Collectors.toMap(DatastreamValue::getDatastreamId, DatastreamValue::getValue));
    }
}
