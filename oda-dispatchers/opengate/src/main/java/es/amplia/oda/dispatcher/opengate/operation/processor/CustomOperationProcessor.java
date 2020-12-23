package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.utils.ServiceLocator;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.Step;
import es.amplia.oda.dispatcher.opengate.domain.custom.RequestCustomOperation;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ParameterSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.RequestSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ValueSetting;
import es.amplia.oda.operation.api.CustomOperation;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.CustomOperation.*;

class CustomOperationProcessor extends OperationProcessorTemplate<Map<String, Object>, Result> {

    private final ServiceLocator<CustomOperation> operationServiceLocator;
    private String customOperationName;


    CustomOperationProcessor(ServiceLocator<CustomOperation> operationServiceLocator) {
        this.operationServiceLocator = operationServiceLocator;
    }

    @Override
    Map<String, Object> parseParameters(Request request) {
        RequestCustomOperation specificRequest = (RequestCustomOperation) request;

        if(request.getName() == null) {
            throw new IllegalArgumentException("Parameter " + request.getName() + " has no value");
        }
        customOperationName = request.getName();

        Map<String, Object> parameters;
        try {
            parameters = specificRequest.getParameters();
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }

        return parameters;
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, Map<String, Object> params) {
        return operationServiceLocator.findAll().stream()
                .filter(operation -> customOperationName.equals(operation.getOperationSatisfied()))
                .findFirst()
                .map(operation -> operation.execute(deviceIdForOperations, params))
                // Returning null to notify operation is not supported
                .orElse(null);
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId, String[] path) {
        List<Step> steps = translateCustomSteps(result);
        Response response = new Response(requestId, deviceId, path, customOperationName, getOperationResult(result),
                result.getDescription(), steps);
        OutputOperation operation = new OutputOperation(response);
        return new Output(OPENGATE_VERSION, operation);
    }

    private List<Step> translateCustomSteps(Result result) {
        Collection<CustomOperation.Step> customSteps = result.getSteps();
        if (customSteps != null && !customSteps.isEmpty()) {
            return customSteps.stream().map(this::translateStep).collect(Collectors.toList());
        } else if (result.getStatus().equals(Status.SUCCESSFUL)) {
            return Collections.singletonList(
                    new Step(customOperationName, getStepResult(result), result.getDescription(), null, null));
        }
        return Collections.emptyList();
    }

    private Step translateStep(CustomOperation.Step customStep) {
        return new Step(customStep.getName(), getStepResult(customStep), customStep.getDescription(),
                customStep.getTimestamp(), customStep.getResponses());
    }

    private StepResultCode getStepResult(CustomOperation.Step customStep) {
        switch (customStep.getResult()) {
            case SUCCESSFUL:
                return StepResultCode.SUCCESSFUL;
            case ERROR:
                return StepResultCode.ERROR;
            case SKIPPED:
                return StepResultCode.SKIPPED;
            case NOT_EXECUTED:
                return StepResultCode.NOT_EXECUTED;
            default:
                throw new IllegalArgumentException("Unknown Custom Step result");
        }
    }

    private StepResultCode getStepResult(Result result) {
        return Status.SUCCESSFUL.equals(result.getStatus())? StepResultCode.SUCCESSFUL: StepResultCode.ERROR;
    }

    private OperationResultCode getOperationResult(Result result) {
        return Status.SUCCESSFUL.equals(result.getStatus())?
                OperationResultCode.SUCCESSFUL: OperationResultCode.ERROR_PROCESSING;
    }
}
