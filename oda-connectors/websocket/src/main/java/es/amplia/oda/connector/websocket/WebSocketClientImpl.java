package es.amplia.oda.connector.websocket;

import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

class WebSocketClientImpl extends WebSocketClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClientImpl.class);

    private static final int MILLISECONDS_PER_SECOND = 1000;

    private final OpenGateConnector connector;
    private final Dispatcher dispatcher;

    WebSocketClientImpl(OpenGateConnector connector, Dispatcher dispatcher, URI uri, int connectionTimeout) {
        super(uri, new Draft_6455(), null, connectionTimeout * MILLISECONDS_PER_SECOND);
        this.connector = connector;
        this.dispatcher = dispatcher;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        LOGGER.info("Open WebSocket connection: {}", serverHandshake.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        LOGGER.info("Messaged arrived");
        LOGGER.debug("Message content: {}", message);
        try {
            CompletableFuture<byte[]> response = dispatcher.process(message.getBytes(StandardCharsets.UTF_8));
            if (response == null) {
                LOGGER.warn("Cannot process message as Dispatcher is not present");
                return;
            }
            response.thenAccept(connector::uplink)
                    .exceptionally(e -> {
                        LOGGER.error("Error processing message {}", message, e);
                        return null;
                    });
        } catch (RuntimeException e) {
            LOGGER.error("Error processing message {}", message, e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        LOGGER.warn("WebSocket connection closed: {}, {}", code, reason);
    }

    @Override
    public void onError(Exception exception) {
        LOGGER.error("Error on WebSocket connection", exception);
    }
}
