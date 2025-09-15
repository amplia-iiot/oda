package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttException;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPahoClientFactory implements MqttClientFactory {

    @Override
    public MqttClient createMqttClient(String serverUri, String clientId) {
        try {
            IMqttAsyncClient innerClient =
                    new org.eclipse.paho.client.mqttv3.MqttAsyncClient(serverUri, clientId, new MemoryPersistence());
            ResubscribeTopicsOnReconnectCallback resubscribeCallback = new ResubscribeTopicsOnReconnectCallback();
            return new MqttPahoClient(innerClient, resubscribeCallback);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e.getMessage(), e, e.getReasonCode());
        }
    }
}
