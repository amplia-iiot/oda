package es.amplia.oda.connector.mqtt;

import es.amplia.oda.comms.mqtt.api.*;
import es.amplia.oda.connector.mqtt.configuration.ConnectorConfiguration;
import es.amplia.oda.core.commons.entities.ContentType;
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
    private String requestTopic;
    private int qos;
    private boolean retained;
    private MqttClient client;
    private boolean hasMaxlength = false;
    private int maxLength = 0;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;
    private ConnectorConfiguration connectorConfiguration;

    MqttConnector(MqttClientFactory mqttClientFactory, Dispatcher dispatcher) {
        this.mqttClientFactory = mqttClientFactory;
        this.dispatcher = dispatcher;
        this.client = null;
    }

    public void loadConfigurationAndInit(ConnectorConfiguration connectorConfiguration) {
        close();
        this.connectorConfiguration = connectorConfiguration;
        this.client = mqttClientFactory.createMqttClient(connectorConfiguration.getBrokerUrl(), connectorConfiguration.getClientId());
        this.iotTopic = connectorConfiguration.getIotTopic();
        this.responseTopic = connectorConfiguration.getResponseTopic();
        this.requestTopic = connectorConfiguration.getRequestTopic();
        this.qos = connectorConfiguration.getQos();
        this.retained = connectorConfiguration.isRetained();
        this.hasMaxlength = connectorConfiguration.isHasMaxlength();
        this.maxLength = connectorConfiguration.getMaxlength();
        createFutureConnection();
        LOGGER.info("Created and prepared mqtt connection");
    }

    private void connect(ConnectorConfiguration configuration) {
        if (client.isConnected()) {
            LOGGER.warn("Client already connected");
            cancelFutureConnection();
            return;
        }

        try {
            client.subscribe(configuration.getRequestTopic(), this);
            client.connect(configuration.getConnectOptions(), new MqttActionListener() {

                @Override
                public void onFailure(Throwable err) {
                    LOGGER.error("Error connecting to " + configuration.getBrokerUrl() + " as " + configuration.getClientId(), err);
                }

                @Override
                public void onSuccess() {
                    cancelFutureConnection();
                    LOGGER.info("Connected to {} as {} for topic {}", configuration.getBrokerUrl(),
                            configuration.getClientId(), configuration.getRequestTopic());
                }
                
            });
        } catch (MqttException e) {
            LOGGER.error("Error connecting through MQTT with configuration {}", configuration, e);
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        LOGGER.debug("Messaged arrived to topic {}", topic);
        LOGGER.trace("Mqtt message content: {}", message);

        // increase counter
        incrCounter(MqttCounters.MqttCounterType.MQTT_CONNECTOR_RECEIVED, topic, 1);

        try {
            // In MQTT v5 contentType should come from MqttProperties Content-Format
            ContentType contentType = ContentType.JSON;
            CompletableFuture<byte[]> response = dispatcher.process(message.getPayload(), contentType);
            if (response == null) {
                LOGGER.debug("Message not processed by this device or Dispatcher is not present");
                return;
            }
            response.thenAccept(responseBytes -> sendMessage(responseTopic, responseBytes, contentType, qos))
                    .exceptionally(e -> {
                        LOGGER.error("Error processing operation {} with content type {}", message, contentType, e);
                        return null;
                    });
        } catch (RuntimeException e) {
            LOGGER.error("Error processing message {}", message, e);
        }
    }

    private void sendMessage(String topic, byte[] payload, ContentType contentType, int qos) {
        if (client == null) {
            LOGGER.warn("Cannot send message to topic {} as we are disconnected from Mqtt", topic);
        } else if (payload == null) {
            LOGGER.warn("Cannot send message to topic {} as payload is null", topic);
        } else if (iotTopic == null || responseTopic == null) {
            LOGGER.warn("Cannot send message as topics null from a bad connection");
        } else if (!client.isConnected()) {
            LOGGER.warn("Cannot send message to topic {} as client is not connected", topic);
            createFutureConnection();
        } else {
            // In MQTT v5 contentType should be injected in MqttProperties Content-Format
            MqttMessage message = MqttMessage.newInstance(payload, qos, retained);
            LOGGER.debug("Sending message to topic {}", topic);
            LOGGER.trace("Mqtt message content: {}", message );
            try {
                client.publish(topic, message, contentType);
                // increase counter
                incrCounter(MqttCounters.MqttCounterType.MQTT_CONNECTOR_SENT, topic, 1);
            } catch (MqttException e) {
                LOGGER.warn("Cannot send the message: {}, on topic: {}", message, topic, e);
                processMQTTException(e);
            }
        }
    }

    @Override
    public void uplink(byte[] payload, ContentType contentType) {
        sendMessage(iotTopic, payload, contentType, qos);
    }

    public void uplinkNoQos(byte[] payload, ContentType contentType) {
        sendMessage(iotTopic, payload, contentType, 0);
    }

    @Override
    public void uplinkResponse(byte[] payload, ContentType contentType) {
        sendMessage(responseTopic, payload, contentType, qos);
    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    @Override
    public void close() {
        cancelFutureConnection();

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

    @Override
    public boolean hasMaxlength() {
        return this.hasMaxlength;
    }

    @Override
    public int getMaxLength() {
        return this.maxLength;
    }

    private void createFutureConnection() {
        if (scheduledFuture == null) {
            LOGGER.info("Scheduling mqtt connection to '{}' with initial delay of {} seconds and retry delay of {} seconds",
                    this.connectorConfiguration.getBrokerUrl(), this.connectorConfiguration.getInitialDelay(),
                    this.connectorConfiguration.getRetryDelay());
            scheduledFuture = executorService.scheduleWithFixedDelay(() -> connect(this.connectorConfiguration),
                    this.connectorConfiguration.getInitialDelay(), this.connectorConfiguration.getRetryDelay(), TimeUnit.SECONDS);
        }
    }

    private void cancelFutureConnection(){
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
    }

    @Override
    public void onFailure(Throwable err) {
        LOGGER.error("Error subscribing to request topic: " + requestTopic, err);
    }

    @Override
    public void onSuccess() {
        LOGGER.info("Subscribed to request topic: " + requestTopic);
    }

    private void incrCounter(MqttCounters.MqttCounterType counterType, String topic, int number) {
        MqttCounters.MqttTopicType topicType;

        if (MqttCounters.compareTopics(this.iotTopic, topic)) {
            topicType = MqttCounters.MqttTopicType.IOT;
        } else if (MqttCounters.compareTopics(this.requestTopic, topic)) {
            topicType = MqttCounters.MqttTopicType.REQUEST;
        } else if (MqttCounters.compareTopics(this.responseTopic, topic)) {
            topicType = MqttCounters.MqttTopicType.RESPONSE;
        } else {
           LOGGER.error("Can't increment counter as topic '{}' is not one of the topics configured in bundle", topic);
           return;
        }

        // increase counter
        MqttCounters.incrCounter(counterType, topicType, number);
    }

    private void processMQTTException(MqttException exception){
        int reasonCode = exception.getReasonCode();
        if (reasonCode == MqttException.REASON_CODE_CLIENT_NOT_CONNECTED) {
            createFutureConnection();
        }
    }

}
