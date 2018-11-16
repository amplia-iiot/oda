package es.amplia.oda.connector.websocket;

import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;

class WebSocketClientFactory {

    private final Dispatcher dispatcher;

    WebSocketClientFactory(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    WebSocketClient createWebSocketClient(OpenGateConnector connector, URI uri, int connectionTimeout,
                                          int keepAliveInterval) {
        WebSocketClient client = new WebSocketClientImpl(connector, dispatcher, uri, connectionTimeout);
        client.setConnectionLostTimeout(keepAliveInterval);
        return  client;
    }
}
