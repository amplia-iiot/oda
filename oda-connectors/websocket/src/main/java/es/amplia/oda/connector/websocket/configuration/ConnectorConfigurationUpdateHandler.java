package es.amplia.oda.connector.websocket.configuration;

import es.amplia.oda.connector.websocket.WebSocketConnector;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

public class ConnectorConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorConfigurationUpdateHandler.class);

    static final String HOST_PROPERTY_NAME = "host";
    static final String PORT_PROPERTY_NAME = "port";
    static final String PATH_PROPERTY_NAME = "path";
    static final String CONNECTION_TIMEOUT_PROPERTY_NAME = "connectionTimeout";
    static final String KEEP_ALIVE_INTERVAL_PROPERTY_NAME = "keepaliveInterval";

    private final WebSocketConnector webSocketConnector;

    private ConnectorConfiguration currentConfiguration;

    public ConnectorConfigurationUpdateHandler(WebSocketConnector webSocketConnector) {
        this.webSocketConnector = webSocketConnector;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading WebSocket Connector configuration");

        ConnectorConfiguration.ConnectorConfigurationBuilder builder = ConnectorConfiguration.builder();

        Optional.ofNullable((String) props.get(HOST_PROPERTY_NAME)).ifPresent(builder::host);
        Optional.ofNullable((String) props.get(PORT_PROPERTY_NAME)).ifPresent(value ->
                builder.port(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(PATH_PROPERTY_NAME)).ifPresent(builder::path);
        Optional.ofNullable((String) props.get(CONNECTION_TIMEOUT_PROPERTY_NAME)).ifPresent(value ->
                builder.connectionTimeout(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(KEEP_ALIVE_INTERVAL_PROPERTY_NAME)).ifPresent(value ->
                builder.keepAliveInterval(Integer.parseInt(value)));

        currentConfiguration = builder.build();

        LOGGER.info("WebSocket Connector configuration loaded");
    }

    @Override
    public void applyConfiguration() {
        LOGGER.info("Applying WebSocket Connector configuration");

        webSocketConnector.loadConfiguration(currentConfiguration);

        LOGGER.info("WebSocket Connector configuration applied");
    }
}
