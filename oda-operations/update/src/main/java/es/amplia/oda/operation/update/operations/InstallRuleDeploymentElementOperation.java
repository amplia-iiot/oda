package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import java.io.File;
import java.io.IOException;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import static es.amplia.oda.operation.update.FileManager.FileException;

public class InstallRuleDeploymentElementOperation extends DeploymentElementOperationBase {


    private final String localFile;
    private final String installFolder;

    private String installedFile;

    InstallRuleDeploymentElementOperation(DeploymentElement deploymentElement, String localFile, String installFolder,
										  FileManager fileManager,
										  OperationConfirmationProcessor operationConfirmationProcessor) {
        super(deploymentElement, fileManager, operationConfirmationProcessor);
        this.localFile = localFile;
        this.installFolder = installFolder;
    }

    @Override
    protected void executeSpecificOperation(FileManager fileManager) throws FileException {
        try {
            File destiny = new File(installFolder);
            if(!destiny.exists() && !destiny.mkdir()) {
                throw new IOException("Impossible create new directory of rule's datastream");
            }
            installedFile = fileManager.copy(localFile, installFolder);
        } catch (IOException e) {
            throw new FileException(e.getMessage());
        }
    }

    @Override
    protected void rollbackSpecificOperation(FileManager fileManager, String backupFile) throws FileException {
        if (installedFile != null) {
            fileManager.delete(installedFile);
        }
    }
}
