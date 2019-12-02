package es.amplia.oda.comms.mqtt.api;

public interface MqttClient {
    void connect();
    void connect(MqttConnectOptions options);
    void subscribe(String topic, MqttMessageListener listener);
    void unsubscribe(String topic);
    void publish(String topic, MqttMessage message);
    boolean isConnected();
    void disconnect();
}
