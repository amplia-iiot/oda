package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.update.DeploymentElementOperation;
import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import static es.amplia.oda.operation.update.FileManager.FileException;

public abstract class DeploymentElementOperationBase implements DeploymentElementOperation {

    private final OperationUpdate.DeploymentElement deploymentElement;

    private final FileManager fileManager;

    private final OperationConfirmationProcessor operationConfirmationProcessor;

    DeploymentElementOperationBase(OperationUpdate.DeploymentElement deploymentElement,
                                   FileManager fileManager,
                                   OperationConfirmationProcessor operationConfirmationProcessor) {
        this.deploymentElement = deploymentElement;
        this.fileManager = fileManager;
        this.operationConfirmationProcessor = operationConfirmationProcessor;
    }

    @Override
    public String getName() {
        return deploymentElement.getName();
    }

    @Override
    public String getVersion() {
        return deploymentElement.getVersion();
    }

    @Override
    public void execute() throws DeploymentElementOperationException {
        try {
            executeSpecificOperation(fileManager);
            if (!operationConfirmationProcessor.waitForConfirmation(deploymentElement)) {
                throw new DeploymentElementOperationException("Operation timeout");
            }
        } catch (FileException exception) {
            throw new DeploymentElementOperationException(exception.getMessage());
        }
    }

    protected abstract void executeSpecificOperation(FileManager fileManager)
            throws FileException, DeploymentElementOperationException;

    @Override
    public void rollback(String backupFile) throws DeploymentElementOperationException {
        try {
            rollbackSpecificOperation(fileManager, backupFile);
            if (!operationConfirmationProcessor.waitForRollbackConfirmation(deploymentElement)) {
                throw new DeploymentElementOperationException("Rollback timeout");
            }
        } catch (FileException exception) {
            throw new DeploymentElementOperationException(exception.getMessage());
        }
    }

    protected abstract void rollbackSpecificOperation(FileManager fileManager, String backupFile) throws FileException;
}
