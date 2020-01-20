package es.amplia.oda.operation.update;


import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;

public interface InstallManager {

    class InstallException extends Exception {
        public InstallException(String message) {
            super(message);
        }
    }

    DeploymentElement assignDeployElementType(DeploymentElement deploymentElement);

    void install(DeploymentElement deploymentElement, String localFile) throws InstallException;

    void rollback(DeploymentElement deploymentElement, String backupFile);

    void clearInstalledDeploymentElements();

    void loadConfig(String rulesPath);
}
