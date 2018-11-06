package es.amplia.oda.operation.set;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
        DatastreamsSettersFinder.Return setters = datastreamsSettersFinder.getSettersSatisfying(deviceId, datastreamIdentifiers);
        
        if(!setters.getNotFoundIds().isEmpty()) {
            String variables = setters.getNotFoundIds().stream().collect(Collectors.joining(","));
            String resultDescription = "Variables [" + variables + "] do not exist.";
            logger.warn(resultDescription);
            return CompletableFuture.completedFuture(new Result(ResultCode.ERROR_IN_PARAM, resultDescription, null));
        }

        List<VariableValue> nullDatastreamValues = values.stream()
                .filter(datastream -> datastream.getValue() == null)
                .collect(Collectors.toList());

        if (!nullDatastreamValues.isEmpty()) {
            String variables =
                    nullDatastreamValues.stream().map(VariableValue::getIdentifier).collect(Collectors.joining(","));
            String resultDescription = "Variables [" + variables + "] has no value to set.";
            logger.warn(resultDescription);
            return CompletableFuture.completedFuture(new Result(ResultCode.ERROR_IN_PARAM, resultDescription, null));
        }
        
        CompletableFuture<Result> inSequence = CompletableFuture.completedFuture(new Result(ResultCode.SUCCESSFUL, "SUCCESSFUL", new ArrayList<>()));
        
        for(VariableValue value: values) {
            inSequence = inSequence.thenCompose(result->execAndAccumulate(result, value, deviceId, setters.getSetters()));
        }

        return inSequence;
    }

    private static CompletableFuture<Result> execAndAccumulate(Result result, VariableValue value, String deviceId, Map<String, DatastreamsSetter> setters) {
        return setters
                .get(value.getIdentifier())
                .set(deviceId, value.getValue())
                .handle((ok,error)->{
                    if(error!=null) return new VariableResult(value.getIdentifier(), error.getMessage());
                    return new VariableResult(value.getIdentifier(), null);
                })
                .thenApply(e->{
                    result.getVariables().add(e);
                    return result;
                });
    }

}
