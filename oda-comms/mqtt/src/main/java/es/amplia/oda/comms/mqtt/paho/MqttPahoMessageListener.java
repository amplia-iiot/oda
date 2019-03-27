package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

class MqttPahoMessageListener implements IMqttMessageListener {

    private final MqttMessageListener mqttMessageListener;

    MqttPahoMessageListener(MqttMessageListener mqttMessageListener) {
        this.mqttMessageListener = mqttMessageListener;
    }

    @Override
    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
        mqttMessageListener.messageArrived(topic,
                MqttMessage.newInstance(message.getPayload(), message.getQos(), message.isRetained()));
    }
}
