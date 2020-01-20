package es.amplia.oda.operation.api;

import lombok.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OperationUpdate {
    enum OperationResultCodes {
        SUCCESSFUL,
        // Normal Update
        OPERATION_PENDING,
        ERROR_IN_PARAM,
        NOT_SUPPORTED,
        ALREADY_IN_PROGRESS,
        ERROR_PROCESSING,
        ERROR_TIMEOUT,
        TIMEOUT_CANCELLED,
        CANCELLED,
        CANCELLED_INTERNAL,
        // Create rule
        ERROR_CREATING,
        ALREADY_EXISTS
    }

    enum StepResultCodes {
        SUCCESSFUL,
        ERROR,
        SKIPPED,
        NOT_EXECUTED
    }

    enum UpdateStepName {
        BEGINUPDATE,
        DOWNLOADFILE,
        BEGININSTALL,
        ENDINSTALL,
        ENDUPDATE
    }

    @Value
    class Result {
        OperationResultCodes resultCode;
        String resultDescription;
        List<StepResult> steps;
    }

    @Value
    class StepResult {
        UpdateStepName name;
        StepResultCodes code;
        String description;
    }

    enum DeploymentElementType {
        FIRMWARE,
        SOFTWARE,
        CONFIGURATION,
        PARAMETERS,
        RULE,
        DEFAULT
    }

    enum DeploymentElementOperationType {
        INSTALL,
        UPGRADE,
        UNINSTALL
    }

    enum DeploymentElementOption {
        MANDATORY,
        OPTIONAL
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    class DeploymentElement {
        String name;
        String version;
        @Setter DeploymentElementType type;
        String downloadUrl;
        String path;
        DeploymentElementOperationType operation;
        DeploymentElementOption option;
        Long order;
    }

    CompletableFuture<Result> update(String bundleName, String bundleVersion, List<DeploymentElement> deploymentElements);
}
