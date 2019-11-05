package es.amplia.oda.hardware.comms;

import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.*;

import static es.amplia.oda.hardware.comms.CommsManagerImpl.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommsManagerImplTest {

    private static final String TEST_PIN = "1111";
    private static final String TEST_APN = "testApn";
    private static final String TEST_USERNAME = "testUsername";
    private static final String TEST_PASS = "testPassword";
    private static final int TEST_CONNECTION_TIMEOUT = 5;
    private static final long TEST_RETRY_CONNECTION_TIMER = 5;
    private static final String TEST_PATH = "test/path";


    @Mock
    private CommandProcessor mockedCommandProcessor;
    @Mock
    private ResourceManager mockedResourceManager;
    @Mock
    private ScheduledExecutorService mockedExecutor;
    @InjectMocks
    private CommsManagerImpl testCommsManager;

    @Mock
    private Thread mockedThread;
    @Mock
    private ScheduledFuture<?> mockedScheduledConnection;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;


    @Test
    public void testConnect() throws CommandExecutionException, InterruptedException, ExecutionException {
        when(mockedResourceManager.getResourcePath(anyString())).thenReturn(TEST_PATH);

        testCommsManager.connect(TEST_PIN, TEST_APN, TEST_USERNAME, TEST_PASS, TEST_CONNECTION_TIMEOUT,
                TEST_RETRY_CONNECTION_TIMER);
        waitToConnect();

        verify(mockedResourceManager).getResourcePath(eq(INIT_SIM_SCRIPT));
        verify(mockedResourceManager).getResourcePath(eq(CONFIGURE_CONNECTION_SCRIPT));
        verify(mockedResourceManager).getResourcePath(eq(CONNECT_SCRIPT));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + " " + TEST_PIN), eq(COMMAND_TIMEOUT_MS));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + " " + TEST_APN + " " + TEST_USERNAME + " " + TEST_PASS),
                eq(COMMAND_TIMEOUT_MS));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + " " + TEST_CONNECTION_TIMEOUT),
                eq(COMMAND_TIMEOUT_MS + TEST_CONNECTION_TIMEOUT * 1000));
    }

    @Test
    public void testConnectCommandExecutionExceptionIsCaught() throws CommandExecutionException {
        when(mockedResourceManager.getResourcePath(anyString())).thenReturn(TEST_PATH);
        when(mockedCommandProcessor.execute(anyString(), anyLong()))
                .thenThrow(new CommandExecutionException("", "", new RuntimeException()));

        testCommsManager.connect(TEST_PIN, TEST_APN, TEST_USERNAME, TEST_PASS, TEST_CONNECTION_TIMEOUT,
                TEST_RETRY_CONNECTION_TIMER);

        verify(mockedResourceManager).getResourcePath(eq(INIT_SIM_SCRIPT));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + " " + TEST_PIN), eq(COMMAND_TIMEOUT_MS));
        assertTrue("CommandExecutionException should be caught", true);
    }

    @Test
    public void testConnectCommandExecutionConnectingExceptionIsCaught() throws CommandExecutionException, InterruptedException, ExecutionException {
        when(mockedResourceManager.getResourcePath(anyString())).thenReturn(TEST_PATH);
        when(mockedCommandProcessor.execute(anyString(), anyLong())).thenReturn("").thenReturn("")
                .thenThrow(new CommandExecutionException("", "", new RuntimeException())).thenReturn("");

        testCommsManager.connect(TEST_PIN, TEST_APN, TEST_USERNAME, TEST_PASS, TEST_CONNECTION_TIMEOUT, TEST_RETRY_CONNECTION_TIMER);
        waitToConnect();

        verify(mockedResourceManager).getResourcePath(eq(INIT_SIM_SCRIPT));
        verify(mockedResourceManager).getResourcePath(eq(CONFIGURE_CONNECTION_SCRIPT));
        verify(mockedResourceManager).getResourcePath(eq(CONNECT_SCRIPT));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + " " + TEST_PIN), eq(COMMAND_TIMEOUT_MS));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + " " + TEST_APN + " " + TEST_USERNAME + " " + TEST_PASS),
                eq(COMMAND_TIMEOUT_MS));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + " " + TEST_CONNECTION_TIMEOUT),
                eq(COMMAND_TIMEOUT_MS + TEST_CONNECTION_TIMEOUT * 1000));
        verify(mockedExecutor).schedule(runnableCaptor.capture(), eq(TEST_RETRY_CONNECTION_TIMER), eq(TimeUnit.SECONDS));
        Runnable runnableCaptured = runnableCaptor.getValue();
        runnableCaptured.run();
        verify(mockedResourceManager).getResourcePath(eq(CONNECT_SCRIPT));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + " " + TEST_CONNECTION_TIMEOUT),
                eq(COMMAND_TIMEOUT_MS + TEST_CONNECTION_TIMEOUT * 1000));
        assertTrue("CommandExecutionException should be caught", true);
    }

    private void waitToConnect() throws InterruptedException, ExecutionException {
        Executors.newScheduledThreadPool(1).schedule(() -> {}, 1, TimeUnit.SECONDS).get();
    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testCommsManager, "connectionThread", mockedThread);
        Whitebox.setInternalState(testCommsManager, "scheduledConnection", mockedScheduledConnection);

        testCommsManager.close();

        verify(mockedScheduledConnection).cancel(eq(false));
        verify(mockedThread).interrupt();
    }
}