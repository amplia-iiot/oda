package es.amplia.oda.connector.mqtt.configuration;

import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;
import lombok.Value;

@Value
public class ConnectorConfiguration {
    String brokerUrl;
    String clientId;
    MqttConnectOptions connectOptions;
    String iotTopic;
    String requestTopic;
    String responseTopic;
    int qos;
    boolean retained;
    int initialDelay;
    int retryDelay;
    boolean hasMaxlength;
    int maxlength;
}
