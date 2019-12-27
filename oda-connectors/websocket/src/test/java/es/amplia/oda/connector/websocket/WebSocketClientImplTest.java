package es.amplia.oda.connector.websocket;

import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;

import org.java_websocket.handshake.ServerHandshake;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketClientImplTest {

    private static final int TEST_TIMEOUT = 5;
    private static final String TEST_MESSAGE = "Test message";

    @Mock
    private OpenGateConnector mockedConnector;
    @Mock
    private Dispatcher mockedDispatcher;

    private WebSocketClientImpl testClient;

    @Mock
    private CompletableFuture<byte[]> mockedDispatcherResponse;
    @Captor
    private ArgumentCaptor<Consumer<byte[]>> consumerCaptor;

    @Before
    public void setUp() throws Exception {
        URI testUri = new URI("dummy");
        testClient = new WebSocketClientImpl(mockedConnector, mockedDispatcher, testUri, TEST_TIMEOUT);
    }

    @Test
    public void testOnOpen() {
        ServerHandshake mockedHandshake = mock(ServerHandshake.class);

        testClient.onOpen(mockedHandshake);

        assertTrue("Nothing to test", true);
    }

    @Test
    public void onMessage() {
        Consumer<byte[]> consumer;
        byte[] testResponse = new byte[10];

        when(mockedDispatcher.process(any(byte[].class))).thenReturn(mockedDispatcherResponse);

        testClient.onMessage(TEST_MESSAGE);

        verify(mockedDispatcher).process(aryEq(TEST_MESSAGE.getBytes(StandardCharsets.UTF_8)));
        verify(mockedDispatcherResponse).thenAccept(consumerCaptor.capture());
        consumer = consumerCaptor.getValue();
        consumer.accept(testResponse);
        verify(mockedConnector).uplink(aryEq(testResponse));
    }

    @Test
    public void onMessageRuntimeExceptionProcessingIsCaught() {
        when(mockedDispatcher.process(any(byte[].class))).thenThrow(new RuntimeException());

        testClient.onMessage(TEST_MESSAGE);

        verify(mockedDispatcher).process(aryEq(TEST_MESSAGE.getBytes(StandardCharsets.UTF_8)));
        verifyZeroInteractions(mockedConnector);
    }

    @Test
    public void testOnMessageNoDispatcher() {
        when(mockedDispatcher.process(any(byte[].class))).thenReturn(null);

        testClient.onMessage(TEST_MESSAGE);

        verify(mockedDispatcher).process(aryEq(TEST_MESSAGE.getBytes(StandardCharsets.UTF_8)));
        verify(mockedConnector, never()).uplink(any(byte[].class));
    }

    @Test
    public void testOnMessageFutureExceptionallyProcessed() {
        CompletableFuture<byte[]> exceptionallyCompletedFuture = new CompletableFuture<>();
        exceptionallyCompletedFuture.completeExceptionally(new RuntimeException());

        when(mockedDispatcher.process(any(byte[].class))).thenReturn(exceptionallyCompletedFuture);

        testClient.onMessage(TEST_MESSAGE);

        verify(mockedDispatcher).process(aryEq(TEST_MESSAGE.getBytes(StandardCharsets.UTF_8)));
        verify(mockedConnector, never()).uplink(any(byte[].class));
    }

    @Test
    public void testOnClose() {
        int closeCode = 1;
        String reason = "No reason";

        testClient.onClose(closeCode, reason, false);

        assertTrue("Nothing to test", true);
    }

    @Test
    public void testOnError() {
        Exception error = new Exception();

        testClient.onError(error);

        assertTrue("Nothing to test", true);
    }
}