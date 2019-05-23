package es.amplia.oda.connector.mqtt.configuration;

import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;
import lombok.Value;

@Value
public class ConnectorConfiguration {
    private String brokerUrl;
    private String clientId;
    private MqttConnectOptions connectOptions;
    private String iotTopic;
    private String requestTopic;
    private String responseTopic;
    private int qos;
    private boolean retained;
}
