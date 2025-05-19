package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import es.amplia.oda.dispatcher.opengate.domain.update.ParameterUpdateOperation;
import es.amplia.oda.dispatcher.opengate.domain.update.RequestUpdateOperation;
import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.api.OperationUpdate.Result;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

public class UpdateProcessor extends OperationProcessorTemplate<ParameterUpdateOperation, Result> {

    public static final String UPDATE_OPERATION_NAME = "UPDATE";


    private final OperationUpdate operationUpdate;


    UpdateProcessor(OperationUpdate operationUpdate) {
        this.operationUpdate = operationUpdate;
    }

    @Override
    ParameterUpdateOperation parseParameters(Request request) {
        RequestUpdateOperation specificRequest = (RequestUpdateOperation) request;
        if (specificRequest.getParameters() == null) {
            throw new IllegalArgumentException("No parameters in UPDATE");
        }

        ParameterUpdateOperation parameters;
        try {
            parameters = specificRequest.getParameters();
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong format of input parameters");
        }
        ParameterUpdateOperation params =
                new ParameterUpdateOperation(parameters.getBundleName(), parameters.getBundleVersion(), parameters.getDeploymentElements());

        if (params.getBundleName() == null) {
            throw new IllegalArgumentException("Parameter bundleName not found");
        }
        if (params.getBundleVersion() == null) {
            throw new IllegalArgumentException("Parameter bundleVersion not found");
        }
        if (params.getDeploymentElements() == null){
            throw new IllegalArgumentException("Parameter deploymentElements not found");
        }

        if (params.getDeploymentElements().isEmpty()){
            throw new IllegalArgumentException("Parameter deploymentElements must have at least one not null element");
        }

        return params;
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, String operationId, ParameterUpdateOperation params) {
        return operationUpdate.update(operationId, params.getBundleName(), params.getBundleVersion(), params.getDeploymentElements());
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId, String[] path) {
        if (result == null) return null;
        List<Step> steps = result.getSteps().stream()
                .map(r -> new Step(translate(r.getName()), translate(r.getCode()), r.getDescription(), null, null))
                .collect(Collectors.toList());
        OutputOperation operation =
                new OutputOperation(new Response(requestId, deviceId, path, UPDATE_OPERATION_NAME,
                        translate(result.getResultCode()), result.getResultDescription(), steps));
        return new Output(OPENGATE_VERSION, operation);
    }

    private String translate(OperationUpdate.UpdateStepName stepName) {
        return stepName.toString();
    }

    private OperationResultCode translate(OperationUpdate.OperationResultCodes result) {
        return OperationResultCode.valueOf(result.toString());
    }

    private StepResultCode translate(OperationUpdate.StepResultCodes stepResult) {
        return StepResultCode.valueOf(stepResult.toString());
    }
}
