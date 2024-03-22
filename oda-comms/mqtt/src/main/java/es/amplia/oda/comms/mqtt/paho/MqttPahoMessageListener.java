package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;

@Slf4j
class MqttPahoMessageListener implements IMqttMessageListener {

    private final MqttMessageListener mqttMessageListener;
    private final IMqttClient innerMqttClient;

    MqttPahoMessageListener(MqttMessageListener mqttMessageListener, IMqttClient mqttClient) {
        this.mqttMessageListener = mqttMessageListener;
        this.innerMqttClient = mqttClient;
    }

    @Override
    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
        // send ACK message received
        try {
            innerMqttClient.messageArrivedComplete(message.getId(), 1);
        } catch (MqttException e) {
            log.error("Error sending message received ACK. Message id = {}",message.getId());
        }

        // process message arrived
        mqttMessageListener.messageArrived(topic,
                MqttMessage.newInstance(message.getPayload(), message.getQos(), message.isRetained()));
    }
}
