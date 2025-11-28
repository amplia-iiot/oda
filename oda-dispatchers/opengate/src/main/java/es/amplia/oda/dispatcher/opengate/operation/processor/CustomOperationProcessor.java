package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.utils.OsgiContext;
import es.amplia.oda.core.commons.utils.ServiceLocator;
import es.amplia.oda.dispatcher.opengate.domain.OperationResultCode;
import es.amplia.oda.dispatcher.opengate.domain.Output;
import es.amplia.oda.dispatcher.opengate.domain.OutputOperation;
import es.amplia.oda.dispatcher.opengate.domain.Response;
import es.amplia.oda.dispatcher.opengate.domain.Step;
import es.amplia.oda.dispatcher.opengate.domain.StepResultCode;
import es.amplia.oda.dispatcher.opengate.domain.custom.RequestCustomOperation;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import es.amplia.oda.operation.api.CustomOperation;
import es.amplia.oda.operation.api.CustomOperationUtils;
import es.amplia.oda.operation.api.CustomOperation.Result;
import es.amplia.oda.operation.api.CustomOperation.Status;
import es.amplia.oda.operation.api.engine.OperationEngine;
import es.amplia.oda.operation.api.engine.OperationEngineProxy;
import es.amplia.oda.operation.api.engine.OperationNotFound;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

class CustomOperationProcessor extends OperationProcessorTemplate<Map<String, Object>, Result> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomOperationProcessor.class);

    private final ServiceLocator<CustomOperation> operationServiceLocator;
    private final OsgiContext osgiContext;
    private final OperationEngine operationEngine;
    private String customOperationName;


    CustomOperationProcessor(ServiceLocator<CustomOperation> operationServiceLocator, OsgiContext context, OperationEngineProxy operationEngineProxy) {
        this.operationServiceLocator = operationServiceLocator;
        this.osgiContext = context;
        this.operationEngine = operationEngineProxy;
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
    CompletableFuture<Result> processOperation(String deviceIdForOperations, String operationId, Map<String, Object> params) {
        return operationServiceLocator.findAll().stream()
                .filter(operation -> customOperationName.equals(operation.getOperationSatisfied()))
                .findFirst()
                .map(operation -> operation.execute(deviceIdForOperations, operationId, params))
                // if there is no custom operation registered in OSGI that matches, check if there is any js operation script that matches
                .orElse(processOperationScript(deviceIdForOperations, operationId, params));
    }

    private CompletableFuture<Result> processOperationScript(String deviceId, String operationId, Map<String, Object> params) {
        try {
            es.amplia.oda.core.commons.utils.operation.response.Response ret = operationEngine.engine(this.customOperationName, deviceId, operationId, params, osgiContext);
            return CompletableFuture.completedFuture(CustomOperationUtils.translateToResult(ret));
        } catch (OperationNotFound e) {
            LOGGER.error("Operation " + customOperationName + " cannot be executed", e);
            // Returning null to notify operation is not supported
            return null;
        }
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId, String[] path) {
        if (result == null) return null;
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
