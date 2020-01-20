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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InstallRuleDeploymentElementOperationTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement installDeploymentElement =
        new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE, "", "",
                DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 0L);
    private static final String LOCAL_FILE = "path/to/local/file.jar";
    private static final String PATH_TO_RULES_FILE = "path/to/rules/rule";
    private static final String PATH_TO_RULES = "path/to/rules";
    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private InstallRuleDeploymentElementOperation testRulesInstallOperation;

    @Before
    public void setUp() {
        testRulesInstallOperation = new InstallRuleDeploymentElementOperation(installDeploymentElement, LOCAL_FILE, PATH_TO_RULES_FILE,
                mockedFileManager, mockedOperationConfirmationProcessor, PATH_TO_RULES);
    }

    @Test
    public void testExecuteSpecificOperation() throws FileException {
        testRulesInstallOperation.executeSpecificOperation(mockedFileManager);

        verify(mockedFileManager).copy(eq(LOCAL_FILE), eq(PATH_TO_RULES_FILE));
    }

    @Test(expected = FileException.class)
    public void testExecuteSpecificOperationFileException() throws FileException {
        doThrow(new FileException("")).when(mockedFileManager).copy(eq(LOCAL_FILE), eq(PATH_TO_RULES_FILE));

        testRulesInstallOperation.executeSpecificOperation(mockedFileManager);

        fail("File exception must be thrown");
    }

    @Test
    public void testRollbackSpecificOperation() throws FileException {
        String backupFile = "path/to/backup.jar";
        String installedFile = "path/to/installed.jar";

        Whitebox.setInternalState(testRulesInstallOperation, "installedFile", installedFile);

        testRulesInstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile);

        verify(mockedFileManager).delete(eq(installedFile));
    }

    @Test
    public void testRollbackSpecificOperationNoInstalledFile() throws FileException {
        testRulesInstallOperation.rollbackSpecificOperation(mockedFileManager, null);

        verifyZeroInteractions(mockedFileManager);
    }

    @Test(expected = FileException.class)
    public void testRollbackSpecificOperationFileException() throws FileException {
        String backupFile = "path/to/backup.jar";
        String installedFile = "path/to/installed.jar";

        Whitebox.setInternalState(testRulesInstallOperation, "installedFile", installedFile);

        doThrow(new FileException("")).when(mockedFileManager).delete(eq(installedFile));

        testRulesInstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile);

        fail("File exception must be thrown");
    }
}