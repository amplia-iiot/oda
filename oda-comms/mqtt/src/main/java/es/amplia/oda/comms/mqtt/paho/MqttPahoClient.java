package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.*;
import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttConnectOptions;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;

import es.amplia.oda.core.commons.entities.ContentType;
import org.eclipse.paho.client.mqttv3.*;

class MqttPahoClient implements MqttClient {

    private final IMqttClient innerClient;
    private final ResubscribeTopicsOnReconnectCallback resubscribeTopicsOnReconnectCallback;


    MqttPahoClient(IMqttClient innerClient, ResubscribeTopicsOnReconnectCallback resubscribedTopicsCallback) {
        this.innerClient = innerClient;
        this.resubscribeTopicsOnReconnectCallback = resubscribedTopicsCallback;
    }

    @Override
    public void connect() {
        try {
            resubscribeTopicsOnReconnectCallback.listenTo(innerClient);
            innerClient.connect();
        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
            throw new MqttException(e.getMessage(), e);
        }
    }

    @Override
    public void connect(MqttConnectOptions options) {
        try {
            org.eclipse.paho.client.mqttv3.MqttConnectOptions innerOptions = MqttPahoConnectOptionsMapper.from(options);
            resubscribeTopicsOnReconnectCallback.listenTo(innerClient);
            innerClient.connect(innerOptions);
        } catch (Throwable e) {
            throw new MqttException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isConnected() {
        return innerClient.isConnected();
    }

    @Override
    public void publish(String topic, MqttMessage message, ContentType contentType) {
        try {
            // Not supported in MQTT v3. Available through MqttProperties in MQTT v5
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
            resubscribeTopicsOnReconnectCallback.addSubscribedTopic(topic, pahoListener);
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
