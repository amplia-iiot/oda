package es.amplia.oda.connector.mqtt.configuration;

import es.amplia.oda.connector.mqtt.MqttConnector;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Optional;

/**
 * MQTT connector configuration update handler implementing {@link ConfigurationUpdateHandler}
 */
public class ConfigurationUpdateHandlerImpl implements ConfigurationUpdateHandler, ServiceListener {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUpdateHandlerImpl.class);

    /**
     * MQTT Connector to configure.
     */
    private MqttConnector connector;

    /**
     * Device Identifier provider service tracker to use the device identifier to configure the connection properly.
     */
    private DeviceInfoProvider deviceInfoProvider;

    /**
     * Last properties to configure the MQTT connector.
     */
    private Dictionary<String, ?> lastProperties;

    /**
     * MQTT connector configuration
     */
    private ConnectorConfiguration currentConfiguration;


    /**
     * Constructor.
     *
     * @param connector2         MQTT connector to configure.
     * @param deviceInfoProvider Device identifier provider service tracker.
     */
    public ConfigurationUpdateHandlerImpl(MqttConnector connector2, DeviceInfoProvider deviceInfoProvider) {
        this.connector = connector2;
        this.deviceInfoProvider = deviceInfoProvider;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        lastProperties = props;

        String deviceId = deviceInfoProvider.getDeviceId();
        String apiKey = deviceInfoProvider.getApiKey();
        if (deviceId == null || apiKey == null) {
            throw new ConfigurationException("Can not find device identifier and API Key");
        }

        ConnectorConfigurationBuilder builder = ConnectorConfigurationBuilder.newBuilder();

        try {
            // Connector configuration
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_HOST_PROPERTY))
                    .ifPresent(builder::setHost);
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_PORT_PROPERTY))
                    .ifPresent(value -> builder.setPort(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_SECURE_PORT_PROPERTY))
                    .ifPresent(value -> builder.setSecurePort(Integer.parseInt(value)));
            builder.setClientId(deviceId);
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_SECURE_CONNECTION_PROPERTY))
                    .ifPresent(value -> builder.setSecureConnection(Boolean.parseBoolean(value)));

            // Connection configuration
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_MQTT_VERSION_PROPERTY))
                    .ifPresent(value -> builder.setMqttVersion(parseMqttVersion(value)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_AUTOMATIC_RECONNECT_PROPERTY))
                    .ifPresent(value -> builder.setAutomaticReconnect(Boolean.parseBoolean(value)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_CONNECTION_TIMEOUT_PROPERTY))
                    .ifPresent(value -> builder.setConnectionTimeout(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_KEEP_ALIVE_INTERVAL_PROPERTY))
                    .ifPresent(value -> builder.setKeepAliveInterval(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_MAX_IN_FLIGHT_PROPERTY))
                    .ifPresent(value -> builder.setMaxInflight(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_CLEAN_SESSION_PROPERTY))
                    .ifPresent(value -> builder.setCleanSession(Boolean.parseBoolean(value)));
            builder.setUserName(deviceId);
            builder.setPassword(apiKey);

            // LWT Configuration
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_LWT_TOPIC_PROPERTY))
                    .ifPresent(value -> builder.setLwtTopic(getTopic(value, deviceId)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_LWT_PAYLOAD_PROPERTY))
                    .ifPresent(builder::setLwtPayload);
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_LWT_QOS_PROPERTY))
                    .ifPresent(value -> builder.setLwtQualityOfService(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_LWT_RETAINED_PROPERTY))
                    .ifPresent(value -> builder.setLwtRetained(Boolean.parseBoolean(value)));

            // SSL Configuration
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_KEYSTORE_PATH_PROPERTY))
                    .ifPresent(builder::setKeyStorePath);
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_KEYSTORE_TYPE_PROPERTY))
                    .ifPresent(builder::setKeyStoreType);
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_KEYSTORE_PASSWORD_PROPERTY))
                    .ifPresent(builder::setKeyStorePassword);

            // Queues configuration
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_REQUEST_QUEUE_PROPERTY))
                    .ifPresent(value -> builder.setRequestQueue(getTopic(value, deviceId)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_RESPONSE_QUEUE_PROPERTY))
                    .ifPresent(value -> builder.setResponseQueue(getTopic(value, deviceId)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_IOT_QUEUE_PROPERTY))
                    .ifPresent(value -> builder.setIotQueue(getTopic(value, deviceId)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_QOS_PROPERTY))
                    .ifPresent(value -> builder.setQualityOfService(Integer.parseInt(value)));
            Optional.ofNullable((String) props.get(ConnectorConfiguration.MQTT_CONNECTOR_RETAINED_PROPERTY))
                    .ifPresent(value -> builder.setRetained(Boolean.parseBoolean(value)));
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Error parsing configuration properties: " + e);
        }

        currentConfiguration = builder.build();
    }

    /**
     * Parse the MQTT version.
     *
     * @param value MQTT version as String to parse.
     * @return MQTT version as int according to MqttConnectOptions.
     * @throws ConfigurationException Exception parsing MQTT version String.
     */
    private int parseMqttVersion(String value) throws ConfigurationException {
        if (value.equals(ConnectorConfiguration.MQTT_VERSION_3_1))
            return MqttConnectOptions.MQTT_VERSION_3_1;
        else if (value.equals(ConnectorConfiguration.MQTT_VERSION_3_1_1))
            return MqttConnectOptions.MQTT_VERSION_3_1_1;
        throw new ConfigurationException("Unsupported MQTT version");
    }

    /**
     * Get the resulting topic.
     *
     * @param baseTopic Base topic.
     * @param deviceId  Device identifier.
     * @return Resulting topic.
     */
    private String getTopic(String baseTopic, String deviceId) {
        return baseTopic + "/" + deviceId;
    }

    @Override
    public void applyConfiguration() throws Exception {
        connector.loadConfigurationAndInit(currentConfiguration);
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        if (lastProperties != null) {
            try {
                loadConfiguration(lastProperties);
                applyConfiguration();
            } catch (Exception e) {
                logger.error("Can not reload MQTT connector configuration after DeviceInfoProvider service change");
            }
        }
    }
}
