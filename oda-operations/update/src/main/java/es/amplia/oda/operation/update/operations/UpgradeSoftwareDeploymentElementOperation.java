package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import static es.amplia.oda.operation.update.FileManager.FileException;

import es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;

public class UpgradeSoftwareDeploymentElementOperation extends DeploymentElementOperationBase {

    private final String localFile;
    private final String installFolder;

    private String upgradedFile;
    private String installedFile;

    UpgradeSoftwareDeploymentElementOperation(DeploymentElement deploymentElement, String localFile, String installFolder,
                                      FileManager fileManager,
                                      OperationConfirmationProcessor operationConfirmationProcessor) {
        super(deploymentElement, fileManager, operationConfirmationProcessor);
        this.localFile = localFile;
        this.installFolder = installFolder;
    }

    @Override
    protected void executeSpecificOperation(FileManager fileManager)
            throws FileException, DeploymentElementOperationException {
        installedFile = fileManager.find(installFolder, getName());
        if (installedFile == null) {
            throw new DeploymentElementOperationException("Deployment element file to upgrade is not found");
        }
        upgradedFile = fileManager.copy(localFile, installFolder);
    }

    @Override
    protected void executeSpecificSuccessfulOperation(FileManager fileManager) throws FileException {
        fileManager.delete(installedFile);
    }

    @Override
    protected void rollbackSpecificOperation(FileManager fileManager, String backupFile) throws FileException {
        fileManager.delete(upgradedFile);
        if (fileManager.find(installFolder, backupFile) == null) fileManager.copy(backupFile, installFolder);
    }
}
