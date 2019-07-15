package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationSetDeviceParameters.*;

class SetDeviceParametersProcessor  extends OperationProcessorTemplate<List<VariableValue>, Result> {

    static final String SET_DEVICE_PARAMETERS_OPERATION_NAME = "SET_DEVICE_PARAMETERS";


    private final OperationSetDeviceParameters operationSetDeviceParameters;


    SetDeviceParametersProcessor(Serializer serializer, OperationSetDeviceParameters operationSetDeviceParameters) {
        super(serializer);
        this.operationSetDeviceParameters = operationSetDeviceParameters;
    }

    @Override
    List<VariableValue> parseParameters(Request request) {
        if (request.getParameters() == null) {
            throw new IllegalArgumentException("No parameters in SET_DEVICE_PARAMETERS");
        }

        Map<String, ValueObject> params = request.getParameters().stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName() != null)
                .filter(p -> p.getValue() != null)
                .collect(Collectors.toMap(Parameter::getName, Parameter::getValue));

        if (params.size() != 1) {
            throw new IllegalArgumentException("Expected only one parameter in SET_DEVICE_PARAMETERS");
        }

        ValueObject variablesObject = params.get("variableList");
        if (variablesObject == null){
            throw new IllegalArgumentException("Parameter variableList not found");
        }
        if (variablesObject.getArray() == null) {
            throw new IllegalArgumentException("Parameter variableList of incorrect type");
        }
        List<VariableValue>  variables = variablesObject.getArray().stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(this::extractNameAndValue)
                .collect(Collectors.toList());

        if (variables.isEmpty()){
            throw new IllegalArgumentException("Parameter variableList must have at least one not null element");
        }

        return variables;
    }

    private VariableValue extractNameAndValue(Map map) {
        String variableName = (String) map.get("variableName");
        Object value = map.get("variableValue");
        return new OperationSetDeviceParameters.VariableValue(variableName, value);
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, String deviceIdForResponse,
                                               List<VariableValue> params) {
        return operationSetDeviceParameters.setDeviceParameters(deviceIdForOperations, params);
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId) {
        if (result.getResulCode() == ResultCode.ERROR_IN_PARAM) {
            List<Step> steps =
                    Collections.singletonList(new Step(SET_DEVICE_PARAMETERS_OPERATION_NAME, StepResultCode.ERROR,
                            result.getResultDescription(), 0L, null));
            OutputOperation operation =
                    new OutputOperation(new Response(requestId, deviceId, SET_DEVICE_PARAMETERS_OPERATION_NAME,
                            OperationResultCode.ERROR_IN_PARAM, result.getResultDescription(), steps));
            return new Output(OPENGATE_VERSION, operation);
        } else {
            List<Object> outputVariables = result.getVariables().stream()
                    .map(this::translate)
                    .collect(Collectors.toList());
            List<Step> steps =
                    Collections.singletonList(new Step(SET_DEVICE_PARAMETERS_OPERATION_NAME, StepResultCode.SUCCESSFUL,
                            "", 0L, outputVariables));
            OutputOperation operation =
                    new OutputOperation(new Response(requestId, deviceId, SET_DEVICE_PARAMETERS_OPERATION_NAME,
                            OperationResultCode.SUCCESSFUL, result.getResultDescription(), steps));
            return new Output(OPENGATE_VERSION, operation);
        }
    }

    private OutputVariable translate(VariableResult variableResult) {
        if (variableResult.getError() != null) {
            return new OutputVariable(variableResult.getIdentifier(), null, ERROR_RESULT, variableResult.getError());
        }
        return new OutputVariable(variableResult.getIdentifier(), null, SUCCESS_RESULT, SUCCESS_RESULT);
    }
}
