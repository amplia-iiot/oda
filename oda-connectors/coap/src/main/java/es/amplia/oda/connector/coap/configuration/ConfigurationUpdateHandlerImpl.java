package es.amplia.oda.connector.coap.configuration;

import es.amplia.oda.connector.coap.COAPConnector;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import java.util.Dictionary;
import java.util.Optional;

import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.ConnectorType;

public class ConfigurationUpdateHandlerImpl implements ConfigurationUpdateHandler {

    static final String CONNECTOR_TYPE_PROPERTY_NAME = "type";
    static final String REMOTE_HOST_PROPERTY_NAME = "remoteHost";
    static final String REMOTE_PORT_PROPERTY_NAME = "port";
    static final String PATH_PROPERTY_NAME = "path";
    static final String PROVISION_PATH_PROPERTY_NAME = "provisionPath";
    static final String TIMEOUT_PROPERTY_NAME = "timeout";
    static final String MESSAGE_PROTOCOL_VERSION_PROPERTY_NAME = "messageProtocolVersion";
    static final String LOCAL_PORT_PROPERTY_NAME = "localPort";

    private final COAPConnector connector;

    private ConnectorConfiguration currentConfiguration;


    public ConfigurationUpdateHandlerImpl(COAPConnector connector) {
        this.connector = connector;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        ConnectorConfiguration.ConnectorConfigurationBuilder builder = ConnectorConfiguration.builder();
        // Required configuration
        builder.remoteHost((String) props.get(REMOTE_HOST_PROPERTY_NAME));
        builder.path((String) props.get(PATH_PROPERTY_NAME));
        builder.provisionPath((String) props.get(PROVISION_PATH_PROPERTY_NAME));

        // Optional configuration
        Optional.ofNullable((String) props.get(CONNECTOR_TYPE_PROPERTY_NAME))
                .ifPresent(value -> builder.type(ConnectorType.valueOf(value)));
        Optional.ofNullable((String) props.get(REMOTE_PORT_PROPERTY_NAME))
                .ifPresent(value -> builder.remotePort(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(LOCAL_PORT_PROPERTY_NAME))
                .ifPresent(value -> builder.localPort(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(TIMEOUT_PROPERTY_NAME))
                .ifPresent(value -> builder.timeout(Long.parseLong(value)));
        Optional.ofNullable((String) props.get(MESSAGE_PROTOCOL_VERSION_PROPERTY_NAME))
                .ifPresent(builder::messageProtocolVersion);

        currentConfiguration = builder.build();
    }

    @Override
    public void applyConfiguration() {
        connector.loadAndInit(currentConfiguration);
    }
}
