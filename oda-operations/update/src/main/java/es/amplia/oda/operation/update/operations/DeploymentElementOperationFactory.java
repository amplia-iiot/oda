package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.update.DeploymentElementOperation;
import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;

public class DeploymentElementOperationFactory {

    private final FileManager fileManager;

    private final OperationConfirmationProcessor operationConfirmationProcessor;

    public DeploymentElementOperationFactory(FileManager fileManager,
                                             OperationConfirmationProcessor operationConfirmationProcessor) {
        this.fileManager = fileManager;
        this.operationConfirmationProcessor = operationConfirmationProcessor;
    }

    public DeploymentElementOperation createDeploymentElementOperation (DeploymentElement deploymentElement,
                                                                        String localFile, String installFolder,
                                                                        String rulesPath) {
        OperationUpdate.DeploymentElementOperationType operation = deploymentElement.getOperation();
        switch (operation) {
            case INSTALL:
                if (deploymentElement.getPath().startsWith(rulesPath)) {
                    return new InstallRuleDeploymentElementOperation(deploymentElement, localFile , installFolder, fileManager,
                            operationConfirmationProcessor, rulesPath);
                }
                return new InstallDeploymentElementOperation(deploymentElement, localFile, installFolder, fileManager,
                        operationConfirmationProcessor);
            case UPGRADE:
                if (deploymentElement.getPath().startsWith(rulesPath)) {
                    return new UpgradeRuleDeploymentElementOperation(deploymentElement, localFile, installFolder, fileManager,
                            operationConfirmationProcessor, rulesPath);
                }
                return new UpgradeDeploymentElementOperation(deploymentElement, localFile, installFolder, fileManager,
                        operationConfirmationProcessor);
            case UNINSTALL:
                return new UninstallDeploymentElementOperation(deploymentElement, installFolder, fileManager,
                        operationConfirmationProcessor);
            default:
                throw new IllegalArgumentException("Unknown deployment element operation " + operation);
        }
    }
}
