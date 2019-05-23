package es.amplia.oda.comms.mqtt.api;

public interface MqttClient {
    void connect() throws MqttException;
    void connect(MqttConnectOptions options) throws MqttException;
    void subscribe(String topic, MqttMessageListener listener) throws MqttException;
    void unsubscribe(String topic) throws MqttException;
    void publish(String topic, MqttMessage message) throws MqttException;
    boolean isConnected();
    void disconnect() throws MqttException;
}
