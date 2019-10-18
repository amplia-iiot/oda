package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.*;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

class MqttPahoClient implements MqttClient {

    private final IMqttClient innerClient;

    MqttPahoClient(IMqttClient innerClient) {
        this.innerClient = innerClient;
    }

    @Override
    public void connect() {
        try {
            innerClient.connect();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e.getMessage(), e);
        }
    }

    @Override
    public void connect(MqttConnectOptions options) {
        try {
            org.eclipse.paho.client.mqttv3.MqttConnectOptions innerOptions = MqttPahoConnectOptionsMapper.from(options);
            innerClient.connect(innerOptions);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return innerClient.isConnected();
    }

    @Override
    public void publish(String topic, MqttMessage message) {
        try {
            innerClient.publish(topic, message.getPayload(), message.getQos(), message.isRetained());
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e.getMessage(), e);
        }
    }

    @Override
    public void subscribe(String topic, MqttMessageListener listener) {
        try {
            IMqttMessageListener pahoListener = new MqttPahoMessageListener(listener);
            innerClient.subscribe(topic, pahoListener);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e.getMessage(), e);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        try {
            innerClient.unsubscribe(topic);
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        try {
            innerClient.disconnect();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e.getMessage(), e);
        }
    }
}
