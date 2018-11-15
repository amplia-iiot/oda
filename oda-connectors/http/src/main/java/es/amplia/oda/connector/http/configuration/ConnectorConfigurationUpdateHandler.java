package es.amplia.oda.connector.http.configuration;

import es.amplia.oda.connector.http.HttpConnector;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import java.util.Dictionary;
import java.util.Optional;

public class ConnectorConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    static final String HOST_PROPERTY_NAME = "host";
    static final String PORT_PROPERTY_NAME = "port";
    static final String GENERAL_PATH_PROPERTY_NAME = "generalPath";
    static final String COLLECTION_PATH_PROPERTY_NAME = "collectionPath";
    static final String COMPRESSION_ENABLED_PROPERTY_NAME = "compressionEnabled";
    static final String COMPRESSION_THRESHOLD_PROPERTY_NAME = "compressionThreshold";


    private final HttpConnector connector;

    private ConnectorConfiguration currentConfiguration;


    public ConnectorConfigurationUpdateHandler(HttpConnector connector) {
        this.connector = connector;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        ConnectorConfiguration.ConnectorConfigurationBuilder builder = ConnectorConfiguration.builder();

        Optional.ofNullable((String) props.get(HOST_PROPERTY_NAME)).ifPresent(builder::host);
        Optional.ofNullable((String) props.get(PORT_PROPERTY_NAME)).ifPresent(value -> builder.port(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(GENERAL_PATH_PROPERTY_NAME)).ifPresent(builder::generalPath);
        Optional.ofNullable((String) props.get(COLLECTION_PATH_PROPERTY_NAME)).ifPresent(builder::collectionPath);
        Optional.ofNullable((String) props.get(COMPRESSION_ENABLED_PROPERTY_NAME)).ifPresent(value ->
                builder.compressionEnabled(Boolean.valueOf(value)));
        Optional.ofNullable((String) props.get(COMPRESSION_THRESHOLD_PROPERTY_NAME)).ifPresent(value ->
                builder.compressionThreshold(Integer.parseInt(value)));

        currentConfiguration = builder.build();
    }

    @Override
    public void applyConfiguration() {
        if (currentConfiguration != null) {
            connector.loadConfiguration(currentConfiguration);
        }
    }
}
