package es.amplia.oda.operation.update;

import es.amplia.oda.operation.api.OperationUpdate;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;

public interface InstallManager {

    class InstallException extends Exception {
        public InstallException(String message) {
            super(message);
        }
    }

    String getInstallDirectory(OperationUpdate.DeploymentElementType type);

    void install(DeploymentElement deploymentElement, String localFile) throws InstallException;

    void rollback(DeploymentElement deploymentElement, String backupFile);

    void clearInstalledDeploymentElements();
}
