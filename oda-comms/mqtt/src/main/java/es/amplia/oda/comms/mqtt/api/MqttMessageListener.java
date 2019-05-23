package es.amplia.oda.comms.mqtt.api;

public interface MqttMessageListener {
    void messageArrived(String topic, MqttMessage mqttMessage);
}
