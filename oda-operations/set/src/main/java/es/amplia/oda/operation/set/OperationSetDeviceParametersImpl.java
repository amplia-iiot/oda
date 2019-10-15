package es.amplia.oda.operation.set;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class OperationSetDeviceParametersImpl implements OperationSetDeviceParameters {
    private static final Logger logger = LoggerFactory.getLogger(OperationSetDeviceParametersImpl.class);
    
    private final DatastreamsSettersFinder datastreamsSettersFinder;
    
    OperationSetDeviceParametersImpl(DatastreamsSettersFinder datastreamsSettersFinder) {
        this.datastreamsSettersFinder = datastreamsSettersFinder;
    }
    
    @Override
    public CompletableFuture<Result> setDeviceParameters(String deviceId, List<VariableValue> values) {
        logger.info("Setting for the device '{}' the values: {}", deviceId, values);

        Set<String> datastreamIdentifiers = values.stream()
            .map(VariableValue::getIdentifier)
            .collect(Collectors.toSet());
        DatastreamsSettersFinder.Return foundSetters = datastreamsSettersFinder.getSettersSatisfying(deviceId, datastreamIdentifiers);

        List<CompletableFuture<VariableResult>> notFoundValues = getNotFoundIdsAsFutures(foundSetters.getNotFoundIds());
        List<CompletableFuture<VariableResult>> allRecollectedValuesFutures = getFoundIdsAsFutures(deviceId, createSettersWithValues(foundSetters.getSetters(), values));
        allRecollectedValuesFutures.addAll(notFoundValues);

        CompletableFuture<Void> futureThatWillCompleteWhenAllFuturesComplete =
                CompletableFuture.allOf(allRecollectedValuesFutures.toArray(new CompletableFuture<?>[0]));

        CompletableFuture<Result> future = futureThatWillCompleteWhenAllFuturesComplete.thenApply(v -> {
            List<VariableResult> results = allRecollectedValuesFutures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
            return new Result(ResultCode.SUCCESSFUL, "SUCCESSFUL", results);
        });

        logger.debug("Wiring done. Waiting for all values to be complete.");
        return future;
    }

    private static List<CompletableFuture<VariableResult>> getNotFoundIdsAsFutures(Set<String> notFoundIds) {
        return notFoundIds.stream()
                .map(id-> CompletableFuture.completedFuture(new VariableResult(id, "Datastream not found")))
                .collect(Collectors.toList());
    }

    @Value
    private static class SetterWithValue {
        private DatastreamsSetter setter;
        private Object value;
    }

    private static List<CompletableFuture<VariableResult>> getFoundIdsAsFutures(String deviceId, List<SetterWithValue> settersWithValue) {
        return settersWithValue.stream()
                .map(setterWithValue-> getValueFromFutureHandlingExceptions(deviceId, setterWithValue.getSetter(), setterWithValue.getValue()))
                .collect(Collectors.toList());
    }

    private List<SetterWithValue> createSettersWithValues(Map<String, DatastreamsSetter> foundSetters, List<VariableValue> values) {
        return values.stream()
                .filter(variableValue -> foundSetters.containsKey(variableValue.getIdentifier()))
                .map(variableValue -> new SetterWithValue(foundSetters.get(variableValue.getIdentifier()), variableValue.getValue()))
                .collect(Collectors.toList());
    }

    private static CompletableFuture<VariableResult> getValueFromFutureHandlingExceptions(String deviceId, DatastreamsSetter setter, Object value) {
        String datastreamId = setter.getDatastreamIdSatisfied();
        try {
            if (value != null) {
                CompletableFuture<Void> getFuture = setter.set(deviceId, value);
                return getFuture.handle((ok,error)-> {
                    if (error != null) {
                        return new VariableResult(datastreamId, error.getMessage());
                    }
                    return new VariableResult(datastreamId, null);
                });
            } else {
                return CompletableFuture.completedFuture(new VariableResult(datastreamId, "Value can not be null"));
            }
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new VariableResult(datastreamId, e.getMessage()));
        }
    }
}
