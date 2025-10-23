package es.amplia.oda.operation.update;


import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;

import es.amplia.oda.operation.api.OperationUpdate.OperationResultCodes;

public interface InstallManager {

    class InstallException extends Exception {
        public InstallException(String message) {
            super(message);
        }
    }

    DeploymentElement assignDeployElementType(DeploymentElement deploymentElement);

    void install(DeploymentElement deploymentElement, String localFile) throws InstallException;

    void rollback(DeploymentElement deploymentElement, String backupFile);

    void clearInstalledDeploymentElements(OperationResultCodes result);

    void loadConfig(String rulesPath, String rulesUtilsPath, String deployPath, String configurationPath);
}
