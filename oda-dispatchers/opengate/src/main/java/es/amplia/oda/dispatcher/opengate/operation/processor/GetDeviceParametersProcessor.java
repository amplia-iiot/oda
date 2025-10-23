package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.get.ParameterGetOperation;
import es.amplia.oda.dispatcher.opengate.domain.get.RequestGetOperation;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.operation.api.OperationGetDeviceParameters.GetValue;
import es.amplia.oda.operation.api.OperationGetDeviceParameters.Result;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

public class GetDeviceParametersProcessor extends OperationProcessorTemplate<Set<String>, Result> {

    public static final String GET_DEVICE_PARAMETERS_OPERATION_NAME = "GET_DEVICE_PARAMETERS";


    private final OperationGetDeviceParameters operationGetDeviceParameters;


    GetDeviceParametersProcessor(OperationGetDeviceParameters operationGetDeviceParameters) {
        this.operationGetDeviceParameters = operationGetDeviceParameters;
    }

    @Override
    Set<String> parseParameters(Request request) {
        RequestGetOperation specificRequest = (RequestGetOperation) request;
        if (specificRequest.getParameters() == null) {
            throw new IllegalArgumentException("No parameters in GET_DEVICE_PARAMETERS");
        }

        ParameterGetOperation parameters;
        try {
            parameters = specificRequest.getParameters();
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }
        List<String> params = parameters.getVariableList();

        if (params == null){
            throw new IllegalArgumentException("Parameter variableList must have at least one not null element");
        }

        Set<String> set = new HashSet<>(params);

        if (set.isEmpty() || set.contains(null)){
            throw new IllegalArgumentException("Parameter variableList must have at least one not null element");
        }

        return set;
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, String operationId, Set<String> params) {
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
