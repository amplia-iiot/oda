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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DeploymentElementOperationBaseTest {

    private static final String TEST_NAME = "test";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement DEPLOYMENT_ELEMENT =
            new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE,
                    "", "", 1L, DeploymentElementOperationType.INSTALL, Collections.EMPTY_LIST,
                    0L, "0.0.9", DeploymentElementOption.MANDATORY);
    private static final String DEPLOYMENT_ELEMENT_OPERATION_EXCEPTION_MESSAGE = "Deployment element operation exception must be thrown";
    private static final String PATH_TO_BACKUP = "path/to/backup";
    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private DeploymentElementOperationBase testOperationBase;
    private DeploymentElementOperationBase spiedTestOperationBase;

    @BeforeEach
    public void setUp() {
        testOperationBase = new DeploymentElementOperationBase(DEPLOYMENT_ELEMENT,
                                                               mockedFileManager,
                                                               mockedOperationConfirmationProcessor)
        {
            @Override
            @SuppressWarnings("RedundantThrows")
            protected void executeSpecificOperation(FileManager fileManager) throws FileException {
                // Test stub
            }


            @Override
            @SuppressWarnings("RedundantThrows")
            protected void rollbackSpecificOperation(FileManager fileManager, String backupFile) throws FileException {
                // Test stub
            }
        };

        spiedTestOperationBase = spy(testOperationBase);
    }

    @Test
    public void testGetName() {
        assertEquals(TEST_NAME, testOperationBase.getName());
    }

    @Test
    public void testGetVersion() {
        assertEquals(TEST_VERSION, testOperationBase.getVersion());
    }

    @Test
    public void testExecute() throws DeploymentElementOperationException, FileException {
        when(mockedOperationConfirmationProcessor.waitForConfirmation(any(DeploymentElement.class))).thenReturn(true);

        spiedTestOperationBase.execute();

        verify(spiedTestOperationBase).executeSpecificOperation(eq(mockedFileManager));
        verify(mockedOperationConfirmationProcessor).waitForConfirmation(eq(DEPLOYMENT_ELEMENT));
    }

    @Test
    public void testExecuteNotConfirmed() throws DeploymentElementOperationException {
        when(mockedOperationConfirmationProcessor.waitForConfirmation(any(DeploymentElement.class))).thenReturn(false);

        assertThrows(DeploymentElementOperationException.class, () -> testOperationBase.execute());
    }

    @Test
    public void testExecuteSpecificOperationException() throws DeploymentElementOperationException, FileException {
        doThrow(new FileException("")).when(spiedTestOperationBase).executeSpecificOperation(eq(mockedFileManager));

        assertThrows(DeploymentElementOperationException.class, () -> spiedTestOperationBase.execute());
    }

    @Test
    public void testRollback() throws DeploymentElementOperationException, FileException {
        when(mockedOperationConfirmationProcessor.waitForRollbackConfirmation(eq(DEPLOYMENT_ELEMENT))).thenReturn(true);

        spiedTestOperationBase.rollback(PATH_TO_BACKUP);

        verify(spiedTestOperationBase).rollbackSpecificOperation(eq(mockedFileManager), eq(PATH_TO_BACKUP));
        verify(mockedOperationConfirmationProcessor).waitForRollbackConfirmation(eq(DEPLOYMENT_ELEMENT));
    }

    @Test
    public void testRollbackException() throws DeploymentElementOperationException {
        when(mockedOperationConfirmationProcessor.waitForConfirmation(eq(DEPLOYMENT_ELEMENT))).thenReturn(false);

        assertThrows(DeploymentElementOperationException.class, () -> testOperationBase.rollback(PATH_TO_BACKUP));
    }

    @Test
    public void testRollbackSpecificOperationException() throws FileException, DeploymentElementOperationException {
        doThrow(new FileException("")).when(spiedTestOperationBase)
                .rollbackSpecificOperation(eq(mockedFileManager), eq(PATH_TO_BACKUP));

        assertThrows(DeploymentElementOperationException.class, () -> spiedTestOperationBase.rollback(PATH_TO_BACKUP));
    }
}