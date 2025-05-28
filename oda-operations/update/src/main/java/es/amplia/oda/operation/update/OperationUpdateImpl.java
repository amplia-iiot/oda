package es.amplia.oda.operation.update;

import es.amplia.oda.core.commons.utils.operation.response.Operation;
import es.amplia.oda.core.commons.utils.operation.response.OperationResponse;
import es.amplia.oda.core.commons.utils.operation.response.OperationResultCode;
import es.amplia.oda.core.commons.utils.operation.response.Response;
import es.amplia.oda.core.commons.utils.operation.response.Step;
import es.amplia.oda.core.commons.utils.operation.response.StepResultCode;
import es.amplia.oda.event.api.ResponseDispatcher;
import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.update.BackupManager.BackupException;
import es.amplia.oda.operation.update.DownloadManager.DownloadException;
import es.amplia.oda.operation.update.InstallManager.InstallException;
import es.amplia.oda.operation.update.configuration.UpdateConfiguration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

    private final ResponseDispatcher dispatcher;
    private final BundleContext context;

    OperationUpdateImpl(BackupManager backupManager, DownloadManager downloadManager, InstallManager installManager, ResponseDispatcher dispatcher, BundleContext bundleContext) {
        this.backupManager = backupManager;
        this.downloadManager = downloadManager;
        this.installManager = installManager;
        this.dispatcher = dispatcher;
        this.context = bundleContext;
    }

    @Override
    public CompletableFuture<Result> update(String operationId,
                                            String bundleName,
                                            String bundleVersion,
                                            List<DeploymentElement> deploymentElements) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                makeUpdate(operationId, deploymentElements);
            }
            
        });
        t.run();
        return CompletableFuture.completedFuture(null);
    }

    private void makeUpdate(String operationId, List<DeploymentElement> deploymentElements) {
        Result result = new Result(OperationResultCodes.SUCCESSFUL, "", new ArrayList<>());
        try {
            beginUpdate(deploymentElements, result);
            downloadFiles(deploymentElements, result);
            install(deploymentElements, result);
            endUpdate(result);
        } catch (UpdateOperationException exception) {
            result = rollback(deploymentElements, exception.getMessage(), result);
        } catch (Throwable e) {
            String cause = "Unknown exception making update operation";
            LOGGER.error(cause, e);
            result = new Result(OperationResultCodes.ERROR_PROCESSING, cause, result.getSteps());
        }

        try {
            sendResponse(operationId, result);
        } catch (Throwable e) {
            LOGGER.error("Error sending UPDATE response", e);
        }finally {
            cleanResources(result.getResultCode());
        }
    }

    private void sendResponse (String operationId, Result result) {
        List<Step> steps = result.getSteps().stream()
                .map(r -> new Step(r.getName().toString(), StepResultCode.valueOf(r.getCode().toString()), r.getDescription(), null, null))
                .collect(Collectors.toList());
        OperationResponse resp = new OperationResponse("9.0", new Operation
                        (new Response(operationId, null, null, "UPDATE", OperationResultCode.valueOf(result.getResultCode().toString()), result.getResultDescription(), steps)));
        dispatcher.publishResponse(resp);
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
            DeploymentElementOperationType opType = deploymentElement.getOperation();

            StepResult beginInstallStepResult =
                    new StepResult(UpdateStepName.BEGININSTALL, StepResultCodes.SUCCESSFUL,
                            String.format("Begin %s %s-%s", opTypeToIng(opType), name, version));
            result.getSteps().add(beginInstallStepResult);

            try {
                String localFile = downloadManager.getDownloadedFile(deploymentElement);
                installManager.install(deploymentElement, localFile);

                StepResult endInstallStepResult =
                        new StepResult(UpdateStepName.ENDINSTALL, StepResultCodes.SUCCESSFUL,
                                String.format("%s-%s %s", name, version, opTypeToEd(opType)));
                result.getSteps().add(endInstallStepResult);
            } catch (InstallException exception) {
                StepResult errorInstallStepResult =
                        new StepResult(UpdateStepName.ENDINSTALL, StepResultCodes.ERROR,
                                String.format("Error %s %s-%s: %s", opTypeToIng(opType), name, version, exception.getMessage()));
                result.getSteps().add(errorInstallStepResult);
                throw new UpdateOperationException(String.format("Error %s %s-%s", opTypeToIng(opType), name, version));
            }
        }
    }

    private String opTypeToIng (DeploymentElementOperationType type) {
        switch (type) {
            case UPGRADE:
                return "upgrading";
            case UNINSTALL:
                return "uninstalling";
            default:
                return "installing";
        }
    }

    private String opTypeToEd (DeploymentElementOperationType type) {
        switch (type) {
            case UPGRADE:
                return "upgraded";
            case UNINSTALL:
                return "uninstalled";
            default:
                return "installed";
        }
    }

    private void endUpdate(Result result) {
        StepResult endUpdateStep = new StepResult(UpdateStepName.ENDUPDATE, StepResultCodes.SUCCESSFUL, "");
        result.getSteps().add(endUpdateStep);
    }

    private void cleanResources(OperationResultCodes result) {
        backupManager.deleteBackupFiles();
        downloadManager.deleteDownloadedFiles();
        installManager.clearInstalledDeploymentElements(result);
    }

    private Result rollback(List<DeploymentElement> deploymentElements, String cause, Result result) {
        deploymentElements.forEach(element -> installManager.rollback(element, backupManager.getBackupFile(element)));
        return new Result(OperationResultCodes.ERROR_PROCESSING, cause, result.getSteps());
    }

    public void loadConfiguration(UpdateConfiguration config) {
        rulesPath = config.getRulesPath();
        installManager.loadConfig(rulesPath, config.getRulesUtilsPath(), config.getDeployPath(),
                config.getConfigurationPath());
        downloadManager.loadConfig(rulesPath, config.getRulesUtilsPath(), config.getDeployPath(),
                config.getConfigurationPath(), config.getDownloadsPath());
        backupManager.loadConfig(config.getBackupPath());

        File updateBackupPath = new File(config.getBackupPath());
        if (updateBackupPath.exists()) {
            LOGGER.debug("Backup folder found: {}", config.getBackupPath());
            String[] backups = updateBackupPath.list();
            Arrays.asList(backups).forEach(f -> {
                LOGGER.debug("Found file {} in Backup folder", f);
                if (f.startsWith(context.getBundle().getSymbolicName()) && f.endsWith(".jar")) { // Así nos aseguramos de que es un fichero de software
                    LOGGER.info("Found UPDATE bundle {} in Backup folder", f);
                    String version = f.split("-")[1].replace(".jar", "");
                    String []vArray = version.split("\\.");
                    LOGGER.info("   With version: mayor {}, minor {} & bugfix {}", vArray[0], vArray[1], vArray[2]);
                    if ( (Integer.parseInt(vArray[0]) <= 4) && (Integer.parseInt(vArray[1]) <= 12) ) {
                        // Al ser una versión antigua borramos todo lo que haya en la carpeta de 'bakcup' y en la de 'download'
                        Arrays.asList(backups).forEach(fb -> {
                            File backupFile = new File(config.getBackupPath() + "/" + fb);
                            try {
                                Files.delete(backupFile.toPath());
                            } catch (IOException e) {
                                LOGGER.error("Error deleting backup file " + f, e);
                            }
                        });
                        Arrays.asList((new File(config.getDownloadsPath())).list()).forEach(fd -> {
                            File downloadFile = new File(config.getDownloadsPath() + "/" + fd);
                            try {
                                Files.delete(downloadFile.toPath());
                            } catch (IOException e) {
                                LOGGER.error("Error deleting backup file " + f, e);
                            }
                        });

                        // Al ser una versión antigua reiniciamos todos los bundles para que finalice la máquina virtual y termine el hilo que espera por el inicio del nuevo bundle
                        Thread t = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    // Do nothing
                                }
                                Bundle[] bundlesArray = context.getBundles();
                                for (int i = 0; i < bundlesArray.length; i++) {
                                    Bundle bdl = bundlesArray[i];
                                    String bdSymbolicName = bdl.getSymbolicName();
                                    //if (bdSymbolicName.startsWith("es.amplia") && !bdSymbolicName.equals(symbolicName)) {
                                        try {
                                            bdl.stop();
                                            LOGGER.info("Bundle " + bdSymbolicName + " STOPPED due to Update of old OperationUpdate bundle");
                                        } catch (BundleException e) {
                                            LOGGER.error("Error stopping bundle " + bdSymbolicName , e);
                                        }
                                    //}
                                }
                            }
                            
                        });
                        t.run();

                        return;
                    }
                }
            });
        }
    }
}
