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

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketClientImplTest {

    private static final int TEST_TIMEOUT = 5;

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

        // Nothing to test
    }

    @Test
    public void onMessage() {
        String testMessage = "Test message";
        Consumer<byte[]> consumer;
        byte[] testResponse = new byte[10];

        when(mockedDispatcher.process(any(byte[].class))).thenReturn(mockedDispatcherResponse);

        testClient.onMessage(testMessage);

        verify(mockedDispatcher).process(aryEq(testMessage.getBytes(StandardCharsets.UTF_8)));
        verify(mockedDispatcherResponse).thenAccept(consumerCaptor.capture());
        consumer = consumerCaptor.getValue();
        consumer.accept(testResponse);
        verify(mockedConnector).uplink(aryEq(testResponse));
    }

    @Test
    public void testOnMessageNoDispatcher() {
        String testMessage = "Test message";

        when(mockedDispatcher.process(any(byte[].class))).thenReturn(null);

        testClient.onMessage(testMessage);

        verify(mockedDispatcher).process(aryEq(testMessage.getBytes(StandardCharsets.UTF_8)));
        verify(mockedConnector, never()).uplink(any(byte[].class));
    }

    @Test
    public void testOnClose() {
        int closeCode = 1;
        String reason = "No reason";

        testClient.onClose(closeCode, reason, false);

        // Nothing to test
    }

    @Test
    public void testOnError() {
        Exception error = new Exception();

        testClient.onError(error);

        // Nothing to test
    }
}