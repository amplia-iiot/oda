package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import java.io.IOException;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import static es.amplia.oda.operation.update.FileManager.FileException;

public class UpgradeRuleDeploymentElementOperation extends DeploymentElementOperationBase {

    private static final String libFile = "utils.js";

    private final String localFile;
    private final String installFolder;
    private final String toInsert;

    private String upgradedFile;

    UpgradeRuleDeploymentElementOperation(DeploymentElement deploymentElement, String localFile, String installFolder,
										  FileManager fileManager,
										  OperationConfirmationProcessor operationConfirmationProcessor,
                                          String rulesPath) {
        super(deploymentElement, fileManager, operationConfirmationProcessor);
        this.localFile = localFile;
        this.installFolder = installFolder;
        this.toInsert = "load(\"" + System.getProperty("user.dir") + "/" + rulesPath + libFile + "\");\n\n";
    }

    @Override
    protected void executeSpecificOperation(FileManager fileManager)
            throws FileException, DeploymentElementOperationException {
        try {
            String installedFile = fileManager.find(installFolder, getName());
            if (installedFile == null) {
                throw new DeploymentElementOperationException("Deployment element file to upgrade is not found");
            }
            fileManager.delete(installedFile);
            upgradedFile = fileManager.copy(localFile, installFolder + ".js");
            fileManager.insertInFile(toInsert, 0, installedFile);
        } catch (IOException e) {
            throw new FileException(e.getMessage());
        }
    }

    @Override
    protected void rollbackSpecificOperation(FileManager fileManager, String backupFile) throws FileException {
        fileManager.delete(upgradedFile);
        fileManager.copy(backupFile, installFolder);
    }
}
