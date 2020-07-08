package es.amplia.oda.hardware.comms;

import es.amplia.oda.core.commons.utils.CommandExecutionException;
import es.amplia.oda.core.commons.utils.CommandProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.*;

import static es.amplia.oda.hardware.comms.CommsManagerImpl.*;
import static org.junit.Assert.assertFalse;
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
    @InjectMocks
    private CommsManagerImpl testCommsManager;

    @Mock
    private Thread mockedThread;


    @Test
    public void testConnect() throws CommandExecutionException, InterruptedException, ExecutionException {
        when(mockedCommandProcessor.execute(eq(TEST_PATH + CONNECT_SCRIPT))).thenReturn("Connected");

        testCommsManager.connect(TEST_PIN, TEST_APN, TEST_USERNAME, TEST_PASS, TEST_CONNECTION_TIMEOUT,
                TEST_RETRY_CONNECTION_TIMER, TEST_PATH);
        waitToConnect();
        Thread executionThread = (Thread) Whitebox.getInternalState(testCommsManager, "connectionThread");

        verify(mockedCommandProcessor).execute(eq(TEST_PATH + INIT_SIM_SCRIPT + " " + TEST_PIN), eq(COMMAND_TIMEOUT_MS));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + CONFIGURE_CONNECTION_SCRIPT + " " + TEST_APN + " " + TEST_USERNAME + " " + TEST_PASS),
                eq(COMMAND_TIMEOUT_MS));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + CONNECT_SCRIPT + " " + TEST_CONNECTION_TIMEOUT),
                eq(COMMAND_TIMEOUT_MS + TEST_CONNECTION_TIMEOUT * 1000));
        executionThread.interrupt();
    }

    @Test
    public void testConnectCommandExecutionExceptionIsCaught() throws CommandExecutionException {
        when(mockedCommandProcessor.execute(anyString(), anyLong()))
                .thenThrow(new CommandExecutionException("", "", new RuntimeException()));

        testCommsManager.connect(TEST_PIN, TEST_APN, TEST_USERNAME, TEST_PASS, TEST_CONNECTION_TIMEOUT,
                TEST_RETRY_CONNECTION_TIMER, TEST_PATH);

        assertTrue("CommandExecutionException should be caught", true);
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + INIT_SIM_SCRIPT + " " + TEST_PIN), eq(COMMAND_TIMEOUT_MS));
    }

    @Test
    public void testConnectCommandExecutionConnectingExceptionIsCaught() throws CommandExecutionException, InterruptedException, ExecutionException {
        when(mockedCommandProcessor.execute(anyString(), anyLong())).thenReturn("").thenReturn("")
                .thenThrow(new CommandExecutionException("", "", new RuntimeException()));

        testCommsManager.connect(TEST_PIN, TEST_APN, TEST_USERNAME, TEST_PASS, TEST_CONNECTION_TIMEOUT,
                TEST_RETRY_CONNECTION_TIMER, TEST_PATH);
        waitToConnect();

        assertTrue("CommandExecutionException should be caught", true);
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + INIT_SIM_SCRIPT + " " + TEST_PIN), eq(COMMAND_TIMEOUT_MS));
        verify(mockedCommandProcessor).execute(eq(TEST_PATH + CONFIGURE_CONNECTION_SCRIPT + " " + TEST_APN + " " + TEST_USERNAME + " " + TEST_PASS),
                eq(COMMAND_TIMEOUT_MS));
        verify(mockedCommandProcessor, atLeast(1)).execute(eq(TEST_PATH + CHECK_APN_SCRIPT + " " + TEST_APN + " " + TEST_PATH), eq(COMMAND_TIMEOUT_MS));
    }

    private void waitToConnect() throws InterruptedException, ExecutionException {
        Executors.newScheduledThreadPool(1).schedule(() -> {}, 1, TimeUnit.SECONDS).get();
    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testCommsManager, "connectionThread", mockedThread);

        testCommsManager.close();


        boolean reconnect = (boolean) Whitebox.getInternalState(testCommsManager, "reconnect");

        assertFalse(reconnect);
        verify(mockedThread).interrupt();
    }
}