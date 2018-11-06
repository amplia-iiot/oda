package es.amplia.oda.connector.thingstream.configuration;

import com.myriadgroup.iot.sdk.client.message.MessageClientException;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.connector.thingstream.ThingstreamConnector;

import java.util.Dictionary;
import java.util.Optional;

import static es.amplia.oda.connector.thingstream.configuration.ConnectorConfiguration.*;

public class ConfigurationUpdateHandlerImpl implements ConfigurationUpdateHandler {

    static final String CLIENT_TYPE_PROPERTY_NAME = "clientType";
    static final String DATA_TOPIC_PROPERTY_NAME = "topic";
    static final String OPERATION_TOPIC_PROPERTY_NAME = "operationTopic";
    static final String QOS_PROPERTY_NAME = "qos";
    static final String MQTTSN_CLIENT_ID_PROPERTY_NAME = "clientId";
    static final String MQTTSN_SHORT_CODE_PROPERTY_NAME = "shortCode";
    static final String MQTT_HOST_PROPERTY_NAME = "mqttHost";
    static final String MQTT_PORT_PROPERTY_NAME = "mqttPort";
    static final String MQTT_CLIENT_ID_PROPERTY_NAME = "mqttClientId";
    static final String MQTT_USERNAME_PROPERTY_NAME = "mqttUsername";
    static final String MQTT_PASSWORD_PROPERTY_NAME = "mqttPassword";

    private final ThingstreamConnector connector;

    private ConnectorConfiguration currentConfiguration;

    public ConfigurationUpdateHandlerImpl(ThingstreamConnector connector) {
        this.connector = connector;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        ConnectorConfigurationBuilder builder = ConnectorConfiguration.builder();

        try {
            // Connector es.amplia.oda.connector.thingstream.configuration
            builder.clientType(ClientType.parse((String) props.get(CLIENT_TYPE_PROPERTY_NAME)));
            builder.dataTopic((String) props.get(DATA_TOPIC_PROPERTY_NAME));
            Optional.ofNullable((String) props.get(OPERATION_TOPIC_PROPERTY_NAME)).ifPresent(builder::operationTopic);
            builder.qos(QOS.parse((String) props.get(QOS_PROPERTY_NAME)));

            // MQTT-SN es.amplia.oda.connector.thingstream.configuration
            Optional.ofNullable((String) props.get(MQTTSN_CLIENT_ID_PROPERTY_NAME)).ifPresent(builder::clientId);
            Optional.ofNullable((String) props.get(MQTTSN_SHORT_CODE_PROPERTY_NAME))
                    .ifPresent(value -> builder.shortCode(Integer.parseInt(value)));

            // MQTT es.amplia.oda.connector.thingstream.configuration
            Optional.ofNullable((String) props.get(MQTT_HOST_PROPERTY_NAME)).ifPresent(builder::mqttHost);
            Optional.ofNullable((String) props.get(MQTT_PORT_PROPERTY_NAME))
                    .ifPresent(value -> builder.mqttPort(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(MQTT_CLIENT_ID_PROPERTY_NAME)).ifPresent(builder::mqttClientId);
            Optional.ofNullable((String) props.get(MQTT_USERNAME_PROPERTY_NAME)).ifPresent(builder::mqttUsername);
            Optional.ofNullable((String) props.get(MQTT_PASSWORD_PROPERTY_NAME)).ifPresent(builder::mqttPassword);

            currentConfiguration = builder.build();
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Error parsing es.amplia.oda.connector.thingstream.configuration properties: " + e.getMessage());
        }
    }

    @Override
    public void applyConfiguration() {
        if (currentConfiguration != null) {
            try {
                connector.loadConfigurationAndInit(currentConfiguration);
            } catch (MessageClientException e) {
                throw new ConfigurationException(e.getMessage());
            }
        }
    }
}
