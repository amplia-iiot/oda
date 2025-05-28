package es.amplia.oda.operation.update;

import es.amplia.oda.core.commons.utils.operation.response.Operation;
import es.amplia.oda.core.commons.utils.operation.response.OperationResponse;
import es.amplia.oda.core.commons.utils.operation.response.OperationResultCode;
import es.amplia.oda.core.commons.utils.operation.response.Response;
import es.amplia.oda.core.commons.utils.operation.response.Step;
import es.amplia.oda.core.commons.utils.operation.response.StepResultCode;
import es.amplia.oda.event.api.ResponseDispatcher;
import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.api.OperationUpdate.OperationResultCodes;
import es.amplia.oda.operation.api.OperationUpdate.UpdateStepName;
import es.amplia.oda.operation.update.configuration.UpdateConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OperationUpdateImplTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION_1 = "1.0.0";
    private static final String OPERATION_ID = "operationId";
    @Mock
    private BackupManager mockedBackupManager;
    @Mock
    private DownloadManager mockedDownloadManager;
    @Mock
    private InstallManager mockedInstallManager;
    @Mock
    private ResponseDispatcher mockedDispatcher;
    @InjectMocks
    private OperationUpdateImpl operationUpdate;

    private final OperationUpdate.DeploymentElement installDeploymentElement =
            new OperationUpdate.DeploymentElement("testDeploymentElement",
                    TEST_VERSION_1,
                    OperationUpdate.DeploymentElementType.SOFTWARE,
                    "testUrl1",
                    "testPath1",
                    1L,
                    OperationUpdate.DeploymentElementOperationType.INSTALL,
                    Collections.EMPTY_LIST,
                    0L,
                    "0.0.9",
                    OperationUpdate.DeploymentElementOption.MANDATORY);
    private final OperationUpdate.DeploymentElement upgradeDeploymentElement =
            new OperationUpdate.DeploymentElement("testDeploymentElement",
                    "2.0.0",
                    OperationUpdate.DeploymentElementType.CONFIGURATION,
                    "testUrl2",
                    "testPath2",
                    1L,
                    OperationUpdate.DeploymentElementOperationType.UPGRADE,
                    Collections.EMPTY_LIST,
                    0L,
                    "0.0.9",
                    OperationUpdate.DeploymentElementOption.OPTIONAL);
    private final OperationUpdate.DeploymentElement uninstallDeploymentElement =
            new OperationUpdate.DeploymentElement("testDeploymentElement3",
                    "3.3.3",
                    OperationUpdate.DeploymentElementType.FIRMWARE,
                    "testUrl3",
                    "testPath3",
                    1L,
                    OperationUpdate.DeploymentElementOperationType.UNINSTALL,
                    Collections.EMPTY_LIST,
                    0L,
                    "0.0.9",
                    OperationUpdate.DeploymentElementOption.OPTIONAL);
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
        operationUpdate.update(OPERATION_ID, TEST_NAME, TEST_VERSION_1, testDeploymentElements);
        Thread.sleep(500);

        List<Step> steps = new ArrayList<>();
        steps.add(new Step(UpdateStepName.BEGINUPDATE.toString(), StepResultCode.SUCCESSFUL, "System prepared for update", null, null));
        steps.add(new Step(UpdateStepName.DOWNLOADFILE.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-1.0.0 downloaded", null, null));
        steps.add(new Step(UpdateStepName.DOWNLOADFILE.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-2.0.0 downloaded", null, null));
        steps.add(new Step(UpdateStepName.BEGININSTALL.toString(), StepResultCode.SUCCESSFUL, "Begin installing testDeploymentElement-1.0.0", null, null));
        steps.add(new Step(UpdateStepName.ENDINSTALL.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-1.0.0 installed", null, null));
        steps.add(new Step(UpdateStepName.BEGININSTALL.toString(), StepResultCode.SUCCESSFUL, "Begin upgrading testDeploymentElement-2.0.0", null, null));
        steps.add(new Step(UpdateStepName.ENDINSTALL.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-2.0.0 upgraded", null, null));
        steps.add(new Step(UpdateStepName.BEGININSTALL.toString(), StepResultCode.SUCCESSFUL, "Begin uninstalling testDeploymentElement3-3.3.3", null, null));
        steps.add(new Step(UpdateStepName.ENDINSTALL.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement3-3.3.3 uninstalled", null, null));
        steps.add(new Step(UpdateStepName.ENDUPDATE.toString(), StepResultCode.SUCCESSFUL, "", null, null));
        OperationResponse resp = new OperationResponse("9.0", new Operation
                        (new Response(OPERATION_ID, null, null, "UPDATE", OperationResultCode.SUCCESSFUL, "", steps)));

        verify(mockedDispatcher).publishResponse(resp);

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
        verify(mockedInstallManager).clearInstalledDeploymentElements(OperationResultCodes.SUCCESSFUL);
    }

    @Test
    public void testUpdateErrorPreparingSystem() throws BackupManager.BackupException, ExecutionException,
            InterruptedException, DownloadManager.DownloadException, InstallManager.InstallException {
        doThrow(new BackupManager.BackupException("")).when(mockedBackupManager)
                .backup(any(OperationUpdate.DeploymentElement.class), anyString());

        operationUpdate.update(OPERATION_ID, TEST_NAME, TEST_VERSION_1, testDeploymentElements);
        Thread.sleep(500);

        List<Step> steps = new ArrayList<>();
        steps.add(new Step(UpdateStepName.BEGINUPDATE.toString(), StepResultCode.ERROR, "Error preparing system for update", null, null));
        OperationResponse resp = new OperationResponse("9.0", new Operation
                        (new Response(OPERATION_ID, null, null, "UPDATE", OperationResultCode.ERROR_PROCESSING, "Can not prepare system for operation update: ", steps)));

        verify(mockedDispatcher).publishResponse(resp);

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
        verify(mockedInstallManager).clearInstalledDeploymentElements(OperationResultCodes.ERROR_PROCESSING);
    }

    @Test
    public void testUpdateErrorDownloading() throws DownloadManager.DownloadException, ExecutionException,
            InterruptedException, BackupManager.BackupException, InstallManager.InstallException {
        doNothing().doThrow(new DownloadManager.DownloadException("")).when(mockedDownloadManager)
                .download(any(OperationUpdate.DeploymentElement.class));

        operationUpdate.update(OPERATION_ID, TEST_NAME, TEST_VERSION_1, testDeploymentElements);
        Thread.sleep(500);

        List<Step> steps = new ArrayList<>();
        steps.add(new Step(UpdateStepName.BEGINUPDATE.toString(), StepResultCode.SUCCESSFUL, "System prepared for update", null, null));
        steps.add(new Step(UpdateStepName.DOWNLOADFILE.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-1.0.0 downloaded", null, null));
        steps.add(new Step(UpdateStepName.DOWNLOADFILE.toString(), StepResultCode.ERROR, "Error downloading testDeploymentElement-2.0.0: ", null, null));
        OperationResponse resp = new OperationResponse("9.0", new Operation
                        (new Response(OPERATION_ID, null, null, "UPDATE", OperationResultCode.ERROR_PROCESSING, "Error downloading testDeploymentElement-2.0.0", steps)));

        verify(mockedDispatcher).publishResponse(resp);

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
        verify(mockedInstallManager).clearInstalledDeploymentElements(OperationResultCodes.ERROR_PROCESSING);
    }

    @Test
    public void testUpdateErrorInstalling() throws InstallManager.InstallException, ExecutionException,
            InterruptedException, BackupManager.BackupException, DownloadManager.DownloadException {
        doNothing().doNothing().doThrow(new InstallManager.InstallException("")).when(mockedInstallManager)
                .install(any(OperationUpdate.DeploymentElement.class), anyString());

        operationUpdate.update(OPERATION_ID, TEST_NAME, TEST_VERSION_1, testDeploymentElements);
        Thread.sleep(500);

        List<Step> steps = new ArrayList<>();
        steps.add(new Step(UpdateStepName.BEGINUPDATE.toString(), StepResultCode.SUCCESSFUL, "System prepared for update", null, null));
        steps.add(new Step(UpdateStepName.DOWNLOADFILE.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-1.0.0 downloaded", null, null));
        steps.add(new Step(UpdateStepName.DOWNLOADFILE.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-2.0.0 downloaded", null, null));
        steps.add(new Step(UpdateStepName.BEGININSTALL.toString(), StepResultCode.SUCCESSFUL, "Begin installing testDeploymentElement-1.0.0", null, null));
        steps.add(new Step(UpdateStepName.ENDINSTALL.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-1.0.0 installed", null, null));
        steps.add(new Step(UpdateStepName.BEGININSTALL.toString(), StepResultCode.SUCCESSFUL, "Begin upgrading testDeploymentElement-2.0.0", null, null));
        steps.add(new Step(UpdateStepName.ENDINSTALL.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-2.0.0 upgraded", null, null));
        steps.add(new Step(UpdateStepName.BEGININSTALL.toString(), StepResultCode.SUCCESSFUL, "Begin uninstalling testDeploymentElement3-3.3.3", null, null));
        steps.add(new Step(UpdateStepName.ENDINSTALL.toString(), StepResultCode.ERROR, "Error uninstalling testDeploymentElement3-3.3.3: ", null, null));
        OperationResponse resp = new OperationResponse("9.0", new Operation
                        (new Response(OPERATION_ID, null, null, "UPDATE", OperationResultCode.ERROR_PROCESSING, "Error uninstalling testDeploymentElement3-3.3.3", steps)));

        verify(mockedDispatcher).publishResponse(resp);

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
        verify(mockedInstallManager).clearInstalledDeploymentElements(OperationResultCodes.ERROR_PROCESSING);
    }

    @Test
    public void testUpdateUnknownException() throws InstallManager.InstallException, ExecutionException,
            InterruptedException {
        doNothing().doNothing().doThrow(new RuntimeException("Unknown")).when(mockedInstallManager)
                .install(any(OperationUpdate.DeploymentElement.class), anyString());

        operationUpdate.update(OPERATION_ID, TEST_NAME, TEST_VERSION_1, testDeploymentElements);
        Thread.sleep(500);

        List<Step> steps = new ArrayList<>();
        steps.add(new Step(UpdateStepName.BEGINUPDATE.toString(), StepResultCode.SUCCESSFUL, "System prepared for update", null, null));
        steps.add(new Step(UpdateStepName.DOWNLOADFILE.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-1.0.0 downloaded", null, null));
        steps.add(new Step(UpdateStepName.DOWNLOADFILE.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-2.0.0 downloaded", null, null));
        steps.add(new Step(UpdateStepName.BEGININSTALL.toString(), StepResultCode.SUCCESSFUL, "Begin installing testDeploymentElement-1.0.0", null, null));
        steps.add(new Step(UpdateStepName.ENDINSTALL.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-1.0.0 installed", null, null));
        steps.add(new Step(UpdateStepName.BEGININSTALL.toString(), StepResultCode.SUCCESSFUL, "Begin upgrading testDeploymentElement-2.0.0", null, null));
        steps.add(new Step(UpdateStepName.ENDINSTALL.toString(), StepResultCode.SUCCESSFUL, "testDeploymentElement-2.0.0 upgraded", null, null));
        steps.add(new Step(UpdateStepName.BEGININSTALL.toString(), StepResultCode.SUCCESSFUL, "Begin uninstalling testDeploymentElement3-3.3.3", null, null));
        OperationResponse resp = new OperationResponse("9.0", new Operation
                        (new Response(OPERATION_ID, null, null, "UPDATE", OperationResultCode.ERROR_PROCESSING, "Unknown exception making update operation", steps)));

        verify(mockedDispatcher).publishResponse(resp);
    }

    @Test
    public void testLoadConfiguration() {
        UpdateConfiguration config = UpdateConfiguration.builder().rulesPath("the/correct/path").backupPath("backup/path").build();

        operationUpdate.loadConfiguration(config);

        assertEquals("the/correct/path", Whitebox.getInternalState(operationUpdate, "rulesPath"));
    }
}