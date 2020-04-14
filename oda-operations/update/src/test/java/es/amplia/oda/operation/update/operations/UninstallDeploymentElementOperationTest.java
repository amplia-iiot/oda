package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.update.DeploymentElementOperation.DeploymentElementOperationException;
import static es.amplia.oda.operation.update.FileManager.FileException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UninstallDeploymentElementOperationTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement uninstallDeploymentElement =
            new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE, "", "", 1L,
                    DeploymentElementOperationType.UNINSTALL, Collections.EMPTY_LIST,
                    0L, "0.0.9", DeploymentElementOption.MANDATORY);
    private static final String PATH_TO_INSTALL_FOLDER = "path/to/install/folder";

    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private UninstallDeploymentElementOperation testUninstallOperation;

    @Before
    public void setUp() {
        testUninstallOperation = new UninstallDeploymentElementOperation(uninstallDeploymentElement, PATH_TO_INSTALL_FOLDER,
                mockedFileManager, mockedOperationConfirmationProcessor);
    }

    @Test
    public void testExecuteSpecificOperation() throws DeploymentElementOperationException, FileException {
        String installedFile = "path/to/installed/file.jar";

        when(mockedFileManager.find(eq(PATH_TO_INSTALL_FOLDER), eq(TEST_NAME))).thenReturn(installedFile);

        testUninstallOperation.executeSpecificOperation(mockedFileManager);

        verify(mockedFileManager).delete(eq(installedFile));
    }

    @Test(expected = DeploymentElementOperationException.class)
    public void testExecuteSpecificOperationNoFileFound() throws DeploymentElementOperationException, FileException {
        when(mockedFileManager.find(eq(PATH_TO_INSTALL_FOLDER), eq(TEST_NAME))).thenReturn(null);

        testUninstallOperation.executeSpecificOperation(mockedFileManager);

        fail("Deployment Element Operation exception must be thrown");
    }

    @Test(expected = FileException.class)
    public void testExecuteSpecificOperationDeleteFileException() throws FileException,
            DeploymentElementOperationException {
        String installedFile = "path/to/installed/file.jar";

        when(mockedFileManager.find(eq(PATH_TO_INSTALL_FOLDER), eq(TEST_NAME))).thenReturn(installedFile);
        doThrow(new FileException("")).when(mockedFileManager).delete(eq(installedFile));

        testUninstallOperation.executeSpecificOperation(mockedFileManager);

        fail("File exception must be thrown");
    }

    @Test
    public void testRollbackSpecificOperation() throws FileException {
        String backupFile = "path/to/backup.jar";

        testUninstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile);

        verify(mockedFileManager).copy(eq(backupFile), eq(PATH_TO_INSTALL_FOLDER));
    }

    @Test(expected = FileException.class)
    public void testRollbackSpecificOperationFileException() throws FileException {
        String backupFile = "path/to/backup.jar";

        doThrow(new FileException("")).when(mockedFileManager).copy(eq(backupFile), eq(PATH_TO_INSTALL_FOLDER));

        testUninstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile);

        fail("File exception must be thrown");
    }
}