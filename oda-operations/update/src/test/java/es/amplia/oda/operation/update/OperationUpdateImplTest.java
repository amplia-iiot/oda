package es.amplia.oda.operation.update;

import es.amplia.oda.operation.api.OperationUpdate;

import es.amplia.oda.operation.update.configuration.UpdateConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.operation.api.OperationUpdate.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OperationUpdateImplTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION_1 = "1.0.0";
    @Mock
    private BackupManager mockedBackupManager;
    @Mock
    private DownloadManager mockedDownloadManager;
    @Mock
    private InstallManager mockedInstallManager;
    @InjectMocks
    private OperationUpdateImpl operationUpdate;

    private final OperationUpdate.DeploymentElement installDeploymentElement =
            new OperationUpdate.DeploymentElement("testDeploymentElement",
                    TEST_VERSION_1,
                    OperationUpdate.DeploymentElementType.SOFTWARE,
                    "testUrl1",
                    "testPath1",
                    OperationUpdate.DeploymentElementOperationType.INSTALL,
                    OperationUpdate.DeploymentElementOption.MANDATORY,
                    1L);
    private final OperationUpdate.DeploymentElement upgradeDeploymentElement =
            new OperationUpdate.DeploymentElement("testDeploymentElement",
                    "2.0.0",
                    OperationUpdate.DeploymentElementType.CONFIGURATION,
                    "testUrl2",
                    "testPath2",
                    OperationUpdate.DeploymentElementOperationType.UPGRADE,
                    OperationUpdate.DeploymentElementOption.OPTIONAL,
                    2L);
    private final OperationUpdate.DeploymentElement uninstallDeploymentElement =
            new OperationUpdate.DeploymentElement("testDeploymentElement3",
                    "3.3.3",
                    OperationUpdate.DeploymentElementType.FIRMWARE,
                    "testUrl3",
                    "testPath3",
                    OperationUpdate.DeploymentElementOperationType.UNINSTALL,
                    OperationUpdate.DeploymentElementOption.OPTIONAL,
                    3L);
    private final List<OperationUpdate.DeploymentElement> testDeploymentElements =
            Arrays.asList(installDeploymentElement, upgradeDeploymentElement, uninstallDeploymentElement);

    private final int numOfDeploymentElements = testDeploymentElements.size();
    private final int deploymentElementsToBackup =
            (int) testDeploymentElements.stream()
                    .filter(deploymentElement -> deploymentElement.getOperation() == OperationUpdate.DeploymentElementOperationType.UPGRADE ||
                            deploymentElement.getOperation() == OperationUpdate.DeploymentElementOperationType.UNINSTALL).count();
    private final int deploymentElementsToDownload =
            (int) testDeploymentElements.stream()
                    .filter(deploymentElement -> deploymentElement.getOperation() == OperationUpdate.DeploymentElementOperationType.INSTALL ||
                            deploymentElement.getOperation() == OperationUpdate.DeploymentElementOperationType.UPGRADE).count();

    @Test
    public void testUpdate() throws ExecutionException, InterruptedException, BackupManager.BackupException,
            DownloadManager.DownloadException, InstallManager.InstallException {
        CompletableFuture<OperationUpdate.Result> future =
                operationUpdate.update(TEST_NAME, TEST_VERSION_1, testDeploymentElements);
        OperationUpdate.Result result = future.get();

        assertEquals(OperationResultCodes.SUCCESSFUL, result.getResultCode());
        assertStepResult(UpdateStepName.BEGINUPDATE, StepResultCodes.SUCCESSFUL, result);
        assertStepResult(UpdateStepName.DOWNLOADFILE, StepResultCodes.SUCCESSFUL, deploymentElementsToDownload, result);
        assertStepResult(UpdateStepName.BEGININSTALL, StepResultCodes.SUCCESSFUL, numOfDeploymentElements, result);
        assertStepResult(UpdateStepName.ENDINSTALL, StepResultCodes.SUCCESSFUL, numOfDeploymentElements, result);
        assertStepResult(UpdateStepName.ENDUPDATE, StepResultCodes.SUCCESSFUL, result);

        verify(mockedBackupManager).createBackupDirectory();
        verify(mockedBackupManager, times(deploymentElementsToBackup))
                .backup(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedDownloadManager).createDownloadDirectory();
        verify(mockedDownloadManager, times(deploymentElementsToDownload))
                .download(any(OperationUpdate.DeploymentElement.class));
        verify(mockedInstallManager, times(numOfDeploymentElements))
                .install(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedBackupManager).deleteBackupFiles();
        verify(mockedDownloadManager).deleteDownloadedFiles();
        verify(mockedInstallManager).clearInstalledDeploymentElements();
    }

    private void assertStepResult(OperationUpdate.UpdateStepName name,
                                  OperationUpdate.StepResultCodes code,
                                  OperationUpdate.Result result) {
        assertStepResult(name, code, 1, result);
    }

    private void assertStepResult(OperationUpdate.UpdateStepName name,
                                  OperationUpdate.StepResultCodes code,
                                  int number,
                                  OperationUpdate.Result result) {
        List<OperationUpdate.StepResult> stepResults = result.getSteps();
        assertEquals(number,
                stepResults.stream()
                        .filter(stepResult -> stepResult.getName() == name && stepResult.getCode() == code)
                        .count());
    }

    @Test
    public void testUpdateErrorPreparingSystem() throws BackupManager.BackupException, ExecutionException,
            InterruptedException, DownloadManager.DownloadException, InstallManager.InstallException {
        doThrow(new BackupManager.BackupException("")).when(mockedBackupManager)
                .backup(any(OperationUpdate.DeploymentElement.class), anyString());

        CompletableFuture<OperationUpdate.Result> future =
                operationUpdate.update(TEST_NAME, TEST_VERSION_1, testDeploymentElements);
        OperationUpdate.Result result = future.get();

        assertEquals(OperationResultCodes.ERROR_PROCESSING, result.getResultCode());
        assertStepResult(UpdateStepName.BEGINUPDATE, StepResultCodes.ERROR, result);

        verify(mockedBackupManager).createBackupDirectory();
        verify(mockedBackupManager, atLeastOnce()).backup(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedDownloadManager, times(0)).download(any(OperationUpdate.DeploymentElement.class));
        verify(mockedInstallManager, times(0)).install(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedBackupManager, times(numOfDeploymentElements))
                .getBackupFile(any(OperationUpdate.DeploymentElement.class));
        verify(mockedInstallManager, times(numOfDeploymentElements))
                .rollback(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedBackupManager).deleteBackupFiles();
        verify(mockedDownloadManager).deleteDownloadedFiles();
        verify(mockedInstallManager).clearInstalledDeploymentElements();
    }

    @Test
    public void testUpdateErrorDownloading() throws DownloadManager.DownloadException, ExecutionException,
            InterruptedException, BackupManager.BackupException, InstallManager.InstallException {
        doNothing().doThrow(new DownloadManager.DownloadException("")).when(mockedDownloadManager)
                .download(any(OperationUpdate.DeploymentElement.class));

        CompletableFuture<OperationUpdate.Result> future =
                operationUpdate.update(TEST_NAME, TEST_VERSION_1, testDeploymentElements);
        OperationUpdate.Result result = future.get();

        assertEquals(OperationResultCodes.ERROR_PROCESSING, result.getResultCode());
        assertStepResult(UpdateStepName.BEGINUPDATE, StepResultCodes.SUCCESSFUL, result);
        assertStepResult(UpdateStepName.DOWNLOADFILE, StepResultCodes.SUCCESSFUL, result);
        assertStepResult(UpdateStepName.DOWNLOADFILE, StepResultCodes.ERROR, result);

        verify(mockedBackupManager).createBackupDirectory();
        verify(mockedBackupManager, times(deploymentElementsToBackup))
                .backup(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedDownloadManager, times(deploymentElementsToDownload))
                .download(any(OperationUpdate.DeploymentElement.class));
        verify(mockedInstallManager, times(0)).install(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedBackupManager, times(numOfDeploymentElements))
                .getBackupFile(any(OperationUpdate.DeploymentElement.class));
        verify(mockedInstallManager, times(numOfDeploymentElements))
                .rollback(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedBackupManager).deleteBackupFiles();
        verify(mockedDownloadManager).deleteDownloadedFiles();
        verify(mockedInstallManager).clearInstalledDeploymentElements();
    }

    @Test
    public void testUpdateErrorInstalling() throws InstallManager.InstallException, ExecutionException,
            InterruptedException, BackupManager.BackupException, DownloadManager.DownloadException {
        doNothing().doNothing().doThrow(new InstallManager.InstallException("")).when(mockedInstallManager)
                .install(any(OperationUpdate.DeploymentElement.class), anyString());

        CompletableFuture<OperationUpdate.Result> future =
                operationUpdate.update(TEST_NAME, TEST_VERSION_1, testDeploymentElements);
        OperationUpdate.Result result = future.get();

        assertEquals(OperationResultCodes.ERROR_PROCESSING, result.getResultCode());
        assertStepResult(UpdateStepName.BEGINUPDATE, StepResultCodes.SUCCESSFUL, result);
        assertStepResult(UpdateStepName.DOWNLOADFILE, StepResultCodes.SUCCESSFUL, deploymentElementsToDownload, result);
        assertStepResult(UpdateStepName.BEGININSTALL, StepResultCodes.SUCCESSFUL, numOfDeploymentElements, result);
        assertStepResult(UpdateStepName.ENDINSTALL, StepResultCodes.SUCCESSFUL, 2, result);
        assertStepResult(UpdateStepName.ENDINSTALL, StepResultCodes.ERROR, result);

        verify(mockedBackupManager).createBackupDirectory();
        verify(mockedBackupManager, times(deploymentElementsToBackup))
                .backup(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedDownloadManager, times(deploymentElementsToDownload))
                .download(any(OperationUpdate.DeploymentElement.class));
        verify(mockedInstallManager, times(3)).install(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedBackupManager, times(numOfDeploymentElements))
                .getBackupFile(any(OperationUpdate.DeploymentElement.class));
        verify(mockedInstallManager, times(numOfDeploymentElements))
                .rollback(any(OperationUpdate.DeploymentElement.class), anyString());
        verify(mockedBackupManager).deleteBackupFiles();
        verify(mockedDownloadManager).deleteDownloadedFiles();
        verify(mockedInstallManager).clearInstalledDeploymentElements();
    }

    @Test
    public void testUpdateUnknownException() throws InstallManager.InstallException, ExecutionException,
            InterruptedException {
        doNothing().doNothing().doThrow(new RuntimeException("Unknown")).when(mockedInstallManager)
                .install(any(OperationUpdate.DeploymentElement.class), anyString());

        CompletableFuture<OperationUpdate.Result> future =
                operationUpdate.update(TEST_NAME, TEST_VERSION_1, testDeploymentElements);
        OperationUpdate.Result result = future.get();

        assertEquals(OperationResultCodes.ERROR_PROCESSING, result.getResultCode());
        assertNotNull(result.getSteps());
    }

    @Test
    public void testLoadConfiguration() {
        UpdateConfiguration config = UpdateConfiguration.builder().rulesPath("the/correct/path").build();

        operationUpdate.loadConfiguration(config);

        assertEquals("the/correct/path", Whitebox.getInternalState(operationUpdate, "rulesPath"));
    }
}