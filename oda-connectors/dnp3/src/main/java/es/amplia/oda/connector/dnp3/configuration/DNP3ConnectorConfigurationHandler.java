package es.amplia.oda.connector.dnp3.configuration;

import com.automatak.dnp3.DNP3Exception;
import es.amplia.oda.connector.dnp3.DNP3Connector;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

public class DNP3ConnectorConfigurationHandler implements ConfigurationUpdateHandler {

    private static final Logger logger = LoggerFactory.getLogger(DNP3ConnectorConfigurationHandler.class);

    static final String CHANNEL_IDENTIFIER_PROPERTY_NAME = "channelIdentifier";
    static final String OUTSTATION_IDENTIFIER_PROPERTY_NAME = "outstationIdentifier";
    static final String IP_ADDRESS_PROPERTY_NAME = "ipAddress";
    static final String IP_PORT_PROPERTY_NAME = "ipPort";
    static final String LOCAL_DEVICE_DNP_ADDRESS_PROPERTY_NAME = "localDeviceDnpAddress";
    static final String REMOTE_DEVICE_DNP_ADDRESS_PROPERTY_NAME = "remoteDeviceDnpAddress";
    static final String UNSOLICITED_RESPONSE_PROPERTY_NAME = "unsolicitedResponse";
    static final String EVENT_BUFFER_SIZE_PROPERTY_NAME = "eventBufferSize";
    static final String LOG_LEVEL_PROPERTY_NAME = "logLevel";
    static final String ENABLE_PROPERTY_NAME = "enable";

    private final DNP3Connector connector;

    private DNP3ConnectorConfiguration currentConfiguration;

    public DNP3ConnectorConfigurationHandler(DNP3Connector connector) {
        this.connector = connector;
    }

    @Override
    public void loadDefaultConfiguration() {
        logger.info("Loading default configuration");
        currentConfiguration = DNP3ConnectorConfiguration.builder().build();
        logger.info("Loaded default configuration");
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        logger.info("Loading new configuration");

        DNP3ConnectorConfiguration.DNP3ConnectorConfigurationBuilder builder = DNP3ConnectorConfiguration.builder();

        Optional.ofNullable((String) props.get(CHANNEL_IDENTIFIER_PROPERTY_NAME)).ifPresent(builder::channelIdentifier);
        Optional.ofNullable((String) props.get(OUTSTATION_IDENTIFIER_PROPERTY_NAME))
                .ifPresent(builder::outstationIdentifier);
        Optional.ofNullable((String) props.get(IP_ADDRESS_PROPERTY_NAME)).ifPresent(builder::ipAddress);
        Optional.ofNullable((String) props.get(IP_PORT_PROPERTY_NAME))
                .ifPresent(value -> builder.ipPort(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(LOCAL_DEVICE_DNP_ADDRESS_PROPERTY_NAME))
                .ifPresent(value -> builder.localDeviceDNP3Address(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(REMOTE_DEVICE_DNP_ADDRESS_PROPERTY_NAME))
                .ifPresent(value -> builder.remoteDeviceDNP3Address(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(UNSOLICITED_RESPONSE_PROPERTY_NAME))
                .ifPresent(value -> builder.unsolicitedResponse(Boolean.parseBoolean(value)));
        Optional.ofNullable((String) props.get(EVENT_BUFFER_SIZE_PROPERTY_NAME))
                .ifPresent(value -> builder.eventBufferSize(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(LOG_LEVEL_PROPERTY_NAME))
                .ifPresent(value -> builder.logLevel(Integer.parseInt(value)));
        Optional.ofNullable((String) props.get(ENABLE_PROPERTY_NAME))
                .ifPresent(value -> builder.enable(Boolean.parseBoolean(value)));

        currentConfiguration = builder.build();

        logger.info("New configuration loaded");
    }

    @Override
    public void applyConfiguration() {
        try {
            logger.info("Applying last configuration");
            connector.loadConfiguration(currentConfiguration);

            if (currentConfiguration.isEnable()) {
                connector.init();
                logger.info("Connector is configured and enabled");
            } else {
                logger.info("Connector is configured but disabled");
            }
            logger.info("Last configuration applied");
        } catch (DNP3Exception e) {
            throw new IllegalArgumentException("Invalid configuration for DNP3 Connector", e);
        }
    }
}
