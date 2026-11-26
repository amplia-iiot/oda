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
import static es.amplia.oda.operation.update.DeploymentElementOperation.DeploymentElementOperationException;
import static es.amplia.oda.operation.update.FileManager.FileException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpgradeDeploymentElementOperationTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement UPGRADE_DEPLOYMENT_ELEMENT =
            new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.CONFIGURATION, "", "", 1L,
                    DeploymentElementOperationType.UPGRADE, Collections.EMPTY_LIST,
                    0L, "0.0.9", DeploymentElementOption.MANDATORY);
    private static final String LOCAL_FILE = "path/to/local/file.cfg";
    private static final String INSTALL_FOLDER = "path/to/install/folder";
    private static final String PATH_TO_BACKUP_CFG = "path/to/backup.cfg";
    private static final String PATH_TO_UPGRADED_CFG = "path/to/upgraded.cfg";

    private static final String FILE_EXCEPTION_MESSAGE = "File exception must be thrown";
    private static final String UPGRADED_FILE_FIELD_NAME = "upgradedFile";

    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private UpgradeDeploymentElementOperation testUpgradeOperation;

    @BeforeEach
    public void setUp() {
        testUpgradeOperation = new UpgradeDeploymentElementOperation(UPGRADE_DEPLOYMENT_ELEMENT, LOCAL_FILE, INSTALL_FOLDER,
                mockedFileManager, mockedOperationConfirmationProcessor);
    }

    @Test
    public void testExecuteSpecificOperation() throws DeploymentElementOperationException, FileException {
        String oldVersion = "/path/to/last/version";

        when(mockedFileManager.find(eq(INSTALL_FOLDER), eq(TEST_NAME))).thenReturn(oldVersion);

        testUpgradeOperation.executeSpecificOperation(mockedFileManager);

        verify(mockedFileManager).delete(eq(oldVersion));
        verify(mockedFileManager).copy(eq(LOCAL_FILE), eq(INSTALL_FOLDER));
    }

    @Test
    public void testExecuteOldVersionNotFoundException() throws DeploymentElementOperationException, FileException {
        when(mockedFileManager.find(eq(INSTALL_FOLDER), eq(TEST_NAME))).thenReturn(null);

        assertThrows(DeploymentElementOperationException.class, () -> testUpgradeOperation.executeSpecificOperation(mockedFileManager));
    }

    @Test
    public void testExecuteFileException() throws DeploymentElementOperationException, FileException {
        String oldVersion = "/path/to/last/version";

        when(mockedFileManager.find(eq(INSTALL_FOLDER), eq(TEST_NAME))).thenReturn(oldVersion);
        doThrow(new FileException("")).when(mockedFileManager).copy(eq(LOCAL_FILE), eq(INSTALL_FOLDER));

        assertThrows(FileException.class, () -> testUpgradeOperation.executeSpecificOperation(mockedFileManager));
    }

    @Test
    public void testRollbackSpecificOperation() throws FileException {
        Whitebox.setInternalState(testUpgradeOperation, UPGRADED_FILE_FIELD_NAME, PATH_TO_UPGRADED_CFG);

        testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_CFG);

        verify(mockedFileManager).delete(eq(PATH_TO_UPGRADED_CFG));
        verify(mockedFileManager).copy(eq(PATH_TO_BACKUP_CFG), eq(INSTALL_FOLDER));
    }

    @Test
    public void testRollbackSpecificOperationDeleteFileException() throws FileException {
        Whitebox.setInternalState(testUpgradeOperation, UPGRADED_FILE_FIELD_NAME, PATH_TO_UPGRADED_CFG);

        doThrow(new FileException("")).when(mockedFileManager).delete(eq(PATH_TO_UPGRADED_CFG));

        assertThrows(FileException.class, () -> testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_CFG));
    }

    @Test
    public void testRollbackSpecificOperationCopyFileException() throws FileException {
        Whitebox.setInternalState(testUpgradeOperation, UPGRADED_FILE_FIELD_NAME, PATH_TO_UPGRADED_CFG);

        doThrow(new FileException("")).when(mockedFileManager).copy(eq(PATH_TO_BACKUP_CFG), eq(INSTALL_FOLDER));

        assertThrows(FileException.class, () -> testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_CFG));
    }
}