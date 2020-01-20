package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import java.io.File;
import java.io.IOException;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import static es.amplia.oda.operation.update.FileManager.FileException;

public class InstallRuleDeploymentElementOperation extends DeploymentElementOperationBase {

    private static final String LIB_FILE = "utils.js";

    private final String localFile;
    private final String installFolder;
    private final String toInsert;

    private String installedFile;

    InstallRuleDeploymentElementOperation(DeploymentElement deploymentElement, String localFile, String installFolder,
										  FileManager fileManager,
										  OperationConfirmationProcessor operationConfirmationProcessor,
                                          String rulesPath) {
        super(deploymentElement, fileManager, operationConfirmationProcessor);
        this.localFile = localFile;
        this.installFolder = installFolder;
        this.toInsert = "load(\"" + System.getProperty("user.dir") + "/" + rulesPath + LIB_FILE + "\");\n\n";
    }

    @Override
    protected void executeSpecificOperation(FileManager fileManager) throws FileException {
        try {
            File destiny = new File(installFolder);
            if(!destiny.exists()) {
                destiny.mkdir();
            }
            installedFile = fileManager.copy(localFile, installFolder);
            fileManager.insertInFile(toInsert, 0, installedFile);
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
