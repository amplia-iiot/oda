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
public class UpgradeSoftwareDeploymentElementOperationTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION = "1.0.0";
    private static final String TEST_OLD_VERSION = "0.0.9";
    private static final DeploymentElement UPGRADE_DEPLOYMENT_ELEMENT =
            new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE, "", "", 1L,
                    DeploymentElementOperationType.UPGRADE, Collections.EMPTY_LIST,
                    0L, TEST_OLD_VERSION, DeploymentElementOption.MANDATORY);
    private static final String LOCAL_FILE = "path/to/local/file.jar";
    private static final String INSTALL_FOLDER = "path/to/install/folder";
    private static final String PATH_TO_BACKUP_JAR = "path/to/backup.jar";
    private static final String PATH_TO_UPGRADED_JAR = "path/to/upgraded.jar";

    private static final String FILE_EXCEPTION_MESSAGE = "File exception must be thrown";
    private static final String UPGRADED_FILE_FIELD_NAME = "upgradedFile";

    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private UpgradeSoftwareDeploymentElementOperation testUpgradeOperation;

    @BeforeEach
    public void setUp() {
        testUpgradeOperation = new UpgradeSoftwareDeploymentElementOperation(UPGRADE_DEPLOYMENT_ELEMENT, LOCAL_FILE, INSTALL_FOLDER,
                mockedFileManager, mockedOperationConfirmationProcessor);
    }

    @Test
    public void testExecuteSpecificOperation() throws DeploymentElementOperationException, FileException {
        String oldVersion = "/path/to/last/version";

        when(mockedFileManager.find(eq(INSTALL_FOLDER), eq(TEST_NAME + "-" + TEST_OLD_VERSION))).thenReturn(oldVersion);

        testUpgradeOperation.executeSpecificOperation(mockedFileManager);

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

        when(mockedFileManager.find(eq(INSTALL_FOLDER), eq(TEST_NAME + "-" + TEST_OLD_VERSION))).thenReturn(oldVersion);
        doThrow(new FileException("")).when(mockedFileManager).copy(eq(LOCAL_FILE), eq(INSTALL_FOLDER));

        assertThrows(FileException.class, () -> testUpgradeOperation.executeSpecificOperation(mockedFileManager));
    }

    @Test
    public void testRollbackSpecificOperation() throws FileException {
        Whitebox.setInternalState(testUpgradeOperation, UPGRADED_FILE_FIELD_NAME, PATH_TO_UPGRADED_JAR);

        testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_JAR);

        verify(mockedFileManager).delete(eq(PATH_TO_UPGRADED_JAR));
        verify(mockedFileManager).copy(eq(PATH_TO_BACKUP_JAR), eq(INSTALL_FOLDER));
    }

    @Test
    public void testRollbackSpecificOperationDeleteFileException() throws FileException {
        Whitebox.setInternalState(testUpgradeOperation, UPGRADED_FILE_FIELD_NAME, PATH_TO_UPGRADED_JAR);

        doThrow(new FileException("")).when(mockedFileManager).delete(eq(PATH_TO_UPGRADED_JAR));

        assertThrows(FileException.class, () -> testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_JAR));
    }

    @Test
    public void testRollbackSpecificOperationCopyFileException() throws FileException {
        Whitebox.setInternalState(testUpgradeOperation, UPGRADED_FILE_FIELD_NAME, PATH_TO_UPGRADED_JAR);

        doThrow(new FileException("")).when(mockedFileManager).copy(eq(PATH_TO_BACKUP_JAR), eq(INSTALL_FOLDER));

        assertThrows(FileException.class, () -> testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_JAR));
    }
}