package es.amplia.oda.connector.thingstream.mqttsn;

import es.amplia.oda.hardware.atmanager.api.*;

import com.myriadgroup.iot.sdk.IoTSDKConstants;
import com.myriadgroup.iot.sdk.client.modem.IModemCallback;
import com.myriadgroup.iot.sdk.client.modem.ModemException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static es.amplia.oda.connector.thingstream.mqttsn.USSDModemImplementation.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class USSDModemImplementationTest {

    private static final String MESSAGE = "This is a test message";
    private static final int SECONDS_TO_WAIT = 2;

    private static final ATCommand TEST_EMPTY_COMMAND = ATCommand.emptyCommand();
    private static final ATCommand TEST_ATF_COMMAND = ATCommand.ampersandCommand('F');
    private static final ATCommand TEST_ATE0_COMMAND = ATCommand.basicCommand('E', 0);
    private static final ATCommand TEST_ATS3_COMMAND = ATCommand.sSetCommand(3, 13);
    private static final ATCommand TEST_CMGF_COMMAND = ATCommand.extendedSetCommand(CMGF_COMMAND, "0");
    private static final ATCommand TEST_ENABLE_CUSD_COMMAND = ATCommand.extendedSetCommand(CUSD_COMMAND, "1");
    private static final ATCommand TEST_CUSD_COMMAND = ATCommand.extendedSetCommand(CUSD_COMMAND, "1", MESSAGE);
    private static final ATCommand TEST_CMEE_COMMAND = ATCommand.extendedSetCommand(CMEE_COMMAND, "1");
    private static final ATCommand TEST_CSQ_COMMAND = ATCommand.extendedCommand(ATCommandType.ACTION, CSQ_COMMAND);
    private static final ATCommand TEST_COPS_COMMAND = ATCommand.extendedCommand(ATCommandType.READ, COPS_COMMAND);
    private static final ATCommand TEST_CREG_COMMAND = ATCommand.extendedCommand(ATCommandType.READ, CREG_COMMAND);
    private static final ATCommand TEST_CUSD_CLOSE_COMMAND = ATCommand.extendedSetCommand(CUSD_COMMAND, "2");

    private static final ATResponse mockedResponse =
            ATResponse.ok(Collections.singletonList(ATEvent.event("+MOCK", "mockResult")));
    private static final ATResponse mockedErrorResponse = ATResponse.error();

    private static final String IS_RUNNING_FIELD_NAME = "isRunning";
    private static final String MODEM_CALLBACKS_FIELD_NAME = "modemCallbacks";
    private static final String MODEM_EXCEPTION_MESSAGE = "Modem exception must be thrown";
    private static final String SUCCESS_COUNT_FIELD_NAME = "successCount";
    private static final String FAILURE_COUNT_FIELD_NAME = "failureCount";

    @Mock
    private ATManager mockedAtManager;
    @InjectMocks
    private USSDModemImplementation testUSSDModem;

    @Mock
    private IModemCallback mockedModemCallback;
    @Spy
    private Set<IModemCallback> spiedEmptyModemCallbacks = new HashSet<>();
    private Set<IModemCallback> spiedModemCallbacksWithMockedCallback;

    @Before
    public void setUp() {
        Set<IModemCallback> modemCallbacksWithMockedCallback = new HashSet<>();
        modemCallbacksWithMockedCallback.add(mockedModemCallback);
        spiedModemCallbacksWithMockedCallback = spy(modemCallbacksWithMockedCallback);
    }

    @Test
    public void testStart() throws ModemException, ATManager.AlreadyRegisteredException {
        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(mockedResponse));

        testUSSDModem.start();

        assertTrue(Whitebox.getInternalState(testUSSDModem, IS_RUNNING_FIELD_NAME));
        verify(mockedAtManager).send(eq(TEST_EMPTY_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_ATF_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_ATE0_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_ATS3_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_CMGF_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_ENABLE_CUSD_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_CMEE_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_CSQ_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_COPS_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_CREG_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).registerEvent(eq(CUSD_COMMAND), any());
    }

    @Test(expected = ModemException.class)
    public void testStartException() throws ModemException {
        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenThrow(new RuntimeException(""));

        testUSSDModem.start();

        fail(MODEM_EXCEPTION_MESSAGE);
    }

    @Test
    public void testStartAlreadyRunning() throws ModemException {
        Whitebox.setInternalState(testUSSDModem, IS_RUNNING_FIELD_NAME, true);

        testUSSDModem.start();

        verifyZeroInteractions(mockedAtManager);
    }

    @Test
    public void testStop() throws ModemException {
        Whitebox.setInternalState(testUSSDModem, IS_RUNNING_FIELD_NAME, true);
        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, spiedModemCallbacksWithMockedCallback);

        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(mockedResponse));

        testUSSDModem.stop();

        assertFalse(Whitebox.getInternalState(testUSSDModem, IS_RUNNING_FIELD_NAME));
        verify(mockedAtManager).send(eq(TEST_CUSD_CLOSE_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).unregisterEvent(CUSD_COMMAND);
        verify(spiedModemCallbacksWithMockedCallback).clear();
    }

    @Test
    public void testStopNotRunning() throws ModemException {
        Whitebox.setInternalState(testUSSDModem, IS_RUNNING_FIELD_NAME, false);

        testUSSDModem.stop();

        verifyZeroInteractions(mockedAtManager);
    }

    @Test
    public void testStopExceptionCaught() throws ModemException {
        Whitebox.setInternalState(testUSSDModem, IS_RUNNING_FIELD_NAME, true);
        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, spiedModemCallbacksWithMockedCallback);

        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenThrow(new RuntimeException(""));

        testUSSDModem.stop();

        assertFalse(Whitebox.getInternalState(testUSSDModem, IS_RUNNING_FIELD_NAME));
        verify(mockedAtManager).unregisterEvent(eq(CUSD_COMMAND));
        verify(spiedModemCallbacksWithMockedCallback).clear();
    }

    @Test
    public void testIsRunning() {
        Whitebox.setInternalState(testUSSDModem, IS_RUNNING_FIELD_NAME, true);

        assertTrue(testUSSDModem.isRunning());
    }

    @Test
    public void testSendData() throws ModemException, UnsupportedEncodingException, InterruptedException {
        int responseCode = 1;
        String response = "This is a test response";
        ATResponse mockedCusdResponse =
                ATResponse.ok(Collections.singletonList(ATEvent.event(CUSD_COMMAND, String.valueOf(responseCode), response)));
        CountDownLatch latch = new CountDownLatch(1);

        Set<IModemCallback> modemCallbacks = new HashSet<>();
        IModemCallback modemCallback = (code, data) -> latch.countDown();
        modemCallbacks.add(modemCallback);

        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, modemCallbacks);

        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(mockedCusdResponse));

        testUSSDModem.sendData(MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING));

        assertTrue(latch.await(SECONDS_TO_WAIT, TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_CUSD_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        assertEquals(1, getAtomicIntegerFieldWithName(SUCCESS_COUNT_FIELD_NAME).get());
        assertEquals(0, getAtomicIntegerFieldWithName(FAILURE_COUNT_FIELD_NAME).get());
    }

    private AtomicInteger getAtomicIntegerFieldWithName(String fieldName) {
        return Whitebox.getInternalState(testUSSDModem, fieldName);
    }

    @Test(expected = ModemException.class)
    public void testSendDataErrorResponse() throws ModemException, UnsupportedEncodingException {
        ATResponse mockedCusdErrorResponse =
                ATResponse.error("Error description");

        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(mockedCusdErrorResponse));

        try{
            testUSSDModem.sendData(MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING));
        } finally {
            verify(mockedAtManager).send(eq(TEST_CUSD_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
            assertEquals(0, getAtomicIntegerFieldWithName(SUCCESS_COUNT_FIELD_NAME).get());
            assertEquals(1, getAtomicIntegerFieldWithName(FAILURE_COUNT_FIELD_NAME).get());
        }
    }

    @Test
    public void testSendDataNoPartialResponses() throws ModemException, UnsupportedEncodingException {
        ATResponse mockedCusdResponseWithoutPartialResponses = ATResponse.ok();

        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, spiedModemCallbacksWithMockedCallback);

        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(mockedCusdResponseWithoutPartialResponses));

        testUSSDModem.sendData(MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING));

        verify(mockedAtManager).send(eq(TEST_CUSD_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verifyZeroInteractions(mockedModemCallback);
        assertEquals(1, getAtomicIntegerFieldWithName(SUCCESS_COUNT_FIELD_NAME).get());
        assertEquals(0, getAtomicIntegerFieldWithName(FAILURE_COUNT_FIELD_NAME).get());
    }

    @Test(expected = ModemException.class)
    public void testSendDataException() throws ModemException, UnsupportedEncodingException {
        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenThrow(new RuntimeException(""));

        try {
            testUSSDModem.sendData(MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING));
        } finally {
            assertEquals(0, getAtomicIntegerFieldWithName(SUCCESS_COUNT_FIELD_NAME).get());
            assertEquals(1, getAtomicIntegerFieldWithName(FAILURE_COUNT_FIELD_NAME).get());
        }
    }

    @Test
    public void testSendDataCusdCode4() throws ModemException, UnsupportedEncodingException {
        int responseCode = 4;
        String response = "This is a test response";
        ATResponse mockedCusdResponse =
                ATResponse.ok(Collections.singletonList(ATEvent.event(CUSD_COMMAND, String.valueOf(responseCode), response)));

        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, spiedModemCallbacksWithMockedCallback);

        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(mockedCusdResponse));

        testUSSDModem.sendData(MESSAGE.getBytes(IoTSDKConstants.DEFAULT_ENCODING));

        verify(mockedAtManager).send(eq(TEST_CUSD_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verifyZeroInteractions(mockedModemCallback);
    }

    @Test
    public void testRegisterCallback() throws ModemException {
        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, spiedEmptyModemCallbacks);

        testUSSDModem.registerCallback(mockedModemCallback);

        verify(spiedEmptyModemCallbacks).add(eq(mockedModemCallback));
    }

    @Test
    public void testRegisterCallbackAlreadyRegistered() throws ModemException {
        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, spiedModemCallbacksWithMockedCallback);

        testUSSDModem.registerCallback(mockedModemCallback);

        verify(spiedModemCallbacksWithMockedCallback, never()).add(any(IModemCallback.class));
    }

    @Test(expected = ModemException.class)
    public void testRegisterNullCallback() throws ModemException {
        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, spiedEmptyModemCallbacks);

        testUSSDModem.registerCallback(null);

        fail(MODEM_EXCEPTION_MESSAGE);
    }

    @Test
    public void testUnregisterCallback() throws ModemException {
        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, spiedModemCallbacksWithMockedCallback);

        testUSSDModem.unregisterCallback(mockedModemCallback);

        verify(spiedModemCallbacksWithMockedCallback).remove(eq(mockedModemCallback));
    }

    @Test
    public void testUnregisterCallbackNotRegisteredBefore() throws ModemException {
        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, spiedEmptyModemCallbacks);

        testUSSDModem.unregisterCallback(mockedModemCallback);

        verify(spiedEmptyModemCallbacks, never()).remove(any(IModemCallback.class));
    }

    @Test(expected = ModemException.class)
    public void testUnregisterNullCallback() throws ModemException {
        Whitebox.setInternalState(testUSSDModem, MODEM_CALLBACKS_FIELD_NAME, spiedModemCallbacksWithMockedCallback);

        testUSSDModem.unregisterCallback(null);

        fail(MODEM_EXCEPTION_MESSAGE);
    }

    @Test
    public void testGetStatus() {
        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(mockedResponse));

        Map<String, String> result = testUSSDModem.getStatus();

        assertEquals(2, result.size());
        verify(mockedAtManager).send(eq(TEST_CSQ_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedAtManager).send(eq(TEST_COPS_COMMAND), eq(AT_COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testGetStatusExceptionCaught() {
        when(mockedAtManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(mockedErrorResponse));

        Map<String, String> result = testUSSDModem.getStatus();

        assertTrue("Exception is caught", true);
        assertNotNull(result);
    }
}