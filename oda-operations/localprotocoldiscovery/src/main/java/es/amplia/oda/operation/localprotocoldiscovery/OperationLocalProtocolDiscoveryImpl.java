package es.amplia.oda.operation.localprotocoldiscovery;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.mqtt.MqttDatastreamsService;
import es.amplia.oda.core.commons.osgi.proxies.MqttDatastreamsServiceProxy;
import es.amplia.oda.operation.api.OperationDiscover;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class OperationLocalProtocolDiscoveryImpl implements OperationDiscover, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationLocalProtocolDiscoveryImpl.class);

    static final String EMPTY_MESSAGE = "{}";


    private final MqttClientFactory mqttClientFactory;
    private final MqttDatastreamsService mqttDatastreamsService;
    private final Serializer serializer;
    private MqttClient mqttClient;
    private String topic;
    private CompletableFuture<Void> waitForMqttDatastreamsService;


    OperationLocalProtocolDiscoveryImpl(MqttClientFactory mqttClientFactoryProxy, MqttDatastreamsServiceProxy mqttDatastreamsServiceProxy, Serializer serializer) {
        this.mqttClientFactory = mqttClientFactoryProxy;
        this.mqttDatastreamsService = mqttDatastreamsServiceProxy;
        this.serializer = serializer;
    }
    
    @Override
    public CompletableFuture<Result> discover() {
        LOGGER.info("Processing discover operation in ODA");

        try {
            waitForMqttDatastreamsService.get();
            byte[] msg = serializer.serialize(EMPTY_MESSAGE);
            mqttClient.publish(topic, MqttMessage.newInstance(msg), ContentType.CBOR);
            return CompletableFuture.completedFuture(new Result(ResultCode.SUCCESSFUL, ""));
        } catch (IOException | MqttException | InterruptedException | ExecutionException e) {
            return CompletableFuture.completedFuture(new Result(ResultCode.ERROR_PROCESSING, e.getMessage()));
        }
    }

    public void loadConfiguration(String serverUri, String clientId, String discoverTopic) {
        close();

        if(waitForMqttDatastreamsService != null) {
            waitForMqttDatastreamsService.cancel(true);
            waitForMqttDatastreamsService = null;
        }

        waitForMqttDatastreamsService = waitForMqttDatastreamService(serverUri, clientId, discoverTopic);
    }

    public CompletableFuture<Void> waitForMqttDatastreamService(String serverUri, String clientId, String discoverTopic) {
        return CompletableFuture.runAsync(() -> {
            LOGGER.info("Waiting for MQTT Datastreams service ready");
            try {
                for(int i = 0; i < 3 && !mqttDatastreamsService.isReady(); i++) {
                    LOGGER.debug("Waiting 3 seconds... Try {}/3. ", i);
                    TimeUnit.SECONDS.sleep(3);
                }
            } catch (InterruptedException e) {
                LOGGER.error("Error waiting for service. Will try it immediately");
                Thread.currentThread().interrupt();
            }
            if (mqttDatastreamsService.isReady()) {
                LOGGER.info("MQTT Datastreams service is ready yet, preparing the discover operation");
                topic = discoverTopic;
                mqttClient = mqttClientFactory.createMqttClient(serverUri, clientId);
                mqttClient.connect();
                discover();
            } else {
                LOGGER.error("MQTT Datastream Service wasn't loaded. Discover operation won't works");
            }
        });
    }

    @Override
    public void close() {
        if(waitForMqttDatastreamsService != null) {
            waitForMqttDatastreamsService.cancel(true);
            waitForMqttDatastreamsService = null;
        }

        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                LOGGER.warn("Error closing MQTT client");
            }
        }
    }
}
