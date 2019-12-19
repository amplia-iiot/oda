package es.amplia.oda.operation.localprotocoldiscovery;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.operation.api.OperationDiscover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class OperationLocalProtocolDiscoveryImpl implements OperationDiscover, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationLocalProtocolDiscoveryImpl.class);

    static final String EMPTY_MESSAGE = "{}";


    private final MqttClientFactory mqttClientFactory;
    private final Serializer serializer;
    private MqttClient mqttClient;
    private String topic;


    OperationLocalProtocolDiscoveryImpl(MqttClientFactory mqttClientFactoryProxy, Serializer serializer) {
        this.mqttClientFactory = mqttClientFactoryProxy;
        this.serializer = serializer;
    }
    
    @Override
    public CompletableFuture<Result> discover() {
        LOGGER.info("Processing discover operation in ODA");
        try {
            byte[] msg = serializer.serialize(EMPTY_MESSAGE);
            mqttClient.publish(topic, MqttMessage.newInstance(msg), ContentType.CBOR);
            return CompletableFuture.completedFuture(new Result(ResultCode.SUCCESSFUL, ""));
        } catch (IOException | MqttException e) {
            return CompletableFuture.completedFuture(new Result(ResultCode.ERROR_PROCESSING, e.getMessage()));
        }
    }

    public void loadConfiguration(String serverUri, String clientId, String discoverTopic) {
        close();
        topic = discoverTopic;
        mqttClient = mqttClientFactory.createMqttClient(serverUri, clientId);
        mqttClient.connect();
    }

    @Override
    public void close() {
        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                LOGGER.warn("Error closing MQTT client");
            }
        }
    }
}
