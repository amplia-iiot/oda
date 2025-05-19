package es.amplia.oda.operation.update.internal;

import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import es.amplia.oda.operation.api.OperationUpdate.OperationResultCodes;
import es.amplia.oda.operation.update.DeploymentElementOperation;
import es.amplia.oda.operation.update.DeploymentElementOperation.DeploymentElementOperationException;
import es.amplia.oda.operation.update.InstallManager;
import es.amplia.oda.operation.update.operations.DeploymentElementOperationFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class InstallManagerImpl implements InstallManager {

    private static final Logger logger = LoggerFactory.getLogger(InstallManagerImpl.class);

    private final DeploymentElementOperationFactory deploymentElementOperationFactory;

    private final Map<DeploymentElement, DeploymentElementOperation> installedDeploymentElements = new HashMap<>();

    private String rulesPath;
    private String rulesUtilsPath;
    private String deployPath= "deploy/";
    private String configurationPath= "configuration/";

    public InstallManagerImpl(DeploymentElementOperationFactory deploymentElementOperationFactory) {
        this.deploymentElementOperationFactory = deploymentElementOperationFactory;
    }

    public DeploymentElement assignDeployElementType(DeploymentElement deploymentElement) {
        if (deploymentElement.getPath().equals(deployPath)) {
            deploymentElement.setType(OperationUpdate.DeploymentElementType.SOFTWARE);
            return deploymentElement;
        } else if (deploymentElement.getPath().equals(configurationPath)) {
            deploymentElement.setType(OperationUpdate.DeploymentElementType.CONFIGURATION);
            return deploymentElement;
        } else {
            if (deploymentElement.getPath().startsWith(rulesPath)
                    || deploymentElement.getPath().startsWith(rulesUtilsPath)) {
                deploymentElement.setType(OperationUpdate.DeploymentElementType.RULE);
            } else {
                deploymentElement.setType(OperationUpdate.DeploymentElementType.DEFAULT);
            }
            return deploymentElement;
        }
    }

    @Override
    public void install(DeploymentElement deploymentElement, String localFile) throws InstallException {
        DeploymentElementOperation operation = null;
        try {
            if (!validLocalFile(deploymentElement, localFile)) {
                throw  new InstallException("Deployment element file has not been downloaded");
            }

            deploymentElement = assignDeployElementType(deploymentElement);
            operation =
                    deploymentElementOperationFactory.createDeploymentElementOperation(deploymentElement, localFile,
                            deploymentElement.getPath(), rulesPath, rulesUtilsPath);
            operation.execute();
        } catch (DeploymentElementOperationException exception) {
            throw new InstallException(exception.getMessage());
        } finally {
            if (operation != null) {
                installedDeploymentElements.put(deploymentElement, operation);
            }
        }

    }

    private boolean validLocalFile(DeploymentElement deploymentElement, String localFile) {
        return deploymentElement.getOperation() == OperationUpdate.DeploymentElementOperationType.UNINSTALL
                || localFile != null;
    }

    @Override
    public void rollback(DeploymentElement deploymentElement, String backupFile) {
        if (installedDeploymentElements.containsKey(deploymentElement)) {
            DeploymentElementOperation deploymentElementOperation = installedDeploymentElements.get(deploymentElement);

            try {
                deploymentElementOperation.rollback(backupFile);
            } catch (DeploymentElementOperation.DeploymentElementOperationException exception) {
                logger.warn("Error doing rollback of {}-{}: {}", deploymentElementOperation.getName(),
                        deploymentElementOperation.getVersion(), exception.getMessage());
            }
        }
    }

    @Override
    public void clearInstalledDeploymentElements(OperationResultCodes result) {
        if (result.equals(OperationResultCodes.SUCCESSFUL)) {
            installedDeploymentElements.values().forEach(o -> {
                try {
                    o.executePostSuccessfulOperation();
                } catch (DeploymentElementOperationException e) {
                    logger.error("Error executing post acction after SUCCESSFUL operation", e);
                }
            });
        }
        installedDeploymentElements.clear();
    }

    @Override
    public void loadConfig(String rulesPath, String rulesUtilsPath, String deployPath, String configurationPath) {
        this.rulesPath = rulesPath;
        this.rulesUtilsPath = rulesUtilsPath;
        this.deployPath = deployPath;
        this.configurationPath = configurationPath;
    }
}
