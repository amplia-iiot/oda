package es.amplia.oda.connector.mqtt;

import es.amplia.oda.comms.mqtt.api.*;
import es.amplia.oda.connector.mqtt.configuration.ConnectorConfiguration;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class MqttConnector implements MqttMessageListener, OpenGateConnector, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttConnector.class);

    private final MqttClientFactory mqttClientFactory;
    private final Dispatcher dispatcher;

    private String iotTopic;
    private String responseTopic;
    private int qos;
    private boolean retained;
    private MqttClient client;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;

    MqttConnector(MqttClientFactory mqttClientFactory, Dispatcher dispatcher) {
        this.mqttClientFactory = mqttClientFactory;
        this.dispatcher = dispatcher;
        this.client = null;
    }

    public void loadConfigurationAndInit(ConnectorConfiguration connectorConfiguration)
            throws MqttException {
        close();
        client = mqttClientFactory.createMqttClient(connectorConfiguration.getBrokerUrl(),
                connectorConfiguration.getClientId());
        scheduledFuture = executorService.scheduleWithFixedDelay(() -> connect(connectorConfiguration),
                connectorConfiguration.getInitialDelay(), connectorConfiguration.getRetryDelay(), TimeUnit.SECONDS);
        this.iotTopic = connectorConfiguration.getIotTopic();
        this.responseTopic = connectorConfiguration.getResponseTopic();
        this.qos = connectorConfiguration.getQos();
        this.retained = connectorConfiguration.isRetained();
    }

    private void connect(ConnectorConfiguration configuration) {
        try {
            client.connect(configuration.getConnectOptions());
            client.subscribe(configuration.getRequestTopic(), this);
            scheduledFuture.cancel(false);
            LOGGER.info("Reconnected to {} as {} for topic {}", configuration.getBrokerUrl(),
                    configuration.getClientId(), configuration.getRequestTopic());
        } catch (MqttException e) {
            LOGGER.error("Error connecting through MQTT with configuration {}", configuration, e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        LOGGER.info("Messaged arrived: {},{}", topic, message);
        try {
            CompletableFuture<byte[]> response = dispatcher.process(message.getPayload());
            if (response == null) {
                LOGGER.warn("Cannot process message as Dispatcher is not present");
                return;
            }
            response.thenAccept(responseBytes -> sendMessage(responseTopic, responseBytes));
        } catch (RuntimeException e) {
            LOGGER.error("Error processing message {}", message, e);
        }
    }

    private void sendMessage(String topic, byte[] payload) {
        if (client == null) {
            LOGGER.warn("Cannot send message as we are disconnected from Mqtt");
        } else if (payload == null) {
            LOGGER.warn("Cannot send message as payload is null");
        } else if (iotTopic == null || responseTopic == null) {
            LOGGER.warn("Cannot send message as topics null from a bad connection");
        } else {
            MqttMessage message = MqttMessage.newInstance(payload, qos, retained);
            LOGGER.info("Sending message: {}, {}", topic, message);
            try {
                client.publish(topic, message);
            } catch (MqttException e) {
                LOGGER.warn("Error sending response: ", e);
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
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        if (client == null) {
            return;
        }

        LOGGER.info("Releasing MQTT entities");
        try {
            client.disconnect();
        } catch (MqttException exception) {
            LOGGER.error("Error disconnecting MQTT client: ", exception);
        }
        client = null;
    }
}
