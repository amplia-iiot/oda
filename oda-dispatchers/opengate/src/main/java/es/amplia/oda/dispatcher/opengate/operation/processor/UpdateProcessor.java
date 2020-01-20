package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationUpdate;

import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationUpdate.*;

class UpdateProcessor extends OperationProcessorTemplate<UpdateProcessor.UpdateParameters, Result> {

    static final String UPDATE_OPERATION_NAME = "UPDATE";


    private final OperationUpdate operationUpdate;


    UpdateProcessor(OperationUpdate operationUpdate) {
        this.operationUpdate = operationUpdate;
    }

    @Value
    static class UpdateParameters {
        String bundleName;
        String bundleVersion;
        List<OperationUpdate.DeploymentElement> deploymentElements;
    }

    @Override
    UpdateParameters parseParameters(Request request) {
        if (request.getParameters() == null) {
            throw new IllegalArgumentException("No parameters in UPDATE");
        }
        Map<String, ValueObject> params = request.getParameters().stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName() != null)
                .filter(p -> p.getValue() != null)
                .collect(Collectors.toMap(Parameter::getName, Parameter::getValue));

        if (params.size() != 3) {
            throw new IllegalArgumentException("Expected three parameters in UPDATE");
        }

        ValueObject bundleNameObject = params.get("bundleName");
        ValueObject bundleVersionObject = params.get("bundleVersion");
        ValueObject deploymentElementsObject = params.get("deploymentElements");

        if (bundleNameObject == null) {
            throw new IllegalArgumentException("Parameter bundleName not found");
        }
        if (bundleVersionObject == null) {
            throw new IllegalArgumentException("Parameter bundleVersion not found");
        }
        if (deploymentElementsObject == null){
            throw new IllegalArgumentException("Parameter deploymentElements not found");
        }

        String bundleName = bundleNameObject.getString();
        String bundleVersion = bundleVersionObject.getString();
        if (bundleName == null) {
            throw new IllegalArgumentException("Parameter bundleName of incorrect type");
        }
        if (bundleVersion == null) {
            throw new IllegalArgumentException("Parameter bundleVersion of incorrect type");
        }
        if (deploymentElementsObject.getArray() == null) {
            throw new IllegalArgumentException("Parameter deploymentElements of incorrect type");
        }
        List<OperationUpdate.DeploymentElement> deploymentElements = deploymentElementsObject.getArray().stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(this::createDeploymentElement)
                .collect(Collectors.toList());

        if (deploymentElements.isEmpty()){
            throw new IllegalArgumentException("Parameter deploymentElements must have at least one not null element");
        }

        return new UpdateParameters(bundleName, bundleVersion, deploymentElements);
    }

    private OperationUpdate.DeploymentElement createDeploymentElement(Map map) {
        String name = (String) map.get("name");
        String version = (String) map.get("version");
        OperationUpdate.DeploymentElementType type = null;
        String downloadUrl = (String) map.get("downloadUrl");
        String path = (String) map.get("path");
        OperationUpdate.DeploymentElementOperationType operation =
                Optional.ofNullable((String) map.get("operation"))
                        .map(OperationUpdate.DeploymentElementOperationType::valueOf).orElse(null);
        OperationUpdate.DeploymentElementOption option =
                Optional.ofNullable((String) map.get("option"))
                        .map(OperationUpdate.DeploymentElementOption::valueOf).orElse(null);
        Long order = Optional.ofNullable((Integer) map.get("order")).map(Long::valueOf).orElse(1L);
        return new OperationUpdate.DeploymentElement(name, version, type, downloadUrl, path, operation, option, order);
    }

    @Override
    CompletableFuture<Result> processOperation(String deviceIdForOperations, UpdateParameters params) {
        return operationUpdate.update(params.getBundleName(), params.getBundleVersion(), params.getDeploymentElements());
    }

    @Override
    Output translateToOutput(Result result, String requestId, String deviceId, String[] path) {
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
