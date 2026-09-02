package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.update.DeploymentElementOperation.DeploymentElementOperationException;
import static es.amplia.oda.operation.update.FileManager.FileException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UninstallDeploymentElementOperationTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement uninstallDeploymentElement =
            new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE, "", "", 1L,
                    DeploymentElementOperationType.UNINSTALL, Collections.EMPTY_LIST,
                    0L, "0.0.9", DeploymentElementOption.MANDATORY);
    private static final DeploymentElement uninstallConfDeploymentElement =
            new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.CONFIGURATION, "", "", 1L,
                    DeploymentElementOperationType.UNINSTALL, Collections.EMPTY_LIST,
                    0L, "0.0.9", DeploymentElementOption.MANDATORY);
    private static final String PATH_TO_INSTALL_FOLDER = "path/to/install/folder";

    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private UninstallDeploymentElementOperation testUninstallOperation;
    private UninstallDeploymentElementOperation testUninstallConfOperation;

    @BeforeEach
    public void setUp() {
        testUninstallOperation = new UninstallDeploymentElementOperation(uninstallDeploymentElement, PATH_TO_INSTALL_FOLDER,
                mockedFileManager, mockedOperationConfirmationProcessor);
        testUninstallConfOperation = new UninstallDeploymentElementOperation(uninstallConfDeploymentElement, PATH_TO_INSTALL_FOLDER,
                mockedFileManager, mockedOperationConfirmationProcessor);
    }

    @Test
    public void testExecuteSpecificOperation() throws DeploymentElementOperationException, FileException {
        String installedFile = "path/to/installed/file.jar";

        when(mockedFileManager.find(eq(PATH_TO_INSTALL_FOLDER), eq(TEST_NAME + "-" + TEST_VERSION))).thenReturn(installedFile);

        testUninstallOperation.executeSpecificOperation(mockedFileManager);

        verify(mockedFileManager).delete(eq(installedFile));
    }

    @Test
    public void testExecuteSpecificConfOperation() throws DeploymentElementOperationException, FileException {
        String installedFile = "path/to/installed/file.jar";

        when(mockedFileManager.find(eq(PATH_TO_INSTALL_FOLDER), eq(TEST_NAME))).thenReturn(installedFile);

        testUninstallConfOperation.executeSpecificOperation(mockedFileManager);

        verify(mockedFileManager).delete(eq(installedFile));
    }

    @Test
    public void testExecuteSpecificOperationNoFileFound() throws DeploymentElementOperationException, FileException {
        when(mockedFileManager.find(eq(PATH_TO_INSTALL_FOLDER), eq(TEST_NAME))).thenReturn(null);

        assertThrows(DeploymentElementOperationException.class, () -> testUninstallOperation.executeSpecificOperation(mockedFileManager));
    }

    @Test
    public void testExecuteSpecificOperationDeleteFileException() throws FileException,
            DeploymentElementOperationException {
        String installedFile = "path/to/installed/file.jar";

        when(mockedFileManager.find(eq(PATH_TO_INSTALL_FOLDER), eq(TEST_NAME + "-" + TEST_VERSION))).thenReturn(installedFile);
        doThrow(new FileException("")).when(mockedFileManager).delete(eq(installedFile));

        assertThrows(FileException.class, () -> testUninstallOperation.executeSpecificOperation(mockedFileManager));
    }

    @Test
    public void testRollbackSpecificOperation() throws FileException {
        String backupFile = "path/to/backup.jar";

        testUninstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile);

        verify(mockedFileManager).copy(eq(backupFile), eq(PATH_TO_INSTALL_FOLDER));
    }

    @Test
    public void testRollbackSpecificOperationFileException() throws FileException {
        String backupFile = "path/to/backup.jar";

        doThrow(new FileException("")).when(mockedFileManager).copy(eq(backupFile), eq(PATH_TO_INSTALL_FOLDER));

        assertThrows(FileException.class, () -> testUninstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile));
    }
}