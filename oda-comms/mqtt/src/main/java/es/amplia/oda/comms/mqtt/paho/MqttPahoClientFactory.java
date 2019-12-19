package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttException;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPahoClientFactory implements MqttClientFactory {

    @Override
    public MqttClient createMqttClient(String serverUri, String clientId) {
        try {
            IMqttClient innerClient =
                    new org.eclipse.paho.client.mqttv3.MqttClient(serverUri, clientId, new MemoryPersistence());
            ResubscribeTopicsOnReconnectCallback resubscribeCallback = new ResubscribeTopicsOnReconnectCallback();
            return new MqttPahoClient(innerClient, resubscribeCallback);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e.getMessage(), e);
        }
    }
}
