package es.amplia.oda.operation.update.internal;

import es.amplia.oda.operation.update.BackupManager;
import es.amplia.oda.operation.update.FileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import static es.amplia.oda.operation.update.FileManager.FileException;

public class BackupManagerImpl implements BackupManager {

    private static final Logger logger = LoggerFactory.getLogger(BackupManagerImpl.class);

    private final FileManager fileManager;

    private String backupFolder = "backup";

    private final Map<DeploymentElement, String> backupFiles = new HashMap<>();

    public BackupManagerImpl(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Override
    public void createBackupDirectory() throws BackupException {
        if (!fileManager.exist(backupFolder)) {
            try {
                fileManager.createDirectory(backupFolder);
            } catch (FileException exception) {
                throw new BackupException("Can not create backup folder");
            }
        }
    }

    @Override
    public void backup(DeploymentElement deploymentElement, String basePath) throws BackupException {
        String name = deploymentElement.getName();
        String fileToBackup = fileManager.find(basePath, name);

        if (fileToBackup == null) {
            throw new BackupException(String.format("Can not find last installed version of %s to back up", name));
        }

        try {
            String backupFile = fileManager.copy(fileToBackup, backupFolder);
            backupFiles.put(deploymentElement, backupFile);
        } catch (FileException exception) {
            throw new BackupException(String.format("Can not back up last installed version of %s: %s", name,
                    exception.getMessage()));
        }
    }

    @Override
    public String getBackupFile(DeploymentElement deploymentElement) {
        if (backupFiles.containsKey(deploymentElement)) {
            return backupFiles.get(deploymentElement);
        }
        return null;
    }

    @Override
    public void deleteBackupFiles() {
        backupFiles.values().stream()
                .filter(Objects::nonNull)
                .forEach(backupFile -> {
                    try {
                        fileManager.delete(backupFile);
                    } catch (FileException exception) {
                        logger.warn("Can not delete backup file {}", backupFile);
                    }
                });
        backupFiles.clear();
    }

    @Override
    public void loadConfig(String backupPath) {
        this.backupFolder = backupPath;
    }
}
