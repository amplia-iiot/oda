package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import static es.amplia.oda.operation.update.FileManager.FileException;

public class UninstallDeploymentElementOperation extends DeploymentElementOperationBase {

    private final String installFolder;

    UninstallDeploymentElementOperation(DeploymentElement deploymentElement, String installFolder,
                                        FileManager fileManager,
                                        OperationConfirmationProcessor operationConfirmationProcessor) {
        super(deploymentElement, fileManager, operationConfirmationProcessor);
        this.installFolder = installFolder;
    }

    @Override
    protected void executeSpecificOperation(FileManager fileManager)
            throws DeploymentElementOperationException, FileException {
        String installedFile = fileManager.find(installFolder, getName());
        if (installedFile == null) {
            throw new DeploymentElementOperationException("Deployment element file to uninstall is not found");
        }

        fileManager.delete(installedFile);
    }

    @Override
    protected void rollbackSpecificOperation(FileManager fileManager, String backupFile) throws FileException {
        fileManager.copy(backupFile, installFolder);
    }
}
