package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.util.Collections;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.update.FileManager.FileException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InstallRuleDeploymentElementOperationTest {

    private static final String TEST_NAME = "testBundle";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement installDeploymentElement =
        new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE, "", "", 1L,
                DeploymentElementOperationType.INSTALL, Collections.EMPTY_LIST,
                0L, "0.0.9",DeploymentElementOption.MANDATORY);
    private static final String LOCAL_FILE = "path/to/local/file.jar";
    private static final String PATH_TO_RULES_FILE = "path/to/rules/rule";
    private static final String PATH_TO_RULES = "path/to/rules";
    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private InstallRuleDeploymentElementOperation testRulesInstallOperation;

    @BeforeEach
    public void setUp() {
        testRulesInstallOperation = new InstallRuleDeploymentElementOperation(installDeploymentElement, LOCAL_FILE,
                PATH_TO_RULES_FILE, mockedFileManager, mockedOperationConfirmationProcessor);
    }

    @AfterAll
    public static void cleanUp() {
        StringBuilder path = new StringBuilder(PATH_TO_RULES_FILE);
        do {
            File testFile = new File(PATH_TO_RULES_FILE);
            if (testFile.exists()) {
                testFile.delete();
            }
            String[] dirs = path.toString().split("/");
            int max = dirs.length - 1;
            path = new StringBuilder();
            for (int i = 0; i < max; i++) {
                path.append(dirs[i]).append("/");
            }
        } while (!path.toString().equals(""));
    }

    @Test
    public void testExecuteSpecificOperation() throws FileException {
        File rulesDir = new File(PATH_TO_RULES);
        rulesDir.mkdirs();

        testRulesInstallOperation.executeSpecificOperation(mockedFileManager);

        verify(mockedFileManager).copy(eq(LOCAL_FILE), eq(PATH_TO_RULES_FILE));
        File mainDir = new File(PATH_TO_RULES.split("/")[0]);
        mainDir.delete();
    }

    @Test
    public void testExecuteSpecificOperationFileException() throws FileException {
        doThrow(new FileException("")).when(mockedFileManager).copy(eq(LOCAL_FILE), eq(PATH_TO_RULES_FILE));

        assertThrows(FileException.class, () -> testRulesInstallOperation.executeSpecificOperation(mockedFileManager));
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

        verifyNoInteractions(mockedFileManager);
    }

    @Test
    public void testRollbackSpecificOperationFileException() throws FileException {
        String backupFile = "path/to/backup.jar";
        String installedFile = "path/to/installed.jar";

        Whitebox.setInternalState(testRulesInstallOperation, "installedFile", installedFile);

        doThrow(new FileException("")).when(mockedFileManager).delete(eq(installedFile));

        assertThrows(FileException.class, () -> testRulesInstallOperation.rollbackSpecificOperation(mockedFileManager, backupFile));
    }
}