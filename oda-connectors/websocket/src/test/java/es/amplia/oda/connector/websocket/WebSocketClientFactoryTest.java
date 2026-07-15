package es.amplia.oda.connector.websocket;

import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import org.java_websocket.client.WebSocketClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class WebSocketClientFactoryTest {

    private static final int TEST_TIMEOUT = 5;
    private static final int TEST_KEEP_ALIVE = 10;

    @Mock
    private Dispatcher mockedDispatcher;
    @InjectMocks
    private WebSocketClientFactory testFactory;

    @Mock
    private OpenGateConnector mockedConnector;

    @Test
    public void testCreateWebSocketClient() throws Exception {
        URI testUri = new URI("dummy");

        List<List<?>> clientArgs = new ArrayList<>();
        try (MockedConstruction<WebSocketClientImpl> clientCons =
                     mockConstruction(WebSocketClientImpl.class,
                             (mock, mctx) -> clientArgs.add(new ArrayList<>(mctx.arguments())))) {

            WebSocketClient createdClient =
                    testFactory.createWebSocketClient(mockedConnector, testUri, TEST_TIMEOUT, TEST_KEEP_ALIVE);

            assertEquals(1, clientCons.constructed().size());
            assertEquals(clientCons.constructed().get(0), createdClient);
            assertEquals(mockedConnector, clientArgs.get(0).get(0));
            assertEquals(mockedDispatcher, clientArgs.get(0).get(1));
            assertEquals(testUri, clientArgs.get(0).get(2));
            assertEquals(TEST_TIMEOUT, clientArgs.get(0).get(3));
            verify(clientCons.constructed().get(0)).setConnectionLostTimeout(eq(TEST_KEEP_ALIVE));
        }
    }
}
