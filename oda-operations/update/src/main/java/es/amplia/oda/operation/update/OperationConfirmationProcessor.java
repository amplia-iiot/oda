package es.amplia.oda.operation.update;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;

public interface OperationConfirmationProcessor {

    boolean waitForConfirmation(DeploymentElement deploymentElement);

    boolean waitForRollbackConfirmation(DeploymentElement deploymentElement);
}
