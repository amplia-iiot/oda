package es.amplia.oda.operation.update;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;

public interface DownloadManager {
    class DownloadException extends Exception {
        public DownloadException(String message) {
            super(message);
        }
    }

    void createDownloadDirectory() throws DownloadException;

    void download(DeploymentElement deploymentElement) throws DownloadException;

    String getDownloadedFile(DeploymentElement deploymentElement);

    void deleteDownloadedFiles();
}
