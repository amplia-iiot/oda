package es.amplia.oda.connector.thingstream.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ConnectorConfiguration {

    public static final int DEFAULT_MQTT_PORT = 1883;

    public enum ClientType {
        MQTTSN, MQTT;

        public static ClientType parse(String value) {
            if (value.equalsIgnoreCase("usb") || value.equalsIgnoreCase("serial") || value.equalsIgnoreCase("mqtt-sn"))
                return MQTTSN;
            else if (value.equalsIgnoreCase("internet") || value.equalsIgnoreCase("mqtt"))
                return MQTT;
            else
                throw new IllegalArgumentException("Unknown client type");
        }
    }

    public enum QOS {
        QOS_0, QOS_1, QOS_2;

        public static QOS parse(String value) {
            if (value.equalsIgnoreCase("0") || value.equalsIgnoreCase("qos_0"))
                return QOS_0;
            else if (value.equalsIgnoreCase("1") || value.equalsIgnoreCase("qos_1"))
                return QOS_1;
            else if (value.equalsIgnoreCase("2") || value.equalsIgnoreCase("qos_2"))
                return QOS_2;
            else
                throw new IllegalArgumentException("Unknown client type");
        }
    }

    @NonNull
    private ClientType clientType;
    @NonNull
    private String dataTopic;
    private String operationTopic;
    @NonNull
    private QOS qos;

    private String clientId;
    private int shortCode;

    private String mqttHost;
    private int mqttPort;
    private String mqttClientId;
    private String mqttUsername;
    private String mqttPassword;

    public static ConnectorConfigurationBuilder builder() {
        return new CheckedConnectorConfigurationBuilder();
    }

    public static class CheckedConnectorConfigurationBuilder extends ConnectorConfigurationBuilder {

        CheckedConnectorConfigurationBuilder() {
            // Set default values
            super.operationTopic = "";
            super.mqttPort = DEFAULT_MQTT_PORT;
        }

        @Override
        public ConnectorConfiguration build() {
            checkMandatoryFields();
            checkConfiguredFields();
            return super.build();
        }

        private void checkMandatoryFields() {
            if (super.clientType == null || super.dataTopic == null || super.qos == null)
                throw new IllegalArgumentException("Invalid connector es.amplia.oda.connector.thingstream.configuration: Mandatory fields missing");
        }

        private void checkConfiguredFields() {
            if (!isMQTTConfigured() && !isMQTTSNConfigured())
                throw new IllegalArgumentException("Invalid connector es.amplia.oda.connector.thingstream.configuration");
        }

        private boolean isMQTTSNConfigured() {
            return super.clientType == ClientType.MQTTSN && super.clientId != null && super.shortCode != 0;
        }

        private boolean isMQTTConfigured() {
            return super.clientType == ClientType.MQTT && super.mqttHost != null && super.mqttPort != 0 &&
                    super.mqttClientId != null && super.mqttUsername != null && super.mqttPassword != null;
        }
    }
}
