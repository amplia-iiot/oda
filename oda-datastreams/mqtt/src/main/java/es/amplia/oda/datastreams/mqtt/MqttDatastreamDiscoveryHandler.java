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

import static es.amplia.oda.datastreams.mqtt.MqttDatastreams.*;

class MqttDatastreamDiscoveryHandler implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamDiscoveryHandler.class);

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
                                   String enableTopic, String disableTopic)
            throws MqttException {
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

    private void subscribeToDiscoveryTopics() throws MqttException {
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
                String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
                String deviceId = topicLevels[topicLevels.length - ONE_LEVEL];
                EnableDeviceMessage enableDeviceMessage =
                        serializer.deserialize(mqttMessage.getPayload(), EnableDeviceMessage.class);
                enableDeviceMessage.getDatastreams().forEach(enabledDatastream ->
                        enableDatastream(deviceId, enabledDatastream.getDatastreamId(), enabledDatastream.getMode()));
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
                String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
                String deviceId = topicLevels[topicLevels.length - TWO_LEVELS];
                String datastreamId = topicLevels[topicLevels.length - ONE_LEVEL];
                EnableDatastreamMessage enableDatastreamMessage =
                        serializer.deserialize(mqttMessage.getPayload(), EnableDatastreamMessage.class);
                enableDatastream(deviceId, datastreamId, enableDatastreamMessage.getMode());
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
                String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
                String deviceId = topicLevels[topicLevels.length - ONE_LEVEL];
                DisableDeviceMessage disableDeviceMessage =
                        serializer.deserialize(mqttMessage.getPayload(), DisableDeviceMessage.class);
                disableDeviceMessage.getDatastreams().forEach(disabledDatastream ->
                        disableDatastream(deviceId, disabledDatastream.getDatastreamId()));
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
            String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
            String deviceId = topicLevels[topicLevels.length - TWO_LEVELS];
            String datastreamId = topicLevels[topicLevels.length - ONE_LEVEL];
            disableDatastream(deviceId, datastreamId);
        }
    }

    @Override
    public void close() throws MqttException {
        mqttClient.unsubscribe(enableDatastreamTopic);
        mqttClient.unsubscribe(disableDatastreamTopic);
    }
}
