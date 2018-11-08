package es.amplia.oda.connector.coap.at;

import es.amplia.oda.hardware.atmanager.api.*;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.californium.elements.RawData;
import org.eclipse.californium.elements.RawDataChannel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static es.amplia.oda.connector.coap.at.ATUDPConnector.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ATUDPConnectorTest {

    private static final String TEST_REMOTE_HOST = "127.0.0.1";
    private static final int TEST_REMOTE_PORT = 5683;
    private static final String TEST_LOCAL_HOST = "10.10.10.10";
    private static final int TEST_LOCAL_PORT = 4321;
    private static final InetSocketAddress TEST_LOCAL_ADDRESS =
            new InetSocketAddress(TEST_LOCAL_HOST, TEST_LOCAL_PORT);
    private static final String TEST_MESSAGE = "TEST DATA";
    private static final byte[] TEST_DATA = TEST_MESSAGE.getBytes();

    private ATUDPConnector testConnector;

    @Mock
    private ATManager mockedATManager;
    @Mock
    private ExecutorService mockedSenderExecutor;
    @Mock
    private ExecutorService mockedReceiverExecutor;
    @Mock
    private RawDataChannel mockedRawDataChannel;
    @Mock
    private CompletableFuture<ATResponse> mockedCompletableFuture;

    @Before
    public void setUp() {
        testConnector = new ATUDPConnector(mockedATManager, TEST_REMOTE_HOST, TEST_REMOTE_PORT, TEST_LOCAL_PORT);
    }

    @Test
    public void testStart() throws Exception {
        int localSocketId = 0;
        ATCommand closeSocketCommand =
                ATCommand.extendedSetCommand(CLOSE_SOCKET_AT_COMMAND, String.valueOf(localSocketId));
        ATResponse closeSocketResponse = ATResponse.ok();
        ATCommand openSocketCommand = ATCommand.extendedSetCommand(OPEN_SOCKET_AT_COMMAND, DGRAM_TYPE,
                String.valueOf(UDP_TYPE), String.valueOf(TEST_LOCAL_PORT));
        ATResponse openSocketResponse = ATResponse.ok("\n" + localSocketId + "\n");
        ATCommand getAddressCommand = ATCommand.extendedCommand(ATCommandType.ACTION, GET_ADDRESS_AT_COMMAND);
        ATResponse getAddressResponse =  ATResponse.ok(Collections.singletonList(ATEvent.event(GET_ADDRESS_AT_COMMAND,
                String.valueOf(localSocketId), TEST_LOCAL_HOST)));

        Whitebox.setInternalState(testConnector, "running", false);

        when(mockedATManager.send(eq(closeSocketCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(closeSocketResponse));
        when(mockedATManager.send(eq(openSocketCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(openSocketResponse));
        when(mockedATManager.send(eq(getAddressCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(getAddressResponse));

        testConnector.start();

        assertTrue(Whitebox.getInternalState(testConnector, "running"));
        assertEquals(localSocketId, (int) Whitebox.getInternalState(testConnector, "localSocketId"));
        assertEquals(TEST_LOCAL_ADDRESS, testConnector.getAddress());
        verify(mockedATManager).send(eq(closeSocketCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager).send(eq(openSocketCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager).send(eq(getAddressCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager).registerEvent(eq(ARRIVE_SOCKET_MESSAGE_AT_EVENT), any());
    }

    @Test
    public void testStartAlreadyRunning() {
        Whitebox.setInternalState(testConnector, "running", true);

        testConnector.start();

        verifyZeroInteractions(mockedATManager);
    }

    @Test
    public void testStartErrorCreatingSocketAndGettingLocalAddress() throws Exception {
        int localSocketId = 0;
        ATCommand closeSocketCommand =
                ATCommand.extendedSetCommand(CLOSE_SOCKET_AT_COMMAND, String.valueOf(localSocketId));
        ATResponse closeSocketResponse = ATResponse.ok();
        ATCommand openSocketCommand = ATCommand.extendedSetCommand(OPEN_SOCKET_AT_COMMAND, DGRAM_TYPE,
                String.valueOf(UDP_TYPE), String.valueOf(TEST_LOCAL_PORT));
        ATResponse openSocketErrorResponse = ATResponse.error();
        ATCommand getAddressCommand = ATCommand.extendedCommand(ATCommandType.ACTION, GET_ADDRESS_AT_COMMAND);
        ATResponse getAddressResponse = ATResponse.error();

        Whitebox.setInternalState(testConnector, "running", false);

        when(mockedATManager.send(eq(closeSocketCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(closeSocketResponse));
        when(mockedATManager.send(eq(openSocketCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(openSocketErrorResponse));
        when(mockedATManager.send(eq(getAddressCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(getAddressResponse));

        testConnector.start();

        assertFalse(Whitebox.getInternalState(testConnector, "running"));
        verify(mockedATManager).send(eq(openSocketCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager).send(eq(getAddressCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager, never()).registerEvent(eq(ARRIVE_SOCKET_MESSAGE_AT_EVENT), any());
    }

    @Test
    public void testStartInvalidResponseCreatingSocketAndGettingLocalAddress() throws Exception {
        int localSocketId = 0;
        ATCommand closeSocketCommand =
                ATCommand.extendedSetCommand(CLOSE_SOCKET_AT_COMMAND, String.valueOf(localSocketId));
        ATResponse closeSocketResponse = ATResponse.ok();
        ATCommand openSocketCommand = ATCommand.extendedSetCommand(OPEN_SOCKET_AT_COMMAND, DGRAM_TYPE,
                String.valueOf(UDP_TYPE), String.valueOf(TEST_LOCAL_PORT));
        ATResponse openSocketErrorResponse = ATResponse.ok("invalid");
        ATCommand getAddressCommand = ATCommand.extendedCommand(ATCommandType.ACTION, GET_ADDRESS_AT_COMMAND);
        ATResponse getAddressResponse =  ATResponse.ok(Collections.singletonList(ATEvent.event(GET_ADDRESS_AT_COMMAND,
                TEST_LOCAL_HOST)));

        Whitebox.setInternalState(testConnector, "running", false);

        when(mockedATManager.send(eq(closeSocketCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(closeSocketResponse));
        when(mockedATManager.send(eq(openSocketCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(openSocketErrorResponse));
        when(mockedATManager.send(eq(getAddressCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(getAddressResponse));

        testConnector.start();

        assertFalse(Whitebox.getInternalState(testConnector, "running"));
        verify(mockedATManager).send(eq(openSocketCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager).send(eq(getAddressCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager, never()).registerEvent(eq(ARRIVE_SOCKET_MESSAGE_AT_EVENT), any());
    }

    @Test
    public void testStartExecutionExceptionCaughtSendingATCommands() throws Exception {
        Whitebox.setInternalState(testConnector, "running", false);

        when(mockedATManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockedCompletableFuture);
        when(mockedCompletableFuture.get()).thenThrow(new ExecutionException("", null));

        testConnector.start();

        assertFalse(Whitebox.getInternalState(testConnector, "running"));
        verify(mockedATManager, atLeastOnce()).send(any(ATCommand.class), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager, never()).registerEvent(eq(ARRIVE_SOCKET_MESSAGE_AT_EVENT), any());
    }

    @Test
    public void testStartInterruptedExceptionCaughtSendingATCommands() throws Exception {
        Whitebox.setInternalState(testConnector, "running", false);

        when(mockedATManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockedCompletableFuture);
        when(mockedCompletableFuture.get()).thenThrow(new InterruptedException());

        testConnector.start();

        assertFalse(Whitebox.getInternalState(testConnector, "running"));
        verify(mockedATManager, atLeastOnce()).send(any(ATCommand.class), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager, never()).registerEvent(eq(ARRIVE_SOCKET_MESSAGE_AT_EVENT), any());
    }

    @Test
    public void testStartAlreadyRegisteredEventExceptionCaught() throws Exception {
        int localSocketId = 0;
        ATCommand closeSocketCommand =
                ATCommand.extendedSetCommand(CLOSE_SOCKET_AT_COMMAND, String.valueOf(localSocketId));
        ATResponse closeSocketResponse = ATResponse.ok();
        ATCommand openSocketCommand = ATCommand.extendedSetCommand(OPEN_SOCKET_AT_COMMAND, DGRAM_TYPE,
                String.valueOf(UDP_TYPE), String.valueOf(TEST_LOCAL_PORT));
        ATResponse openSocketResponse = ATResponse.ok("\n" + localSocketId + "\n");
        ATCommand getAddressCommand = ATCommand.extendedCommand(ATCommandType.ACTION, GET_ADDRESS_AT_COMMAND);
        ATResponse getAddressResponse =  ATResponse.ok(Collections.singletonList(ATEvent.event(GET_ADDRESS_AT_COMMAND,
                String.valueOf(localSocketId), TEST_LOCAL_HOST)));

        Whitebox.setInternalState(testConnector, "running", false);

        when(mockedATManager.send(eq(closeSocketCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(closeSocketResponse));
        when(mockedATManager.send(eq(openSocketCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(openSocketResponse));
        when(mockedATManager.send(eq(getAddressCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(getAddressResponse));
        doThrow(new ATManager.AlreadyRegisteredException("")).when(mockedATManager)
                .registerEvent(eq(ARRIVE_SOCKET_MESSAGE_AT_EVENT), any());

        testConnector.start();

        assertTrue(Whitebox.getInternalState(testConnector, "running"));
        assertEquals(localSocketId, (int) Whitebox.getInternalState(testConnector, "localSocketId"));
        assertEquals(TEST_LOCAL_ADDRESS, testConnector.getAddress());
        verify(mockedATManager).registerEvent(eq(ARRIVE_SOCKET_MESSAGE_AT_EVENT), any());
    }

    @Test
    public void testProcessArriveSocketMessageEvent() {
        int socketId = 0;
        int messageId = 1;
        ATEvent arriveSocketMessageEvent =
                ATEvent.event(ARRIVE_SOCKET_MESSAGE_AT_EVENT, String.valueOf(socketId), String.valueOf(messageId));

        Whitebox.setInternalState(testConnector, "receiverExecutor", mockedReceiverExecutor);

        testConnector.processArriveSocketMessageEvent(arriveSocketMessageEvent);

        verify(mockedReceiverExecutor).submit(any(Runnable.class));
    }

    @Test
    public void testProcessArriveSocketMessageEventInvalidNumberOfParams() {
        int socketId = 0;
        ATEvent arriveSocketMessageEvent = ATEvent.event(ARRIVE_SOCKET_MESSAGE_AT_EVENT, String.valueOf(socketId));

        Whitebox.setInternalState(testConnector, "receiverExecutor", mockedReceiverExecutor);

        testConnector.processArriveSocketMessageEvent(arriveSocketMessageEvent);

        verify(mockedReceiverExecutor, never()).submit(any(Runnable.class));
    }

    @Test
    public void testProcessArriveSocketMessageEventNumberFormatException() {
        int socketId = 0;
        ATEvent arriveSocketMessageEvent =
                ATEvent.event(ARRIVE_SOCKET_MESSAGE_AT_EVENT, String.valueOf(socketId), "invalid");

        Whitebox.setInternalState(testConnector, "receiverExecutor", mockedReceiverExecutor);

        testConnector.processArriveSocketMessageEvent(arriveSocketMessageEvent);

        verify(mockedReceiverExecutor, never()).submit(any(Runnable.class));
    }

    @Test
    public void testReadArrivedMessage() throws Exception {
        int socketId = 0;
        int messageId = 1;
        ATCommand readMessageCommand = ATCommand.extendedSetCommand(READ_MESSAGE_AT_COMMAND, String.valueOf(socketId),
                String.valueOf(messageId));
        String hexData = Hex.encodeHexString(TEST_DATA);
        ATResponse readMessageResponse =
                ATResponse.ok("\n0,10.10.10.10,5683," + TEST_MESSAGE.length() + "," + hexData + ",0\n");
        RawData rawData = new RawData(TEST_DATA, InetAddress.getByName(TEST_REMOTE_HOST), TEST_REMOTE_PORT);
        ArgumentCaptor<RawData> rawDataCaptor = ArgumentCaptor.forClass(RawData.class);

        Whitebox.setInternalState(testConnector, "remoteHost", TEST_REMOTE_HOST);
        Whitebox.setInternalState(testConnector, "remotePort", TEST_REMOTE_PORT);
        Whitebox.setInternalState(testConnector, "receiver", mockedRawDataChannel);

        when(mockedATManager.send(eq(readMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(readMessageResponse));

        testConnector.readArrivedMessage(socketId, messageId);

        verify(mockedATManager).send(eq(readMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedRawDataChannel).receiveData(rawDataCaptor.capture());
        RawData capturedRawData = rawDataCaptor.getValue();
        assertArrayEquals(rawData.getBytes(), capturedRawData.getBytes());
        assertEquals(rawData.getAddress(), capturedRawData.getAddress());
        assertEquals(rawData.getPort(), capturedRawData.getPort());
    }

    @Test
    public void testReadArrivedMessageReadMessageATResponseError() {
        int socketId = 0;
        int messageId = 1;
        ATCommand readMessageCommand = ATCommand.extendedSetCommand(READ_MESSAGE_AT_COMMAND, String.valueOf(socketId),
                String.valueOf(messageId));
        ATResponse readMessageErrorResponse = ATResponse.error();

        Whitebox.setInternalState(testConnector, "remoteHost", TEST_REMOTE_HOST);
        Whitebox.setInternalState(testConnector, "remotePort", TEST_REMOTE_PORT);
        Whitebox.setInternalState(testConnector, "receiver", mockedRawDataChannel);

        when(mockedATManager.send(eq(readMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(readMessageErrorResponse));

        testConnector.readArrivedMessage(socketId, messageId);

        verify(mockedATManager).send(eq(readMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedRawDataChannel, never()).receiveData(any(RawData.class));
    }

    @Test
    public void testReadArrivedMessageReadMessageATResponseInvalid() {
        int socketId = 0;
        int messageId = 1;
        ATCommand readMessageCommand = ATCommand.extendedSetCommand(READ_MESSAGE_AT_COMMAND, String.valueOf(socketId),
                String.valueOf(messageId));
        ATResponse readMessageInvalidResponse = ATResponse.ok("\ninvalid\n");

        Whitebox.setInternalState(testConnector, "remoteHost", TEST_REMOTE_HOST);
        Whitebox.setInternalState(testConnector, "remotePort", TEST_REMOTE_PORT);
        Whitebox.setInternalState(testConnector, "receiver", mockedRawDataChannel);

        when(mockedATManager.send(eq(readMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(readMessageInvalidResponse));

        testConnector.readArrivedMessage(socketId, messageId);

        verify(mockedATManager).send(eq(readMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedRawDataChannel, never()).receiveData(any(RawData.class));
    }

    @Test
    public void testReadArrivedMessageReadMessageATCommandExecutionExceptionCaught() throws Exception {
        int socketId = 0;
        int messageId = 1;
        ATCommand readMessageCommand = ATCommand.extendedSetCommand(READ_MESSAGE_AT_COMMAND, String.valueOf(socketId),
                String.valueOf(messageId));

        Whitebox.setInternalState(testConnector, "remoteHost", TEST_REMOTE_HOST);
        Whitebox.setInternalState(testConnector, "remotePort", TEST_REMOTE_PORT);
        Whitebox.setInternalState(testConnector, "receiver", mockedRawDataChannel);

        when(mockedATManager.send(eq(readMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockedCompletableFuture);
        when(mockedCompletableFuture.get()).thenThrow(new ExecutionException("", null));

        testConnector.readArrivedMessage(socketId, messageId);

        verify(mockedATManager).send(eq(readMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedRawDataChannel, never()).receiveData(any(RawData.class));
    }

    @Test
    public void testReadArrivedMessageReadMessageATCommandInterruptedExceptionCaught() throws Exception {
        int socketId = 0;
        int messageId = 1;
        ATCommand readMessageCommand = ATCommand.extendedSetCommand(READ_MESSAGE_AT_COMMAND, String.valueOf(socketId),
                String.valueOf(messageId));

        Whitebox.setInternalState(testConnector, "remoteHost", TEST_REMOTE_HOST);
        Whitebox.setInternalState(testConnector, "remotePort", TEST_REMOTE_PORT);
        Whitebox.setInternalState(testConnector, "receiver", mockedRawDataChannel);

        when(mockedATManager.send(eq(readMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockedCompletableFuture);
        when(mockedCompletableFuture.get()).thenThrow(new InterruptedException());

        testConnector.readArrivedMessage(socketId, messageId);

        verify(mockedATManager).send(eq(readMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedRawDataChannel, never()).receiveData(any(RawData.class));
    }

    @Test
    public void testReadArrivedMessageDecoderExceptionCaught() {
        int socketId = 0;
        int messageId = 1;
        ATCommand readMessageCommand = ATCommand.extendedSetCommand(READ_MESSAGE_AT_COMMAND, String.valueOf(socketId),
                String.valueOf(messageId));
        String invalidData = "INVALID";
        ATResponse readMessageResponse =
                ATResponse.ok("\n0,10.10.10.10,5683,8," + invalidData + ",0\n");

        Whitebox.setInternalState(testConnector, "remoteHost", TEST_REMOTE_HOST);
        Whitebox.setInternalState(testConnector, "remotePort", TEST_REMOTE_PORT);
        Whitebox.setInternalState(testConnector, "receiver", mockedRawDataChannel);

        when(mockedATManager.send(eq(readMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(readMessageResponse));

        testConnector.readArrivedMessage(socketId, messageId);

        verify(mockedATManager).send(eq(readMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedRawDataChannel, never()).receiveData(any(RawData.class));
    }

    @Test
    public void testReadArrivedMessageUnknownHostExceptionCaught() {
        int socketId = 0;
        int messageId = 1;
        ATCommand readMessageCommand = ATCommand.extendedSetCommand(READ_MESSAGE_AT_COMMAND, String.valueOf(socketId),
                String.valueOf(messageId));
        String hexData = Hex.encodeHexString(TEST_DATA);
        ATResponse readMessageResponse =
                ATResponse.ok("\n0,10.10.10.10,5683," + TEST_MESSAGE.length() + "," + hexData + ",0\n");

        Whitebox.setInternalState(testConnector, "remoteHost", "invalid.host.es");
        Whitebox.setInternalState(testConnector, "remotePort", TEST_REMOTE_PORT);
        Whitebox.setInternalState(testConnector, "receiver", mockedRawDataChannel);

        when(mockedATManager.send(eq(readMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(readMessageResponse));

        testConnector.readArrivedMessage(socketId, messageId);

        verify(mockedATManager).send(eq(readMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedRawDataChannel, never()).receiveData(any(RawData.class));
    }

    @Test
    public void testSend() {
        RawData testRawData = new RawData(TEST_DATA, TEST_LOCAL_ADDRESS);

        Whitebox.setInternalState(testConnector, "running", true);
        Whitebox.setInternalState(testConnector, "senderExecutor", mockedSenderExecutor);

        testConnector.send(testRawData);

        verify(mockedSenderExecutor).submit(any(Runnable.class));
    }

    @Test
    public void testSendNotRunning() {
        RawData testRawData = new RawData(TEST_DATA, TEST_LOCAL_ADDRESS);

        Whitebox.setInternalState(testConnector, "running", false);

        testConnector.send(testRawData);

        verifyZeroInteractions(mockedSenderExecutor);
    }

    @Test
    public void testSendNullMessage() {
        Whitebox.setInternalState(testConnector, "running", true);

        testConnector.send(null);

        verifyZeroInteractions(mockedSenderExecutor);
    }

    @Test
    public void testSendMessage() {
        int localSocketId = 0;
        String msg = "A1B2C3D4E5F6";
        int length = msg.length();
        ATCommand sendMessageCommand =
                ATCommand.extendedSetCommand(SEND_MESSAGE_AT_COMMAND, String.valueOf(localSocketId), TEST_REMOTE_HOST,
                        String.valueOf(TEST_REMOTE_PORT), String.valueOf(length), msg);
        ATResponse sendMessageResponse = ATResponse.ok();

        when(mockedATManager.send(eq(sendMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(sendMessageResponse));

        testConnector.sendMessage(localSocketId, TEST_REMOTE_HOST, TEST_REMOTE_PORT, length, msg);

        verify(mockedATManager).send(eq(sendMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testSendMessageATResponseError() {
        int localSocketId = 0;
        String msg = "A1B2C3D4E5F6";
        int length = msg.length();
        ATCommand sendMessageCommand =
                ATCommand.extendedSetCommand(SEND_MESSAGE_AT_COMMAND, String.valueOf(localSocketId), TEST_REMOTE_HOST,
                        String.valueOf(TEST_REMOTE_PORT), String.valueOf(length), msg);
        ATResponse sendMessageErrorResponse = ATResponse.error();

        when(mockedATManager.send(eq(sendMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(sendMessageErrorResponse));

        testConnector.sendMessage(localSocketId, TEST_REMOTE_HOST, TEST_REMOTE_PORT, length, msg);

        verify(mockedATManager).send(eq(sendMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testSendMessageExecutionExceptionCaught() throws Exception {
        int localSocketId = 0;
        String msg = "A1B2C3D4E5F6";
        int length = msg.length();
        ATCommand sendMessageCommand =
                ATCommand.extendedSetCommand(SEND_MESSAGE_AT_COMMAND, String.valueOf(localSocketId), TEST_REMOTE_HOST,
                        String.valueOf(TEST_REMOTE_PORT), String.valueOf(length), msg);

        when(mockedATManager.send(eq(sendMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockedCompletableFuture);
        when(mockedCompletableFuture.get()).thenThrow(new ExecutionException("", null));

        testConnector.sendMessage(localSocketId, TEST_REMOTE_HOST, TEST_REMOTE_PORT, length, msg);

        verify(mockedATManager).send(eq(sendMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testSendMessageInterruptedExceptionCaught() throws Exception {
        int localSocketId = 0;
        String msg = "A1B2C3D4E5F6";
        int length = msg.length();
        ATCommand sendMessageCommand =
                ATCommand.extendedSetCommand(SEND_MESSAGE_AT_COMMAND, String.valueOf(localSocketId), TEST_REMOTE_HOST,
                        String.valueOf(TEST_REMOTE_PORT), String.valueOf(length), msg);

        when(mockedATManager.send(eq(sendMessageCommand), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockedCompletableFuture);
        when(mockedCompletableFuture.get()).thenThrow(new InterruptedException());

        testConnector.sendMessage(localSocketId, TEST_REMOTE_HOST, TEST_REMOTE_PORT, length, msg);

        verify(mockedATManager).send(eq(sendMessageCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
    }

    @Test
    public void testSetRawDataReceiver() {
        testConnector.setRawDataReceiver(mockedRawDataChannel);

        assertEquals(mockedRawDataChannel, Whitebox.getInternalState(testConnector, "receiver"));
    }

    @Test
    public void testGetAddress() {
        Whitebox.setInternalState(testConnector, "localAddress", TEST_LOCAL_ADDRESS);

        assertEquals(TEST_LOCAL_ADDRESS, testConnector.getAddress());
    }

    @Test
    public void testStop() {
        int localSocketId = 0;
        ATCommand closeSocketCommand =
                ATCommand.extendedSetCommand(CLOSE_SOCKET_AT_COMMAND, String.valueOf(localSocketId));
        ATResponse closeSocketResponse = ATResponse.ok();

        Whitebox.setInternalState(testConnector, "localSocketId", localSocketId);
        Whitebox.setInternalState(testConnector, "senderExecutor", mockedSenderExecutor);
        Whitebox.setInternalState(testConnector, "receiverExecutor", mockedReceiverExecutor);
        Whitebox.setInternalState(testConnector, "running", true);

        when(mockedATManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(closeSocketResponse));

        testConnector.stop();

        verify(mockedATManager).send(eq(closeSocketCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager).unregisterEvent(ARRIVE_SOCKET_MESSAGE_AT_EVENT);
        verify(mockedSenderExecutor).shutdown();
        verify(mockedSenderExecutor).shutdown();
    }

    @Test
    public void testStopErrorClosingSocket() {
        int localSocketId = 0;
        ATCommand closeSocketCommand =
                ATCommand.extendedSetCommand(CLOSE_SOCKET_AT_COMMAND, String.valueOf(localSocketId));
        ATResponse closeSocketResponseError = ATResponse.error();

        Whitebox.setInternalState(testConnector, "localSocketId", localSocketId);
        Whitebox.setInternalState(testConnector, "senderExecutor", mockedSenderExecutor);
        Whitebox.setInternalState(testConnector, "receiverExecutor", mockedReceiverExecutor);
        Whitebox.setInternalState(testConnector, "running", true);

        when(mockedATManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(closeSocketResponseError));

        testConnector.stop();

        verify(mockedATManager).send(eq(closeSocketCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager).unregisterEvent(ARRIVE_SOCKET_MESSAGE_AT_EVENT);
        verify(mockedSenderExecutor).shutdown();
        verify(mockedSenderExecutor).shutdown();
    }

    @Test
    public void testStopClosingSocketExecutionExceptionCaught() throws Exception {
        int localSocketId = 0;
        ATCommand closeSocketCommand =
                ATCommand.extendedSetCommand(CLOSE_SOCKET_AT_COMMAND, String.valueOf(localSocketId));

        Whitebox.setInternalState(testConnector, "localSocketId", localSocketId);
        Whitebox.setInternalState(testConnector, "senderExecutor", mockedSenderExecutor);
        Whitebox.setInternalState(testConnector, "receiverExecutor", mockedReceiverExecutor);
        Whitebox.setInternalState(testConnector, "running", true);

        when(mockedATManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockedCompletableFuture);
        when(mockedCompletableFuture.get()).thenThrow(new ExecutionException("", null));

        testConnector.stop();

        verify(mockedATManager).send(eq(closeSocketCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager).unregisterEvent(ARRIVE_SOCKET_MESSAGE_AT_EVENT);
        verify(mockedSenderExecutor).shutdown();
        verify(mockedSenderExecutor).shutdown();
    }

    @Test
    public void testStopClosingSocketInterruptedExceptionCaught() throws Exception {
        int localSocketId = 0;
        ATCommand closeSocketCommand =
                ATCommand.extendedSetCommand(CLOSE_SOCKET_AT_COMMAND, String.valueOf(localSocketId));

        Whitebox.setInternalState(testConnector, "localSocketId", localSocketId);
        Whitebox.setInternalState(testConnector, "senderExecutor", mockedSenderExecutor);
        Whitebox.setInternalState(testConnector, "receiverExecutor", mockedReceiverExecutor);
        Whitebox.setInternalState(testConnector, "running", true);

        when(mockedATManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(mockedCompletableFuture);
        when(mockedCompletableFuture.get()).thenThrow(new InterruptedException());

        testConnector.stop();

        verify(mockedATManager).send(eq(closeSocketCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager).unregisterEvent(ARRIVE_SOCKET_MESSAGE_AT_EVENT);
        verify(mockedSenderExecutor).shutdown();
        verify(mockedSenderExecutor).shutdown();
    }

    @Test
    public void testStopNotRunning() {
        Whitebox.setInternalState(testConnector, "running", false);

        testConnector.stop();

        verifyZeroInteractions(mockedATManager);
    }

    @Test
    public void testDestroy() {
        int localSocketId = 0;
        ATCommand closeSocketCommand =
                ATCommand.extendedSetCommand(CLOSE_SOCKET_AT_COMMAND, String.valueOf(localSocketId));
        ATResponse closeSocketResponse = ATResponse.ok();

        Whitebox.setInternalState(testConnector, "localSocketId", localSocketId);
        Whitebox.setInternalState(testConnector, "senderExecutor", mockedSenderExecutor);
        Whitebox.setInternalState(testConnector, "receiverExecutor", mockedReceiverExecutor);
        Whitebox.setInternalState(testConnector, "running", true);

        when(mockedATManager.send(any(ATCommand.class), anyLong(), any(TimeUnit.class)))
                .thenReturn(CompletableFuture.completedFuture(closeSocketResponse));

        testConnector.destroy();

        verify(mockedATManager).send(eq(closeSocketCommand), eq(COMMAND_TIMEOUT), eq(TimeUnit.SECONDS));
        verify(mockedATManager).unregisterEvent(ARRIVE_SOCKET_MESSAGE_AT_EVENT);
        verify(mockedSenderExecutor).shutdown();
        verify(mockedSenderExecutor).shutdown();
    }
}