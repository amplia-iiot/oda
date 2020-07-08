package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.interfaces.Serializer;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static es.amplia.oda.datastreams.mqtt.MqttDatastreams.*;

class MqttDatastreamDiscoveryHandler implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamDiscoveryHandler.class);

    private static final int NUM_THREADS = 10;

    private final ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
    private final MqttClient mqttClient;
    private final Serializer serializer;
    private final MqttDatastreamsManager mqttDatastreamsManager;
    private final MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager;
    private final String enableDeviceTopic;
    private final String enableDatastreamTopic;
    private final String disableDeviceTopic;
    private final String disableDatastreamTopic;

    MqttDatastreamDiscoveryHandler(MqttClient mqttClient, Serializer serializer,
                                   MqttDatastreamsManager mqttDatastreamsManager,
                                   MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager,
                                   String enableTopic, String disableTopic) {
        this.mqttClient = mqttClient;
        this.serializer = serializer;
        this.mqttDatastreamsManager = mqttDatastreamsManager;
        this.mqttDatastreamsPermissionManager = mqttDatastreamsPermissionManager;
        this.enableDeviceTopic = enableTopic + ONE_TOPIC_LEVEL_WILDCARD;
        this.enableDatastreamTopic = enableTopic + TWO_TOPIC_LEVELS_WILDCARD;
        this.disableDeviceTopic = disableTopic + ONE_TOPIC_LEVEL_WILDCARD;
        this.disableDatastreamTopic = disableTopic + TWO_TOPIC_LEVELS_WILDCARD;
        subscribeToDiscoveryTopics();
    }

    private void subscribeToDiscoveryTopics() {
        mqttClient.subscribe(enableDeviceTopic, new EnableDeviceMessageListener());
        mqttClient.subscribe(enableDatastreamTopic, new EnableDatastreamMessageListener());
        mqttClient.subscribe(disableDeviceTopic, new DisableDeviceMessageListener());
        mqttClient.subscribe(disableDatastreamTopic, new DisableDatastreamMessageListener());
    }

    void init(List<DatastreamInfoWithPermission> initialDatastreamsConfiguration) {
        initialDatastreamsConfiguration
                .forEach(conf -> enableDatastream(conf.getDeviceId(), conf.getDatastreamId(), conf.getPermission()));
    }

    private void enableDatastream(String deviceId, String datastreamId, MqttDatastreamPermission mode) {
        try {
            mqttDatastreamsPermissionManager.addPermission(deviceId, datastreamId, mode);
            mqttDatastreamsManager.createDatastream(deviceId, datastreamId);
            LOGGER.info("Created new datastream with deviceId {}, datastreamId {} and {} permissions", deviceId, datastreamId, mode);
        } catch (MqttException e) {
            LOGGER.error("Error creating MQTT datastream {} of device {} with mode {}: {}", datastreamId, deviceId,
                    mode, e);
        }
    }

    @Value
    static class EnableDeviceMessage {
        private List<EnabledDatastream> datastreams;
    }

    @Value
    static class EnabledDatastream {
        private String datastreamId;
        private MqttDatastreamPermission mode;
    }

    class EnableDeviceMessageListener implements MqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                LOGGER.info("Message arrived to the {} topic", topic);
                String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
                String deviceId = topicLevels[topicLevels.length - ONE_LEVEL];
                EnableDeviceMessage enableDeviceMessage =
                        serializer.deserialize(mqttMessage.getPayload(), EnableDeviceMessage.class);
                enableDeviceMessage.getDatastreams().forEach(enabledDatastream ->
                        executor.execute(() -> enableDatastream(deviceId, enabledDatastream.getDatastreamId(), enabledDatastream.getMode())));
            } catch (IOException e) {
                LOGGER.error("Invalid discovery message \"{}\": {}", mqttMessage, e);
            }
        }
    }

    @Value
    static class EnableDatastreamMessage {
        private MqttDatastreamPermission mode;
    }

    class EnableDatastreamMessageListener implements MqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                LOGGER.info("Message arrived to the {} topic", topic);
                String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
                String deviceId = topicLevels[topicLevels.length - TWO_LEVELS];
                String datastreamId = topicLevels[topicLevels.length - ONE_LEVEL];
                EnableDatastreamMessage enableDatastreamMessage =
                        serializer.deserialize(mqttMessage.getPayload(), EnableDatastreamMessage.class);
                executor.execute(() -> enableDatastream(deviceId, datastreamId, enableDatastreamMessage.getMode()));
            } catch (IOException e) {
                LOGGER.error("Invalid discovery message \"{}\": {}", mqttMessage, e);
            }
        }
    }

    @Value
    static class DisableDeviceMessage {
        private List<DisabledDatastream> datastreams;
    }

    @Value
    static class DisabledDatastream {
        private String datastreamId;
    }

    class DisableDeviceMessageListener implements MqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                LOGGER.info("Message arrived to the {} topic", topic);
                String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
                String deviceId = topicLevels[topicLevels.length - ONE_LEVEL];
                DisableDeviceMessage disableDeviceMessage =
                        serializer.deserialize(mqttMessage.getPayload(), DisableDeviceMessage.class);
                disableDeviceMessage.getDatastreams().forEach(disabledDatastream ->
                        executor.execute(() -> disableDatastream(deviceId, disabledDatastream.getDatastreamId())));
            } catch (IOException e) {
                LOGGER.error("Invalid disable device message \"{}\": {}", mqttMessage, e);
            }
        }
    }

    private void disableDatastream(String deviceId, String datastreamId) {
        mqttDatastreamsPermissionManager.removePermission(deviceId, datastreamId);
        mqttDatastreamsManager.removeDatastream(deviceId, datastreamId);
    }

    class DisableDatastreamMessageListener implements MqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            LOGGER.info("Message arrived to the {} topic", topic);
            String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
            String deviceId = topicLevels[topicLevels.length - TWO_LEVELS];
            String datastreamId = topicLevels[topicLevels.length - ONE_LEVEL];
            executor.execute(() -> disableDatastream(deviceId, datastreamId));
        }
    }

    @Override
    public void close() {
        mqttClient.unsubscribe(enableDatastreamTopic);
        mqttClient.unsubscribe(disableDatastreamTopic);
        executor.shutdown();
    }
}
