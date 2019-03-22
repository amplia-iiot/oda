package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.Serializer;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static es.amplia.oda.datastreams.mqtt.MqttDatastreams.*;

class MqttDatastreamsSetter implements DatastreamsSetter, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsSetter.class);

    private final String datastreamId;
    private final List<String> devicesManaged = new ArrayList<>();
    private final MqttClient mqttClient;
    private final Serializer serializer;
    private final MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager;
    private final String writeRequestOperationRootTopic;
    private final String writeResponseOperationRootTopic;
    private final Map<Integer, CompletableFuture<Void>> futures = new ConcurrentHashMap<>();


    MqttDatastreamsSetter(String datastreamId, MqttClient mqttClient,
                          MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager, Serializer serializer,
                          String writeRequestOperationRootTopic, String writeResponseOperationRootTopic)
            throws MqttException {
        this.datastreamId = datastreamId;
        this.mqttClient = mqttClient;
        this.mqttDatastreamsPermissionManager = mqttDatastreamsPermissionManager;
        this.serializer = serializer;
        this.writeRequestOperationRootTopic = writeRequestOperationRootTopic;
        this.writeResponseOperationRootTopic = writeResponseOperationRootTopic + TWO_TOPIC_LEVELS_WILDCARD;
        subscribeToWriteResponseOperationTopic();
    }

    private void subscribeToWriteResponseOperationTopic() throws MqttException {
        mqttClient.subscribe(writeResponseOperationRootTopic, new WriteResponseMessageListener());
    }

    @Override
    public String getDatastreamIdSatisfied() {
        return datastreamId;
    }

    @Override
    public Type getDatastreamType() {
        return Object.class;
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
    static class WriteRequestOperation {
        private int id;
        private Object value;
    }

    @Override
    public CompletableFuture<Void> set(String deviceId, Object value) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (mqttDatastreamsPermissionManager.hasWritePermission(deviceId, datastreamId)) {
            int operationId = ThreadLocalRandom.current().nextInt();
            WriteRequestOperation request = new WriteRequestOperation(operationId, value);

            try {
                String writeDatastreamTopic = getDatastreamTopic(deviceId);
                byte[] payload = serializer.serialize(request);
                MqttMessage message = MqttMessage.newInstance(payload);
                mqttClient.publish(writeDatastreamTopic, message);
                futures.put(request.getId(), future);
            } catch (IOException|MqttException e) {
                LOGGER.error("Error executing write operation request {}: {}", request, e);
                future.completeExceptionally(new RuntimeException("Error  setting value of " +
                        datastreamId + " of device " + deviceId + ":" + e));
            }
        } else {
            future.completeExceptionally(new RuntimeException("Datastream " + datastreamId + " of device " + deviceId +
                    " does not have write access permission"));
        }

        return future;
    }

    private String getDatastreamTopic(String deviceId) {
        return writeRequestOperationRootTopic + TOPIC_LEVEL_SEPARATOR + deviceId + TOPIC_LEVEL_SEPARATOR + datastreamId;
    }

    @Value
    static class WriteResponseOperation {
        private int id;
        private int status;
        private String message;
    }

    class WriteResponseMessageListener implements MqttMessageListener {

        private static final int CREATED_STATUS_CODE = 201;

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            try {
                WriteResponseOperation response =
                        serializer.deserialize(message.getPayload(), WriteResponseOperation.class);

                if (futures.containsKey(response.getId())) {
                    CompletableFuture<Void> future = futures.get(response.getId());
                    futures.remove(response.getId());
                    if (isSuccessStatusCode(response.getStatus())) {
                        future.complete(null);
                    } else {
                        LOGGER.error("Operation {} ends with error: {}-{}", response.getId(), response.getStatus(),
                                response.getMessage());
                        future.completeExceptionally(
                                new RuntimeException("Error executing set operation: " + response.getMessage()));
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error processing get operation response: {0}", e);
            }
        }

        private boolean isSuccessStatusCode(int status) {
            return status == CREATED_STATUS_CODE;
        }
    }

    @Override
    public void close() {
        try {
            unsubscribeFromWriteOperationResponseTopic();
        } catch (Exception e) {
            LOGGER.error("Error closing mqtt datastreams setter for {}: ", datastreamId, e);
        }
    }

    private void unsubscribeFromWriteOperationResponseTopic() throws MqttException {
        mqttClient.unsubscribe(writeResponseOperationRootTopic);
    }
}
