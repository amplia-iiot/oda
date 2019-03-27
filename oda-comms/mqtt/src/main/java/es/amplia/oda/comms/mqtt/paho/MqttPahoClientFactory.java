package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttException;

public class MqttPahoClientFactory implements MqttClientFactory {

    @Override
    public MqttClient createMqttClient(String serverUri, String clientId) throws MqttException {
        try {
            org.eclipse.paho.client.mqttv3.IMqttClient innerClient =
                    new org.eclipse.paho.client.mqttv3.MqttClient(serverUri, clientId);
            return new MqttPahoClient(innerClient);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e.getMessage(), e);
        }
    }
}
