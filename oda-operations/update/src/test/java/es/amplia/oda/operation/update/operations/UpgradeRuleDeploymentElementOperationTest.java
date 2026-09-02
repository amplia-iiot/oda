package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.io.File;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.update.DeploymentElementOperation.DeploymentElementOperationException;
import static es.amplia.oda.operation.update.FileManager.FileException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpgradeRuleDeploymentElementOperationTest {

    private static final String TEST_NAME = "testRule";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement UPGRADE_DEPLOYMENT_ELEMENT =
            new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE, "", "", 1L,
                    DeploymentElementOperationType.UPGRADE, Collections.EMPTY_LIST,
                    0L, "0.0.9", DeploymentElementOption.MANDATORY);
    private static final String LOCAL_FILE = "path/to/rules/rule.js";
    private static final String PATH_TO_BACKUP_JAR = "path/to/backup.js";
    private static final String PATH_TO_UPGRADED_JAR = "path/to/upgraded.js";
    private static final String PATH_TO_RULES_FILE = "path/to/rules/rule";
    private static final String PATH_TO_RULES = "path/to/rules";

    private static final String FILE_EXCEPTION_MESSAGE = "File exception must be thrown";
    private static final String UPGRADED_FILE_FIELD_NAME = "upgradedFile";

    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private UpgradeRuleDeploymentElementOperation testUpgradeOperation;

    @BeforeEach
    public void setUp() {
        testUpgradeOperation = new UpgradeRuleDeploymentElementOperation(UPGRADE_DEPLOYMENT_ELEMENT,
                LOCAL_FILE, PATH_TO_RULES_FILE, mockedFileManager, mockedOperationConfirmationProcessor);
    }

    @AfterEach
    public void cleanUp() {
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
    public void testExecuteSpecificOperation() throws DeploymentElementOperationException, FileException {
        String oldVersion = "/path/to/last/version";

        when(mockedFileManager.find(eq(PATH_TO_RULES_FILE), eq(TEST_NAME))).thenReturn(oldVersion);

        testUpgradeOperation.executeSpecificOperation(mockedFileManager);

        verify(mockedFileManager).delete(eq(oldVersion));
        verify(mockedFileManager).copy(eq(LOCAL_FILE), eq(PATH_TO_RULES_FILE));
    }

    @Test
    public void testExecuteOldVersionNotFoundException() throws DeploymentElementOperationException, FileException {
        when(mockedFileManager.find(eq(PATH_TO_RULES_FILE), eq(TEST_NAME))).thenReturn(null);

        assertThrows(DeploymentElementOperationException.class, () -> testUpgradeOperation.executeSpecificOperation(mockedFileManager));
    }

    @Test
    public void testExecuteFileException() throws DeploymentElementOperationException, FileException {
        String oldVersion = "/path/to/last/version";

        when(mockedFileManager.find(eq(PATH_TO_RULES_FILE), eq(TEST_NAME))).thenReturn(oldVersion);
        doThrow(new FileException("")).when(mockedFileManager).copy(eq(LOCAL_FILE), eq(PATH_TO_RULES_FILE));

        assertThrows(FileException.class, () -> testUpgradeOperation.executeSpecificOperation(mockedFileManager));
    }

    @Test
    public void testRollbackSpecificOperation() throws FileException {
        Whitebox.setInternalState(testUpgradeOperation, UPGRADED_FILE_FIELD_NAME, PATH_TO_UPGRADED_JAR);

        testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_JAR);

        verify(mockedFileManager).delete(eq(PATH_TO_UPGRADED_JAR));
        verify(mockedFileManager).copy(eq(PATH_TO_BACKUP_JAR), eq(PATH_TO_RULES_FILE));
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

        doThrow(new FileException("")).when(mockedFileManager).copy(eq(PATH_TO_BACKUP_JAR), eq(PATH_TO_RULES_FILE));

        assertThrows(FileException.class, () -> testUpgradeOperation.rollbackSpecificOperation(mockedFileManager, PATH_TO_BACKUP_JAR));
    }
}