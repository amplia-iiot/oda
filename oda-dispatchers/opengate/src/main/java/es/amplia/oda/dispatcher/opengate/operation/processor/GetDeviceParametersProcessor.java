package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationGetDeviceParameters.*;

class GetDeviceParametersProcessor extends OperationProcessorTemplate<Set<String>, Result> {

    static final String GET_DEVICE_PARAMETERS_OPERATION_NAME = "GET_DEVICE_PARAMETERS";


    private final OperationGetDeviceParameters operationGetDeviceParameters;


    GetDeviceParametersProcessor(Serializer serializer, OperationGetDeviceParameters operationGetDeviceParameters) {
        super(serializer);
        this.operationGetDeviceParameters = operationGetDeviceParameters;
    }

    @Override
    Set<String> parseParameters(Request request) {
        if (request.getParameters() == null) {
            throw new IllegalArgumentException("No parameters in GET_DEVICE_PARAMETERS");
        }

        Map<String, ValueObject> params = request.getParameters().stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName() != null)
                .filter(p -> p.getValue() != null)
                .collect(Collectors.toMap(Parameter::getName, Parameter::getValue));

        if (params.size() != 1) {
            throw new IllegalArgumentException("Expected one parameter in GET_DEVICE_PARAMETERS");
        }

        ValueObject variablesObject = params.get("variableList");
        if (variablesObject == null){
            throw new IllegalArgumentException("Parameter variableList not found");
        }
        if (variablesObject.getArray() == null) {
            throw new IllegalArgumentException("Parameter variableList of incorrect type");
        }
        Set<String> variables = variablesObject.getArray().stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(map -> (String) map.get("variableName"))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (variables.isEmpty()){
            throw new IllegalArgumentException("Parameter variableList must have at least one not null element");
        }

        return variables;
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, Set<String> params) {
        return operationGetDeviceParameters.getDeviceParameters(deviceIdForOperations, params);
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId, String[] path) {
        List<Object> outputVariables = result.getValues().stream()
                .map(this::translateGetResultToOutput)
                .collect(Collectors.toList());

        List<Step> steps =
                Collections.singletonList(new Step(GET_DEVICE_PARAMETERS_OPERATION_NAME, StepResultCode.SUCCESSFUL, "",
                        null, outputVariables));
        OutputOperation operation = new OutputOperation(new Response(requestId, deviceId, path,
                GET_DEVICE_PARAMETERS_OPERATION_NAME, OperationResultCode.SUCCESSFUL, "No Error.", steps));
        return new Output(OPENGATE_VERSION, operation);
    }

    private OutputVariable translateGetResultToOutput(GetValue v) {
        switch (v.getStatus()) {
            case OK:
                return new OutputVariable(v.getDatastreamId(), v.getValue(), SUCCESS_RESULT, SUCCESS_RESULT);
            case PROCESSING_ERROR:
                return new OutputVariable(v.getDatastreamId(), null, ERROR_RESULT, v.getError());
            case NOT_FOUND:
            default:
                return new OutputVariable(v.getDatastreamId(), null, "NON_EXISTENT", "No datastream found");
        }
    }
}
