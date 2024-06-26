package es.amplia.oda.comms.mqtt.api;

import es.amplia.oda.core.commons.entities.ContentType;

public interface MqttClient {
    void connect();
    void connect(MqttConnectOptions options, MqttActionListener listener);
    void subscribe(String topic, MqttMessageListener listener);
    void unsubscribe(String topic);
    void publish(String topic, MqttMessage message, ContentType contentType);
    boolean isConnected();
    void disconnect();
}
