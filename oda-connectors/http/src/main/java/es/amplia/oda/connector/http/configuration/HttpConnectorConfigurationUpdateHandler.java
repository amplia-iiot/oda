package es.amplia.oda.connector.http.configuration;

import es.amplia.oda.connector.http.HttpConnector;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

public class HttpConnectorConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpConnectorConfigurationUpdateHandler.class);

    static final String HOST_PROPERTY_NAME = "host";
    static final String PORT_PROPERTY_NAME = "port";
    static final String GENERAL_PATH_PROPERTY_NAME = "generalPath";
    static final String COLLECTION_PATH_PROPERTY_NAME = "collectionPath";
    static final String COMPRESSION_ENABLED_PROPERTY_NAME = "compressionEnabled";
    static final String COMPRESSION_THRESHOLD_PROPERTY_NAME = "compressionThreshold";


    private final HttpConnector connector;

    private ConnectorConfiguration currentConfiguration;


    public HttpConnectorConfigurationUpdateHandler(HttpConnector connector) {
        this.connector = connector;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new configuration");
        ConnectorConfiguration.ConnectorConfigurationBuilder builder = ConnectorConfiguration.builder();

        Optional.ofNullable((String) props.get(HOST_PROPERTY_NAME)).ifPresent(builder::host);
        Optional.ofNullable((String) props.get(PORT_PROPERTY_NAME)).ifPresent(value -> builder.port(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(GENERAL_PATH_PROPERTY_NAME)).ifPresent(builder::generalPath);
        Optional.ofNullable((String) props.get(COLLECTION_PATH_PROPERTY_NAME)).ifPresent(builder::collectionPath);
        Optional.ofNullable((String) props.get(COMPRESSION_ENABLED_PROPERTY_NAME)).ifPresent(value ->
                builder.compressionEnabled(Boolean.parseBoolean(value)));
        Optional.ofNullable((String) props.get(COMPRESSION_THRESHOLD_PROPERTY_NAME)).ifPresent(value ->
                builder.compressionThreshold(Integer.parseInt(value)));

        currentConfiguration = builder.build();
        LOGGER.info("New configuration loaded");
    }

    @Override
    public void applyConfiguration() {
        LOGGER.info("Applying last configuration");
        if (currentConfiguration != null) {
            connector.loadConfiguration(currentConfiguration);
        }
        LOGGER.info("Last configuration applied");
    }
}
