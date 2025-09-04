package es.amplia.oda.comms.mqtt.paho;

import es.amplia.oda.comms.mqtt.api.MqttMessage;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

class ResubscribeTopicsOnReconnectCallback implements MqttCallbackExtended {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResubscribeTopicsOnReconnectCallback.class);


    private IMqttAsyncClient innerClient;
    private final Map<String, IMqttMessageListener> subscribedListeners = new HashMap<>();


    public void listenTo(IMqttAsyncClient innerClient) {
        this.innerClient = innerClient;
        innerClient.setCallback(this);
    }

    public void addSubscribedTopic(String topic, IMqttMessageListener listener) {
        LOGGER.debug("Subscribing to {} topic", topic);
        subscribedListeners.put(topic, listener);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverUri) {
        if (reconnect) LOGGER.info("Reconnection completed to {}", serverUri);
        else LOGGER.info("Connection completed to {}", serverUri);
        subscribedListeners.forEach(
                (topic, listener) -> {
                    try {
                        innerClient.subscribe(topic, 2, null, new IMqttActionListener() {

                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                ((MqttPahoMessageListener)listener).getMqttMessageListener().onSuccess();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                ((MqttPahoMessageListener)listener).getMqttMessageListener().onFailure(exception);
                            }
                            
                        }, listener);
                    } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                        LOGGER.warn("Error subscribing to topic {} after reconnection", topic);
                    }
                }
        );
        /*if (reconnect) {
            LOGGER.info("Reconnection completed to {}", serverUri);
            subscribedListeners.forEach(
                    (topic, listener) -> {
                        try {
                            innerClient.subscribe(topic, 0, listener);
                        } catch (org.eclipse.paho.client.mqttv3.MqttException e) {
                            LOGGER.warn("Error subscribing to topic {} after reconnection", topic);
                        }
                    }
            );
        }*/
    }

    @Override
    public void connectionLost(Throwable cause) {
        LOGGER.warn("Connection with MQTT broker '{}' lost", innerClient.getServerURI(), cause);
    }

    @Override
    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
        LOGGER.debug("Message arrived to topic {}: {}", topic,
                MqttMessage.newInstance(message.getPayload(), message.getQos(), message.isRetained()));
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        LOGGER.debug("Delivery complete to topics {}", (Object) token.getTopics());
    }
}
