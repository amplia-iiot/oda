package es.amplia.oda.comms.mqtt.api;

public interface MqttClientFactory {
    MqttClient createMqttClient(String serverUri, String clientId) throws MqttException;
}
