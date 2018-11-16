package es.amplia.oda.connector.websocket;

import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import org.java_websocket.client.WebSocketClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URI;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest( WebSocketClientFactory.class)
public class WebSocketClientFactoryTest {

    private static final int TEST_TIMEOUT = 5;
    private static final int TEST_KEEP_ALIVE = 10;

    @Mock
    private Dispatcher mockedDispatcher;
    @InjectMocks
    private WebSocketClientFactory testFactory;

    @Mock
    private OpenGateConnector mockedConnector;
    @Mock
    private WebSocketClientImpl mockedClient;

    @Test
    public void testCreateWebSocketClient() throws Exception {
        URI testUri = new URI("dummy");

        PowerMockito.whenNew(WebSocketClientImpl.class).withAnyArguments().thenReturn(mockedClient);

        WebSocketClient createdClient =
                testFactory.createWebSocketClient(mockedConnector, testUri, TEST_TIMEOUT, TEST_KEEP_ALIVE);

        assertEquals(mockedClient, createdClient);
        PowerMockito.verifyNew(WebSocketClientImpl.class)
                .withArguments(eq(mockedConnector), eq(mockedDispatcher), eq(testUri), eq(TEST_TIMEOUT));
        verify(mockedClient).setConnectionLostTimeout(eq(TEST_KEEP_ALIVE));
    }
}