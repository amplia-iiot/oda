package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.update.DeploymentElementOperation.DeploymentElementOperationException;
import static es.amplia.oda.operation.update.FileManager.FileException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentElementOperationBaseTest {

    private static final String TEST_NAME = "test";
    private static final String TEST_VERSION = "1.0.0";
    private static final DeploymentElement DEPLOYMENT_ELEMENT =
            new DeploymentElement(TEST_NAME, TEST_VERSION, DeploymentElementType.SOFTWARE,
                    "", "", DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 1L);
    private static final String DEPLOYMENT_ELEMENT_OPERATION_EXCEPTION_MESSAGE = "Deployment element operation exception must be thrown";
    private static final String PATH_TO_BACKUP = "path/to/backup";
    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;

    private DeploymentElementOperationBase testOperationBase;
    private DeploymentElementOperationBase spiedTestOperationBase;

    @Before
    public void setUp() {
        testOperationBase = new DeploymentElementOperationBase(DEPLOYMENT_ELEMENT,
                                                               mockedFileManager,
                                                               mockedOperationConfirmationProcessor)
        {
            @Override
            protected void executeSpecificOperation(FileManager fileManager) throws FileException {
                // Test stub
            }

            @Override
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

    @Test(expected = DeploymentElementOperationException.class)
    public void testExecuteNotConfirmed() throws DeploymentElementOperationException {
        when(mockedOperationConfirmationProcessor.waitForConfirmation(any(DeploymentElement.class))).thenReturn(false);

        testOperationBase.execute();

        fail(DEPLOYMENT_ELEMENT_OPERATION_EXCEPTION_MESSAGE);
    }

    @Test(expected = DeploymentElementOperationException.class)
    public void testExecuteSpecificOperationException() throws DeploymentElementOperationException, FileException {
        doThrow(new FileException("")).when(spiedTestOperationBase).executeSpecificOperation(eq(mockedFileManager));

        spiedTestOperationBase.execute();

        fail(DEPLOYMENT_ELEMENT_OPERATION_EXCEPTION_MESSAGE);
    }

    @Test
    public void testRollback() throws DeploymentElementOperationException, FileException {
        when(mockedOperationConfirmationProcessor.waitForRollbackConfirmation(eq(DEPLOYMENT_ELEMENT))).thenReturn(true);

        spiedTestOperationBase.rollback(PATH_TO_BACKUP);

        verify(spiedTestOperationBase).rollbackSpecificOperation(eq(mockedFileManager), eq(PATH_TO_BACKUP));
        verify(mockedOperationConfirmationProcessor).waitForRollbackConfirmation(eq(DEPLOYMENT_ELEMENT));
    }

    @Test(expected = DeploymentElementOperationException.class)
    public void testRollbackException() throws DeploymentElementOperationException {
        when(mockedOperationConfirmationProcessor.waitForConfirmation(eq(DEPLOYMENT_ELEMENT))).thenReturn(false);

        testOperationBase.rollback(PATH_TO_BACKUP);

        fail(DEPLOYMENT_ELEMENT_OPERATION_EXCEPTION_MESSAGE);
    }

    @Test(expected = DeploymentElementOperationException.class)
    public void testRollbackSpecificOperationException() throws FileException, DeploymentElementOperationException {
        doThrow(new FileException("")).when(spiedTestOperationBase)
                .rollbackSpecificOperation(eq(mockedFileManager), eq(PATH_TO_BACKUP));

        spiedTestOperationBase.rollback(PATH_TO_BACKUP);

        fail(DEPLOYMENT_ELEMENT_OPERATION_EXCEPTION_MESSAGE);
    }
}