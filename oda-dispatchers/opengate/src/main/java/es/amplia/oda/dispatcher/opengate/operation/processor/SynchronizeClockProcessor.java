package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationSynchronizeClock;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationSynchronizeClock.*;

class SynchronizeClockProcessor extends OperationProcessorTemplate<String, Result> {

    static final String SYNCHRONIZE_CLOCK_OPERATION_NAME = "SYNCHRONIZE_CLOCK";


    private final OperationSynchronizeClock operationSynchronizeClock;


    SynchronizeClockProcessor(Serializer serializer, OperationSynchronizeClock operationSynchronizeClock) {
        super(serializer);
        this.operationSynchronizeClock = operationSynchronizeClock;
    }

    @Override
    String parseParameters(Request request) {
        String source = null;

        if (request.getParameters() != null) {
            source = request.getParameters().stream()
                    .filter(Objects::nonNull)
                    .filter(p -> p.getName() != null)
                    .filter(p -> p.getValue() != null)
                    .filter(p -> "source".equals(p.getName()))
                    .map(Parameter::getValue)
                    .map(ValueObject::getString)
                    .findFirst()
                    .orElse(null);
        }

        return source;
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, String params) {
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
