package es.amplia.oda.connector.coap.configuration;

import es.amplia.oda.connector.coap.COAPConnector;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

import static es.amplia.oda.connector.coap.configuration.ConnectorConfiguration.*;

public class ConfigurationUpdateHandlerImpl implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationUpdateHandlerImpl.class);

    static final String CONNECTOR_TYPE_PROPERTY_NAME = "type";
    static final String HOST_PROPERTY_NAME = "host";
    static final String PORT_PROPERTY_NAME = "port";
    static final String PATH_PROPERTY_NAME = "path";
    static final String PROVISION_PATH_PROPERTY_NAME = "provisionPath";
    static final String TIMEOUT_PROPERTY_NAME = "timeout";
    static final String MESSAGE_PROTOCOL_VERSION_PROPERTY_NAME = "messageProtocolVersion";
    static final String LOCAL_PORT_PROPERTY_NAME = "localPort";
    static final String KEY_STORE_TYPE_PROPERTY_NAME = "keyStoreType";
    static final String KEY_STORE_LOCATION_PROPERTY_NAME = "keyStoreLocation";
    static final String KEY_STORE_PASS_PROPERTY_NAME = "keyStorePassword";
    static final String CLIENT_KEY_ALIAS_PROPERTY_NAME = "clientKeyAlias";
    static final String TRUST_STORE_TYPE_PROPERTY_NAME = "trustStoreType";
    static final String TRUST_STORE_LOCATION_PROPERTY_NAME = "trustStoreLocation";
    static final String TRUST_STORE_PASS_PROPERTY_NAME = "trustStorePassword";
    static final String TRUSTED_CERTIFICATES_PROPERTY_NAME = "trustedCertificates";

    static final String TRUSTED_CERTIFICATES_SEPARATOR = ",";

    private final COAPConnector connector;

    private ConnectorConfiguration currentConfiguration;


    public ConfigurationUpdateHandlerImpl(COAPConnector connector) {
        this.connector = connector;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading CoAP connector configuration");

        ConnectorConfigurationBuilder builder = ConnectorConfiguration.builder();
        builder.scheme(COAP_SCHEME);
        builder.port(DEFAULT_COAP_PORT);

        builder.host((String) props.get(HOST_PROPERTY_NAME));
        builder.path((String) props.get(PATH_PROPERTY_NAME));
        builder.provisionPath((String) props.get(PROVISION_PATH_PROPERTY_NAME));

        Optional.ofNullable((String) props.get(CONNECTOR_TYPE_PROPERTY_NAME))
                .ifPresent(value -> setConnectorType(ConnectorType.valueOf(value), builder));
        Optional.ofNullable((String) props.get(PORT_PROPERTY_NAME))
                .ifPresent(value -> builder.port(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(LOCAL_PORT_PROPERTY_NAME))
                .ifPresent(value -> builder.localPort(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(TIMEOUT_PROPERTY_NAME))
                .ifPresent(value -> builder.timeout(Long.parseLong(value)));
        Optional.ofNullable((String) props.get(MESSAGE_PROTOCOL_VERSION_PROPERTY_NAME))
                .ifPresent(builder::messageProtocolVersion);

        Optional.ofNullable((String) props.get(KEY_STORE_TYPE_PROPERTY_NAME)).ifPresent(builder::keyStoreType);
        Optional.ofNullable((String) props.get(KEY_STORE_LOCATION_PROPERTY_NAME)).ifPresent(builder::keyStoreLocation);
        Optional.ofNullable((String) props.get(KEY_STORE_PASS_PROPERTY_NAME)).ifPresent(builder::keyStorePassword);
        Optional.ofNullable((String) props.get(CLIENT_KEY_ALIAS_PROPERTY_NAME)).ifPresent(builder::clientKeyAlias);
        Optional.ofNullable((String) props.get(TRUST_STORE_TYPE_PROPERTY_NAME)).ifPresent(builder::trustStoreType);
        Optional.ofNullable((String) props.get(TRUST_STORE_LOCATION_PROPERTY_NAME))
                .ifPresent(builder::trustStoreLocation);
        Optional.ofNullable((String) props.get(TRUST_STORE_PASS_PROPERTY_NAME))
                .ifPresent(builder::trustStorePassword);
        Optional.ofNullable((String) props.get(TRUSTED_CERTIFICATES_PROPERTY_NAME))
                .ifPresent(value -> builder.trustedCertificates(value.split(TRUSTED_CERTIFICATES_SEPARATOR)));

        currentConfiguration = builder.build();

        validateCurrentConfiguration();

        LOGGER.info("CoAP connector configuration loaded");
    }

    private void setConnectorType(ConnectorType connectorType, ConnectorConfigurationBuilder builder) {
        if (isDtlsConnectorType(connectorType)) {
            builder.scheme(COAP_SECURE_SCHEME);
            builder.port(DEFAULT_COAP_SECURE_PORT);
        }
        builder.type(connectorType);
    }

    private void validateCurrentConfiguration() {
        if (isDtlsConnectorType(currentConfiguration.getType()) &&
                (keyStoreIsNotConfigured() || trustStoreIsNotConfigured())) {
            currentConfiguration = null;
            throw new ConfigurationException("CoAP connector invalid configuration: Missing parameters for DTLS connector type");
        }
    }

    private boolean trustStoreIsNotConfigured() {
        return currentConfiguration.getTrustStoreLocation() == null ||
                currentConfiguration.getTrustStorePassword() == null ||
                currentConfiguration.getTrustedCertificates() == null;
    }

    private boolean keyStoreIsNotConfigured() {
        return currentConfiguration.getKeyStoreLocation() == null ||
                currentConfiguration.getKeyStorePassword() == null;
    }

    private boolean isDtlsConnectorType(ConnectorType type) {
        return type.equals(ConnectorType.DTLS);
    }

    @Override
    public void applyConfiguration() {
        LOGGER.info("Applying CoAP connector configuration");
        if (currentConfiguration != null) {
            connector.loadAndInit(currentConfiguration);
        }
        LOGGER.info("CoAP connector configuration applied");
    }
}
