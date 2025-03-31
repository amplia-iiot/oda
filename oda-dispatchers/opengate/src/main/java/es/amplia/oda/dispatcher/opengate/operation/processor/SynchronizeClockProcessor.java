package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ParameterSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.RequestSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ValueSetting;
import es.amplia.oda.operation.api.OperationSynchronizeClock;
import es.amplia.oda.operation.api.OperationSynchronizeClock.Result;
import es.amplia.oda.operation.api.OperationSynchronizeClock.ResultCode;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

public class SynchronizeClockProcessor extends OperationProcessorTemplate<String, Result> {

    public static final String SYNCHRONIZE_CLOCK_OPERATION_NAME = "SYNCHRONIZE_CLOCK";


    private final OperationSynchronizeClock operationSynchronizeClock;


    SynchronizeClockProcessor(OperationSynchronizeClock operationSynchronizeClock) {
        this.operationSynchronizeClock = operationSynchronizeClock;
    }

    @Override
    String parseParameters(Request request) {
        RequestSetOrConfigureOperation specificRequest = (RequestSetOrConfigureOperation) request;
        String source = null;

        ParameterSetOrConfigureOperation parameters;
        try {
            parameters = specificRequest.getParameters();
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }
        if(parameters == null) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }

        List<ValueSetting> params = parameters.getVariableList();

        if(params != null) {
            for (ValueSetting setting: params) {
                if(setting.getName().equals("source")) {
                    source = (String)setting.getValue();
                }
            }
        }

        return source;
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, String operationId, String params) {
        return operationSynchronizeClock.synchronizeClock(deviceIdForOperations, params);
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId, String[] path) {
        Step setClockStep = new Step(SYNCHRONIZE_CLOCK_OPERATION_NAME, getStepResult(result),
                result.getResultDescription(), null, null);
        Response response = new Response(requestId, deviceId, path, SYNCHRONIZE_CLOCK_OPERATION_NAME,
                getOperationResult(result), result.getResultDescription(), Collections.singletonList(setClockStep));
        OutputOperation operation = new OutputOperation(response);
        return new Output(OPENGATE_VERSION, operation);
    }

    private StepResultCode getStepResult(Result result) {
        return ResultCode.SUCCESSFUL.equals(result.getResultCode())? StepResultCode.SUCCESSFUL: StepResultCode.ERROR;
    }

    private OperationResultCode getOperationResult(Result result) {
        return ResultCode.SUCCESSFUL.equals(result.getResultCode())?
                OperationResultCode.SUCCESSFUL: OperationResultCode.ERROR_PROCESSING;
    }
}
