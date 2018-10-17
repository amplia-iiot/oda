package es.amplia.oda.operation.update;

public interface DeploymentElementOperation {
    class DeploymentElementOperationException extends Exception {
        public DeploymentElementOperationException(String message) {
            super(message);
        }
    }

    String getName();

    String getVersion();

    void execute() throws DeploymentElementOperationException;

    void rollback(String backupFile) throws DeploymentElementOperationException;
}
