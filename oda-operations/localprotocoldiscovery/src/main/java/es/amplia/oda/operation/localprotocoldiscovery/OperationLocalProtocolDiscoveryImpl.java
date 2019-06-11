package es.amplia.oda.operation.localprotocoldiscovery;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.operation.api.OperationDiscover;

import es.amplia.oda.operation.api.OperationSetClock;
import es.amplia.oda.operation.localprotocoldiscovery.configuration.LocalProtocolDiscoveryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class OperationLocalProtocolDiscoveryImpl implements OperationDiscover {
    private static final Logger logger = LoggerFactory.getLogger(OperationLocalProtocolDiscoveryImpl.class);

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
        logger.info("Discovering ODA");
        CompletableFuture<Result> future = new CompletableFuture<>();
        try {
            byte[] mess = serializer.serialize("{}");
            mqttClient.publish(topic, MqttMessage.newInstance(mess));
            new Result(OperationSetClock.ResultCode.ERROR_PROCESSING, "");
            future.complete(new Result(OperationSetClock.ResultCode.ERROR_PROCESSING, ""));
        } catch (IOException | MqttException e) {
            future.complete(new Result(OperationSetClock.ResultCode.ERROR_PROCESSING, e.getMessage()));
        }
        return future;
    }

    public void loadConfiguration(LocalProtocolDiscoveryConfiguration currentConfiguration) throws MqttException {
        try {
            if (mqttClient != null) {
                mqttClient.disconnect();
                mqttClient = null;
            }
        } catch (MqttException e) {
            logger.warn("Error closing Discover resources");
        }
        mqttClient = mqttClientFactory.createMqttClient(currentConfiguration.getServerURI(), currentConfiguration.getClientId());
        mqttClient.connect();
        this.topic = currentConfiguration.getDiscoverTopic();
    }
}
