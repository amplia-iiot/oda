package es.amplia.oda.connector.mqtt;

import es.amplia.oda.connector.mqtt.configuration.ConnectorConfiguration;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;

import lombok.Value;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class MqttConnector implements IMqttMessageListener, OpenGateConnector, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttConnector.class);

    private final Dispatcher dispatcher;

    private String iotTopic;
    private String responsesTopic;
    private int qos;
    private boolean retained;
    private MqttClient client;

    MqttConnector(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.client = null;
    }

    public void loadConfigurationAndInit(ConnectorConfiguration configuration) throws MqttException {
        close();

        iotTopic = configuration.getQueuesConfiguration().getIotQueue();
        responsesTopic = configuration.getQueuesConfiguration().getResponseQueue();
        qos = configuration.getQueuesConfiguration().getQualityOfService();
        retained = configuration.getQueuesConfiguration().isRetained();
        MqttConnectOptions options = configuration.getMqttConnectOptions();
        client = new MqttClient(configuration.getBrokerUrl(), configuration.getClientId(), new MemoryPersistence());
        client.connect(options);
        client.setCallback(new MqttTraceEvents());
        client.subscribe(configuration.getQueuesConfiguration().getRequestQueue(), configuration.getQueuesConfiguration().getQualityOfService(), this);
        LOGGER.info("Reconnected to {} as {} for topic {}", configuration.getBrokerUrl(), configuration.getClientId(), configuration.getQueuesConfiguration().getRequestQueue());
    }


    @Override
    public void messageArrived(String topic, MqttMessage message) {
        LOGGER.info("Messaged arrived: {},{}", topic, message);
        CompletableFuture<byte[]> response = dispatcher.process(message.getPayload());
        if (response == null) {
            LOGGER.warn("Cannot process message as Dispatcher is not present");
            return;
        }
        response.thenAccept(responseBytes -> sendMessage(responsesTopic, responseBytes));
    }

    private void sendMessage(String topic, byte[] payload) {
        if (client == null) {
            LOGGER.warn("Cannot send message as we are disconnected from Mqtt");
        } else if (payload == null) {
            LOGGER.warn("Cannot send message as payload is null");
        } else {
            Message message = new Message(payload);
            LOGGER.info("Sending message: {}, {}", topic, message);
            try {
                client.publish(topic, message.getPayload(), qos, retained);
            } catch (MqttException e) {
                LOGGER.warn("Error sending response: {}", e.getMessage());
            }
        }
    }

    @Override
    public void uplink(byte[] payload) {
        sendMessage(iotTopic, payload);
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    @Override
    public void close() {
        if (client == null) {
            return;
        }

        LOGGER.info("Releasing MQTT entities");
        try {
            client.disconnect();
        } catch (MqttException exception) {
            LOGGER.error("Error disconnecting MQTT connector: ", exception);
        }
        try {
            client.close();
        } catch (MqttException exception) {
            LOGGER.error("Error closing MQTT connector: ", exception);
        }
        client = null;
    }

    @Value
    private static class Message {
        private byte[] payload;

        @Override
        public String toString() {
            return new String(payload);
        }
    }

    private static class MqttTraceEvents implements MqttCallback {
        @Override
        public void connectionLost(Throwable cause) {
            LOGGER.warn("Connection lost with server: ", cause);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            // message processed by parent class MqttConnector
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            LOGGER.info("Delivery complete of {}", token);
        }
    }


}
