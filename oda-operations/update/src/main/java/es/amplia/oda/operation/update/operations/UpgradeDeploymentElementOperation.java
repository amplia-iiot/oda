package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import static es.amplia.oda.operation.update.FileManager.FileException;

public class UpgradeDeploymentElementOperation extends DeploymentElementOperationBase {

    private final String localFile;
    private final String installFolder;

    private String upgradedFile;

    UpgradeDeploymentElementOperation(DeploymentElement deploymentElement, String localFile, String installFolder,
                                      FileManager fileManager,
                                      OperationConfirmationProcessor operationConfirmationProcessor) {
        super(deploymentElement, fileManager, operationConfirmationProcessor);
        this.localFile = localFile;
        this.installFolder = installFolder;
    }

    @Override
    protected void executeSpecificOperation(FileManager fileManager)
            throws FileException, DeploymentElementOperationException {
        String installedFile = fileManager.find(installFolder, getName());
        if (installedFile == null) {
            throw new DeploymentElementOperationException("Deployment element file to upgrade is not found");
        }
        fileManager.delete(installedFile);
        upgradedFile = fileManager.copy(localFile, installFolder);
    }

    @Override
    protected void rollbackSpecificOperation(FileManager fileManager, String backupFile) throws FileException {
        fileManager.delete(upgradedFile);
        fileManager.copy(backupFile, installFolder);
    }
}
