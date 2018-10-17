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
import static es.amplia.oda.operation.update.DeploymentElementOperation.DeploymentElementOperationException;
import static es.amplia.oda.operation.update.FileManager.FileException;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeDeploymentElementOperationTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement UPGRADE_DEPLOYMENT_ELEMENT =
            new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE, "", "",
                    DeploymentElementOperationType.UPGRADE, DeploymentElementOption.MANDATORY, 0L);
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

    private UpgradeDeploymentElementOperation testUpgradeOperation;

    @Before
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

    @Test(expected = DeploymentElementOperationException.class)
    public void testExecuteOldVersionNotFoundException() throws DeploymentElementOperationException, FileException {
        when(mockedFileManager.find(eq(INSTALL_FOLDER), eq(TEST_NAME))).thenReturn(null);

        testUpgradeOperation.executeSpecificOperation(mockedFileManager);

        fail("Deployment Element Operation exception must be thrown");
    }

    @Test(expected = FileException.class)
    public void testExecuteFileException() throws DeploymentElementOperationException, FileException {
        String oldVersion = "/path/to/last/version";

        when(mockedFileManager.find(eq(INSTALL_FOLDER), eq(TEST_NAME))).thenReturn(oldVersion);
        doThrow(new FileException("")).when(mockedFileManager).copy(eq(LOCAL_FILE), eq(INSTALL_FOLDER));

        testUpgradeOperation.executeSpecificOperation(mockedFileManager);

        fail(FILE_EXCEPTION_MESSAGE);
    }

    @Test
    public void testRollbackSpecificOperation() throws FileException {
        Whitebox.setInternalState(testUpgradeOperation, UPGRADED_FILE_FIELD_NAME, PATH_TO_UPGRADED_JAR);

        testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_JAR);

        verify(mockedFileManager).delete(eq(PATH_TO_UPGRADED_JAR));
        verify(mockedFileManager).copy(eq(PATH_TO_BACKUP_JAR), eq(INSTALL_FOLDER));
    }

    @Test(expected = FileException.class)
    public void testRollbackSpecificOperationDeleteFileException() throws FileException {
        Whitebox.setInternalState(testUpgradeOperation, UPGRADED_FILE_FIELD_NAME, PATH_TO_UPGRADED_JAR);

        doThrow(new FileException("")).when(mockedFileManager).delete(eq(PATH_TO_UPGRADED_JAR));

        testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_JAR);

        fail(FILE_EXCEPTION_MESSAGE);
    }

    @Test(expected = FileException.class)
    public void testRollbackSpecificOperationCopyFileException() throws FileException {
        Whitebox.setInternalState(testUpgradeOperation, UPGRADED_FILE_FIELD_NAME, PATH_TO_UPGRADED_JAR);

        doThrow(new FileException("")).when(mockedFileManager).copy(eq(PATH_TO_BACKUP_JAR), eq(INSTALL_FOLDER));

        testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_JAR);

        fail(FILE_EXCEPTION_MESSAGE);
    }
}