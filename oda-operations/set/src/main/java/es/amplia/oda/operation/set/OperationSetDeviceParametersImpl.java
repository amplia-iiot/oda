package es.amplia.oda.operation.set;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;
import es.amplia.oda.core.commons.utils.DatastreamValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class OperationSetDeviceParametersImpl implements OperationSetDeviceParameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationSetDeviceParametersImpl.class);


    private final StateManager stateManager;


    OperationSetDeviceParametersImpl(StateManager stateManager) {
        this.stateManager = stateManager;
    }
    
    @Override
    public CompletableFuture<Result> setDeviceParameters(String deviceId, List<VariableValue> values) {
        LOGGER.info("Setting for the device '{}' the values: {}", deviceId, values);

        return stateManager.setDatastreamValues(deviceId, mapToDatastreamValues(values))
                .thenApply(this::createResult);
    }

    private Map<String, Object> mapToDatastreamValues(List<VariableValue> variableValues) {
        return variableValues.stream()
                .collect(Collectors.toMap(VariableValue::getIdentifier, VariableValue::getValue));
    }


    private Result createResult(Set<DatastreamValue> datastreamValues) {
        return new Result(ResultCode.SUCCESSFUL, null, getSetDatastreamsResult(datastreamValues));
    }

    private List<VariableResult> getSetDatastreamsResult(Set<DatastreamValue> datastreamValues) {
        return datastreamValues.stream()
                .map(this::toVariableResult)
                .collect(Collectors.toList());
    }

    private VariableResult toVariableResult(DatastreamValue datastreamValue) {
        return new VariableResult(datastreamValue.getDatastreamId(), getError(datastreamValue));
    }

    private String getError(DatastreamValue datastreamValue) {
        if (!DatastreamValue.Status.OK.equals(datastreamValue.getStatus())) {
            return datastreamValue.getStatus() +
                    Optional.ofNullable(datastreamValue.getError()).map(error -> ": " + error).orElse("");
        }
        return null;
    }
}
