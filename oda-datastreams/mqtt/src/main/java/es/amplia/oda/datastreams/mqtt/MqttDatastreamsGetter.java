package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.Serializer;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static es.amplia.oda.datastreams.mqtt.MqttDatastreams.*;

class MqttDatastreamsGetter implements DatastreamsGetter, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsGetter.class);

    private final String datastreamId;
    private final List<String> devicesManaged = new ArrayList<>();
    private final MqttClient mqttClient;
    private final MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager;
    private final Serializer serializer;
    private final String readRequestOperationRootTopic;
    private final String readResponseOperationRootTopic;
    private final Map<Integer, CompletableFuture<CollectedValue>> futures = new ConcurrentHashMap<>();


    MqttDatastreamsGetter(String datastreamId, MqttClient mqttClient,
                          MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager, Serializer serializer,
                          String readRequestOperationRootTopic, String readResponseOperationRootTopic) {
        this.datastreamId = datastreamId;
        this.mqttClient = mqttClient;
        this.mqttDatastreamsPermissionManager = mqttDatastreamsPermissionManager;
        this.serializer = serializer;
        this.readRequestOperationRootTopic = readRequestOperationRootTopic;
        this.readResponseOperationRootTopic = readResponseOperationRootTopic + ONE_TOPIC_LEVEL_WILDCARD +
                TOPIC_LEVEL_SEPARATOR + datastreamId;
        subscribeToReadResponseOperationTopic(mqttClient);
    }

    private void subscribeToReadResponseOperationTopic(MqttClient mqttClient) {
        mqttClient.subscribe(readResponseOperationRootTopic, new ReadResponseMessageListener());
    }

    @Override
    public String getDatastreamIdSatisfied() {
        return datastreamId;
    }

    synchronized void addManagedDevice(String deviceId) {
        devicesManaged.add(deviceId);
    }

    synchronized void removeManagedDevice(String deviceId) {
        devicesManaged.remove(deviceId);
    }

    @Override
    public synchronized List<String> getDevicesIdManaged() {
        return devicesManaged;
    }

    @Value
    static class ReadRequest {
        private int id;
    }

    @Override
    public CompletableFuture<CollectedValue> get(String deviceId) {
        CompletableFuture<CollectedValue> future = new CompletableFuture<>();

        if (mqttDatastreamsPermissionManager.hasReadPermission(deviceId, datastreamId)) {
            ReadRequest request = new ReadRequest(ThreadLocalRandom.current().nextInt());
            try {
                String readDatastreamTopic = getDatastreamTopic(deviceId);
                byte[] payload = serializer.serialize(request);
                MqttMessage message = MqttMessage.newInstance(payload);
                mqttClient.publish(readDatastreamTopic, message, ContentType.CBOR);
                futures.put(request.getId(), future);
            } catch (IOException | MqttException e) {
                LOGGER.error("Error executing request {}: {}", request, e);
                future.completeExceptionally(new RuntimeException("Error  getting value for " +
                        datastreamId + " of device " + deviceId + ":" + e));
            }
        } else {
            future.completeExceptionally(new RuntimeException("Datastream " + datastreamId +
                    " of device " + deviceId + " does not have read access permission"));
        }

        return future;
    }

    private String getDatastreamTopic(String deviceId) {
        return readRequestOperationRootTopic + TOPIC_LEVEL_SEPARATOR + deviceId + TOPIC_LEVEL_SEPARATOR + datastreamId;
    }

    @Value
    static class ReadResponse {
        private int id;
        private int status;
        private String message;
        private Long at;
        private Object value;
    }

    class ReadResponseMessageListener implements MqttMessageListener {

        private static final int OK_STATUS_CODE = 200;

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            try {
                ReadResponse response = serializer.deserialize(message.getPayload(), ReadResponse.class);

                if (futures.containsKey(response.getId())) {
                    CompletableFuture<CollectedValue> future = futures.get(response.getId());
                    futures.remove(response.getId());
                    if (isSuccessStatusCode(response.status)) {
                        future.complete(getCollectedValueFrom(response));
                    } else {
                        LOGGER.error("Operation {} ends with error: {}-{}", response.getId(), response.getStatus(),
                                response.getMessage());
                        future.completeExceptionally(
                                new RuntimeException("Error executing get operation: " + response.getMessage()));
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error processing read response message: {0}", e);
            }
        }

        private boolean isSuccessStatusCode(int status) {
            return status == OK_STATUS_CODE;
        }

        private CollectedValue getCollectedValueFrom(ReadResponse response) {
            return new CollectedValue(Optional.ofNullable(response.getAt()).orElse(System.currentTimeMillis()),
                    response.getValue());
        }
    }

    @Override
    public void close() {
        try {
            unsubscribeFromReadOperationResponseTopic();
        } catch (MqttException e) {
            LOGGER.error("Error closing mqtt datastreams getter for {}: ", datastreamId, e);
        }
    }

    private void unsubscribeFromReadOperationResponseTopic() {
        mqttClient.unsubscribe(readResponseOperationRootTopic);
    }
}
