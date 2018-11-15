package es.amplia.oda.connector.websocket;

import es.amplia.oda.connector.websocket.configuration.ConnectorConfiguration;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;

import lombok.Value;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class WebSocketConnector implements OpenGateConnector, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConnector.class);

    static final String WEBSOCKET_HEADER = "ws";
    static final String API_KEY_PARAM = "X-ApiKey";

    private final DeviceInfoProvider deviceInfoProvider;
    private final WebSocketClientFactory clientFactory;

    private WebSocketClient client;


    WebSocketConnector(DeviceInfoProvider deviceInfoProvider, WebSocketClientFactory clientFactory) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.clientFactory = clientFactory;
    }

    public void loadConfiguration(ConnectorConfiguration configuration) {
        close();

        String deviceId = deviceInfoProvider.getDeviceId();
        String apiKey = deviceInfoProvider.getApiKey();
        if (deviceId == null || apiKey == null) {
            String exceptionMessage = "Error getting device identifier";
            LOGGER.error(exceptionMessage);
            throw new ConfigurationException(exceptionMessage);
        }

        URI uri;
        try {
            uri =  getUri(configuration, deviceId, apiKey);
        } catch (URISyntaxException exception) {
            String exceptionMessage = "Exception building URL: " + exception;
            LOGGER.error(exceptionMessage);
            throw new ConfigurationException(exceptionMessage);
        }

        client = clientFactory.createWebSocketClient(this, uri, configuration.getConnectionTimeout(),
                configuration.getKeepAliveInterval());

        try {
            LOGGER.info("Connecting to URI: {}", uri);
            client.connect();
        } catch (Exception exception) {
            String exceptionMessage = "Exception connecting WebSocket client: " + exception;
            LOGGER.error(exceptionMessage);
            throw new ConfigurationException(exceptionMessage);
        }
    }

    private URI getUri(ConnectorConfiguration configuration, String deviceId, String apiKey) throws URISyntaxException {
        return new URI(WEBSOCKET_HEADER, null, configuration.getHost(), configuration.getPort(),
                configuration.getPath() + "/" + deviceId, API_KEY_PARAM + "=" + apiKey, null);
    }

    @Override
    public void uplink(byte[] payload) {
        if (client != null) {
            try {
                WebSocketMessage message = new WebSocketMessage(payload);
                LOGGER.info("Sending message {}", message);
                client.send(message.getPayload());
            } catch (Exception e) {
                LOGGER.error("Error sending message through WebSocket connector: {}", e);
            }
        } else {
            LOGGER.error("WebSocket connector is not properly configured");
        }
    }

    @Value
    private class WebSocketMessage {
        private final byte[] payload;

        @Override
        public String toString() {
            return new String(payload, StandardCharsets.UTF_8);
        }
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isOpen();
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}
