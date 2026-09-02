package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.update.FileManager.FileException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InstallDeploymentElementOperationTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement installDeploymentElement =
        new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE, "", "", 1L,
                DeploymentElementOperationType.INSTALL, Collections.EMPTY_LIST,
                0L, "0.0.9", DeploymentElementOption.MANDATORY);
    private static final String LOCAL_FILE = "path/to/local/file.jar";
    private static final String PATH_TO_INSTALL_FOLDER = "path/to/install/folder";
    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private InstallDeploymentElementOperation testInstallOperation;

    @BeforeEach
    public void setUp() {
        testInstallOperation = new InstallDeploymentElementOperation(installDeploymentElement, LOCAL_FILE, PATH_TO_INSTALL_FOLDER,
                mockedFileManager, mockedOperationConfirmationProcessor);
    }

    @Test
    public void testExecuteSpecificOperation() throws FileException {
        testInstallOperation.executeSpecificOperation(mockedFileManager);

        verify(mockedFileManager).copy(eq(LOCAL_FILE), eq(PATH_TO_INSTALL_FOLDER));
    }

    @Test
    public void testExecuteSpecificOperationFileException() throws FileException {
        doThrow(new FileException("")).when(mockedFileManager).copy(eq(LOCAL_FILE), eq(PATH_TO_INSTALL_FOLDER));

        assertThrows(FileException.class, () -> testInstallOperation.executeSpecificOperation(mockedFileManager));
    }

    @Test
    public void testRollbackSpecificOperation() throws FileException {
        String backupFile = "path/to/backup.jar";
        String installedFile = "path/to/installed.jar";

        Whitebox.setInternalState(testInstallOperation, "installedFile", installedFile);

        testInstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile);

        verify(mockedFileManager).delete(eq(installedFile));
    }

    @Test
    public void testRollbackSpecificOperationNoInstalledFile() throws FileException {
        testInstallOperation.rollbackSpecificOperation(mockedFileManager, null);

        verifyNoInteractions(mockedFileManager);
    }

    @Test
    public void testRollbackSpecificOperationFileException() throws FileException {
        String backupFile = "path/to/backup.jar";
        String installedFile = "path/to/installed.jar";

        Whitebox.setInternalState(testInstallOperation, "installedFile", installedFile);

        doThrow(new FileException("")).when(mockedFileManager).delete(eq(installedFile));

        assertThrows(FileException.class, () -> testInstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile));
    }
}