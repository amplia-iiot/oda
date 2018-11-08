package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.update.FileManager.FileException;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class InstallDeploymentElementOperationTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement installDeploymentElement =
        new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE, "", "",
                DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 0L);
    private static final String LOCAL_FILE = "path/to/local/file.jar";
    private static final String PATH_TO_INSTALL_FOLDER = "path/to/install/folder";
    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private InstallDeploymentElementOperation testInstallOperation;

    @Before
    public void setUp() {
        testInstallOperation = new InstallDeploymentElementOperation(installDeploymentElement, LOCAL_FILE, PATH_TO_INSTALL_FOLDER,
                mockedFileManager, mockedOperationConfirmationProcessor);
    }

    @Test
    public void testExecuteSpecificOperation() throws FileException {
        testInstallOperation.executeSpecificOperation(mockedFileManager);

        verify(mockedFileManager).copy(eq(LOCAL_FILE), eq(PATH_TO_INSTALL_FOLDER));
    }

    @Test(expected = FileException.class)
    public void testExecuteSpecificOperationFileException() throws FileException {
        doThrow(new FileException("")).when(mockedFileManager).copy(eq(LOCAL_FILE), eq(PATH_TO_INSTALL_FOLDER));

        testInstallOperation.executeSpecificOperation(mockedFileManager);

        fail("File exception must be thrown");
    }

    @Test
    public void testRollbackSpecificOperation() throws FileException {
        String backupFile = "path/to/backup.jar";
        String installedFile = "path/to/installed.jar";

        Whitebox.setInternalState(testInstallOperation, "installedFile", installedFile);

        testInstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile);

        verify(mockedFileManager).delete(eq(installedFile));
    }

    @Test(expected = FileException.class)
    public void testRollbackSpecificOperationFileException() throws FileException {
        String backupFile = "path/to/backup.jar";
        String installedFile = "path/to/installed.jar";

        Whitebox.setInternalState(testInstallOperation, "installedFile", installedFile);

        doThrow(new FileException("")).when(mockedFileManager).delete(eq(installedFile));

        testInstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile);

        fail("File exception must be thrown");
    }
}