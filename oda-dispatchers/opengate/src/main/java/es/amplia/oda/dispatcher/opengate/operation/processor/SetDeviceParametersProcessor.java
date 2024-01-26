package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ParameterSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.RequestSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ValueSetting;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationSetDeviceParameters.*;

public class SetDeviceParametersProcessor  extends OperationProcessorTemplate<List<VariableValue>, Result> {

    public static final String SET_DEVICE_PARAMETERS_OPERATION_NAME = "SET_DEVICE_PARAMETERS";


    private final OperationSetDeviceParameters operationSetDeviceParameters;


    SetDeviceParametersProcessor(OperationSetDeviceParameters operationSetDeviceParameters) {
        this.operationSetDeviceParameters = operationSetDeviceParameters;
    }

    @Override
    List<VariableValue> parseParameters(Request request) {
        RequestSetOrConfigureOperation specificRequest = (RequestSetOrConfigureOperation) request;
        if (specificRequest.getParameters() == null) {
            throw new IllegalArgumentException("No parameters in SET_DEVICE_PARAMETERS");
        }

        ParameterSetOrConfigureOperation parameters;
        try {
            parameters = specificRequest.getParameters();
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }
        List<ValueSetting> params = parameters.getVariableList();
        if (params == null) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }
        List<VariableValue> variables = new ArrayList<>();

        for (ValueSetting setting: params) {
            variables.add(new VariableValue(setting.getName(), setting.getValue()));
        }

        if (variables.isEmpty()){
            throw new IllegalArgumentException("Parameter variableList must have at least one not null element");
        }

        return variables;
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, List<VariableValue> params) {
        return operationSetDeviceParameters.setDeviceParameters(deviceIdForOperations, params);
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId, String[] path) {
        if (result.getResultCode() == ResultCode.ERROR_IN_PARAM) {
            List<Step> steps =
                    Collections.singletonList(new Step(SET_DEVICE_PARAMETERS_OPERATION_NAME, StepResultCode.ERROR,
                            result.getResultDescription(), null, null));
            OutputOperation operation =
                    new OutputOperation(new Response(requestId, deviceId, path, SET_DEVICE_PARAMETERS_OPERATION_NAME,
                            OperationResultCode.ERROR_IN_PARAM, result.getResultDescription(), steps));
            return new Output(OPENGATE_VERSION, operation);
        } else {
            List<Object> outputVariables = result.getVariables().stream()
                    .map(this::translate)
                    .collect(Collectors.toList());
            List<Step> steps =
                    Collections.singletonList(new Step(SET_DEVICE_PARAMETERS_OPERATION_NAME, StepResultCode.SUCCESSFUL,
                            "", null, outputVariables));
            OutputOperation operation =
                    new OutputOperation(new Response(requestId, deviceId, path, SET_DEVICE_PARAMETERS_OPERATION_NAME,
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
