package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.ServiceLocator;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.Step;
import es.amplia.oda.operation.api.CustomOperation;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.CustomOperation.*;

class CustomOperationProcessor extends OperationProcessorTemplate<Map<String, Object>, Result> {

    private final ServiceLocator<CustomOperation> operationServiceLocator;
    private String customOperationName;


    CustomOperationProcessor(Serializer serializer, ServiceLocator<CustomOperation> operationServiceLocator) {
        super(serializer);
        this.operationServiceLocator = operationServiceLocator;
    }

    @Override
    Map<String, Object> parseParameters(Request request) {
        customOperationName = request.getName();
        return Optional.ofNullable(request.getParameters()).map(list ->
                list.stream()
                .filter(Objects::nonNull)
                .filter(param -> param.getName() != null)
                .filter(param -> param.getValue() != null)
                .collect(Collectors.toMap(Parameter::getName, this::getValue)))
                .orElse(Collections.emptyMap());
    }

    private Object getValue(Parameter parameter) {
        ValueObject valueObject = parameter.getValue();

        if (valueObject.getString() != null) {
            return valueObject.getString();
        } else if (valueObject.getNumber() != null) {
            return valueObject.getNumber();
        } else if (valueObject.getObject() != null) {
            return valueObject.getObject();
        } else if (valueObject.getArray() != null) {
            return valueObject.getArray();
        } else {
            throw new IllegalArgumentException("Parameter " + parameter.getName() + " has no value");
        }
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, String deviceIdForResponse,
                                               Map<String, Object> params) {
        return operationServiceLocator.findAll().stream()
                .filter(operation -> customOperationName.equals(operation.getOperationSatisfied()))
                .findFirst()
                .map(operation -> operation.execute(deviceIdForOperations, params))
                // Returning null to notify operation is not supported
                .orElse(null);
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId) {
        List<Step> steps = translateCustomSteps(result);
        Response response = new Response(requestId, deviceId, customOperationName, getOperationResult(result),
                result.getDescription(), steps);
        OutputOperation operation = new OutputOperation(response);
        return new Output(OPENGATE_VERSION, operation);
    }

    private List<Step> translateCustomSteps(Result result) {
        Collection<CustomOperation.Step> customSteps = result.getSteps();
        if (customSteps != null && !customSteps.isEmpty()) {
            return customSteps.stream().map(this::translateStep).collect(Collectors.toList());
        }

        return Collections.singletonList(
                new Step(customOperationName, getStepResult(result), result.getDescription(), null, null));
    }

    private Step translateStep(CustomOperation.Step customStep) {
        return new Step(customStep.getName(), getStepResult(customStep), customStep.getDescription(),
                customStep.getTimestamp(), null);
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
