package es.amplia.oda.comms.mqtt.api;

public interface MqttMessageListener extends MqttActionListener {
    void messageArrived(String topic, MqttMessage mqttMessage);
}
