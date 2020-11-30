package es.amplia.oda.operation.update;

import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.update.configuration.UpdateConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.operation.update.BackupManager.BackupException;
import static es.amplia.oda.operation.update.DownloadManager.DownloadException;
import static es.amplia.oda.operation.update.InstallManager.InstallException;

public class OperationUpdateImpl implements OperationUpdate {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationUpdateImpl.class);

    private String rulesPath = "";

    static class UpdateOperationException extends Exception {
        UpdateOperationException(String message) {
            super(message);
        }
    }

    private final BackupManager backupManager;
    private final DownloadManager downloadManager;
    private final InstallManager installManager;

    OperationUpdateImpl(BackupManager backupManager, DownloadManager downloadManager, InstallManager installManager) {
        this.backupManager = backupManager;
        this.downloadManager = downloadManager;
        this.installManager = installManager;
    }

    @Override
    public CompletableFuture<Result> update(String bundleName,
                                            String bundleVersion,
                                            List<DeploymentElement> deploymentElements) {
        return CompletableFuture
                .supplyAsync(() -> makeUpdate(deploymentElements));
    }

    private Result makeUpdate(List<DeploymentElement> deploymentElements) {
        Result result = new Result(OperationResultCodes.SUCCESSFUL, "", new ArrayList<>());
        try {
            beginUpdate(deploymentElements, result);
            downloadFiles(deploymentElements, result);
            install(deploymentElements, result);
            endUpdate(result);
        } catch (UpdateOperationException exception) {
            result = rollback(deploymentElements, exception.getMessage(), result);
        } catch (Exception e) {
            String cause = "Unknown exception making update operation";
            LOGGER.error(cause, e);
            result = new Result(OperationResultCodes.ERROR_PROCESSING, cause, result.getSteps());
        }
        finally {
            cleanResources();
        }

        return result;
    }

    private void beginUpdate(List<DeploymentElement> deploymentElements, Result result)
            throws UpdateOperationException {
        deploymentElements.sort(Comparator.comparing(DeploymentElement::getOrder));
        try {

            List<DeploymentElement> toBackup = deploymentElements.stream()
                                                .filter(this::needBackup)
                                                .collect(Collectors.toList());
            backupManager.createBackupDirectory();
            for (DeploymentElement deploymentElement : toBackup) {
                backupManager.backup(deploymentElement,
                        deploymentElement.getPath());
            }

            downloadManager.createDownloadDirectory();

            StepResult beginUpdateStep =
                    new StepResult(UpdateStepName.BEGINUPDATE, StepResultCodes.SUCCESSFUL,
                            "System prepared for update");
            result.getSteps().add(beginUpdateStep);
        } catch (BackupException | DownloadException exception) {
            StepResult beginUpdateStep =
                    new StepResult(UpdateStepName.BEGINUPDATE, StepResultCodes.ERROR,
                            "Error preparing system for update");
            result.getSteps().add(beginUpdateStep);

            throw new UpdateOperationException("Can not prepare system for operation update: "
                    + exception.getMessage());
        }
    }

    private boolean needBackup(DeploymentElement deploymentElement) {
        DeploymentElementOperationType operation = deploymentElement.getOperation();
        return operation == DeploymentElementOperationType.UPGRADE ||
                operation == DeploymentElementOperationType.UNINSTALL;
    }

    private void downloadFiles(List<DeploymentElement> deploymentElements, Result result) throws UpdateOperationException {
        List<DeploymentElement> toDownload = deploymentElements.stream()
                                                                .filter(this::needDownload)
                                                                .collect(Collectors.toList());

        for (DeploymentElement deploymentElement : toDownload) {
            String name = deploymentElement.getName();
            String version = deploymentElement.getVersion();

            try {
                downloadManager.download(deploymentElement);
                StepResult downloadStepResult =
                        new StepResult(UpdateStepName.DOWNLOADFILE, StepResultCodes.SUCCESSFUL,
                                String.format("%s-%s downloaded", name, version));
                result.getSteps().add(downloadStepResult);
            } catch (DownloadException exception) {
                StepResult errorDownloadStepResult =
                        new StepResult(UpdateStepName.DOWNLOADFILE, StepResultCodes.ERROR,
                                String.format("Error downloading %s-%s: %s", name, version, exception.getMessage()));
                result.getSteps().add(errorDownloadStepResult);
                throw new UpdateOperationException(String.format("Error downloading %s-%s", name, version));
            }
        }
    }

    private boolean needDownload(DeploymentElement deploymentElement) {
        DeploymentElementOperationType operation = deploymentElement.getOperation();
        return operation == DeploymentElementOperationType.INSTALL ||
                operation == DeploymentElementOperationType.UPGRADE;
    }

    private void install(List<DeploymentElement> deploymentElements, Result result) throws UpdateOperationException {
        for (DeploymentElement deploymentElement : deploymentElements) {
            String name = deploymentElement.getName();
            String version = deploymentElement.getVersion();

            StepResult beginInstallStepResult =
                    new StepResult(UpdateStepName.BEGININSTALL, StepResultCodes.SUCCESSFUL,
                            String.format("Begin installing %s-%s", name, version));
            result.getSteps().add(beginInstallStepResult);

            try {
                String localFile = downloadManager.getDownloadedFile(deploymentElement);
                installManager.install(deploymentElement, localFile);

                StepResult endInstallStepResult =
                        new StepResult(UpdateStepName.ENDINSTALL, StepResultCodes.SUCCESSFUL,
                                String.format("%s-%s installed", name, version));
                result.getSteps().add(endInstallStepResult);
            } catch (InstallException exception) {
                StepResult errorInstallStepResult =
                        new StepResult(UpdateStepName.ENDINSTALL, StepResultCodes.ERROR,
                                String.format("Error installing %s-%s: %s", name, version, exception.getMessage()));
                result.getSteps().add(errorInstallStepResult);
                throw new UpdateOperationException(String.format("Error installing %s-%s", name, version));
            }
        }
    }

    private void endUpdate(Result result) {
        StepResult endUpdateStep = new StepResult(UpdateStepName.ENDUPDATE, StepResultCodes.SUCCESSFUL, "");
        result.getSteps().add(endUpdateStep);
    }

    private void cleanResources() {
        backupManager.deleteBackupFiles();
        downloadManager.deleteDownloadedFiles();
        installManager.clearInstalledDeploymentElements();
    }

    private Result rollback(List<DeploymentElement> deploymentElements, String cause, Result result) {
        deploymentElements.forEach(element -> installManager.rollback(element, backupManager.getBackupFile(element)));
        return new Result(OperationResultCodes.ERROR_PROCESSING, cause, result.getSteps());
    }

    public void loadConfiguration(UpdateConfiguration config) {
        rulesPath = config.getRulesPath();
        installManager.loadConfig(rulesPath, config.getDeployPath(), config.getConfigurationPath());
        downloadManager.loadConfig(rulesPath, config.getDeployPath(), config.getConfigurationPath(), config.getDownloadsPath());
        backupManager.loadConfig(config.getBackupPath());
    }
}
