package es.amplia.oda.operation.update;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;

public interface BackupManager {

    class BackupException extends Exception {
        public BackupException(String message) {
            super(message);
        }
    }

    void createBackupDirectory() throws BackupException;
    
    void backup(DeploymentElement deploymentElement, String basePath) throws BackupException;
    
    String getBackupFile(DeploymentElement deploymentElement);

    void deleteBackupFiles();
}
