package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static es.amplia.oda.datastreams.mqtt.MqttDatastreams.*;

class MqttDatastreamsEventHandler implements DatastreamsEvent, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsEventHandler.class);

    private final MqttClient mqttClient;
    private final MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager;
    private final Serializer serializer;
    private final EventDispatcher eventDispatcher;
    private final String deviceEventTopic;
    private final String datastreamEventTopic;

    MqttDatastreamsEventHandler(MqttClient mqttClient, MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager,
                                Serializer serializer, EventDispatcher eventDispatcher, String eventTopic) {
        this.mqttClient = mqttClient;
        this.mqttDatastreamsPermissionManager = mqttDatastreamsPermissionManager;
        this.serializer = serializer;
        this.eventDispatcher = eventDispatcher;
        this.deviceEventTopic = eventTopic + ONE_TOPIC_LEVEL_WILDCARD;
        this.datastreamEventTopic = eventTopic + TWO_TOPIC_LEVELS_WILDCARD;
        registerToEventSource();
    }

    @Override
    public void registerToEventSource() {
        try {
            mqttClient.subscribe(deviceEventTopic, new DeviceEventMessageListener());
            mqttClient.subscribe(datastreamEventTopic, new DatastreamEventMessageListener());
        } catch (MqttException e) {
            LOGGER.error("Error subscribing to MQTT event topics", e);
        }
    }

    @Value
    static class InnerDatastreamEvent {
        private String datastreamId;

        private Long at;
        private Object value;
    }
    @Value
    static class DeviceEventMessage {

        private List<String> path;
        private List<InnerDatastreamEvent> datastreams;
    }
    class DeviceEventMessageListener implements MqttMessageListener {

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                String deviceId = extractDeviceIdFromTopic(topic);
                DeviceEventMessage deviceEvent =
                        serializer.deserialize(mqttMessage.getPayload(), DeviceEventMessage.class);
                List<String> path = deviceEvent.getPath();
                deviceEvent.getDatastreams().stream()
                        .filter(datastream -> hasReadPermission(deviceId, datastream))
                        .forEach(datastream -> publish(deviceId, path, datastream));
            } catch (Exception e) {
                LOGGER.error("Error dispatching device event from MQTT message {}: {}", mqttMessage, e);
            }
        }

        private boolean hasReadPermission(String deviceId, InnerDatastreamEvent datastream) {
            return mqttDatastreamsPermissionManager.hasReadPermission(deviceId, datastream.getDatastreamId());
        }

        private String extractDeviceIdFromTopic(String topic) {
            return topic.substring(topic.lastIndexOf(TOPIC_LEVEL_SEPARATOR) + 1);
        }

        private void publish(String deviceId, List<String> path, InnerDatastreamEvent datastream) {
            MqttDatastreamsEventHandler.this.publish(deviceId, datastream.getDatastreamId(), path, datastream.getAt(),
                    datastream.getValue());
        }

    }
    @Value
    static class DatastreamEvent {

        private List<String> path;
        private Long at;
        private Object value;
    }
    class DatastreamEventMessageListener implements MqttMessageListener {

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                DatastreamInfo datastreamInfo = extractDeviceInfoFromTopic(topic);
                if (hasReadPermission(datastreamInfo)) {
                    DatastreamEvent datastreamEvent =
                            serializer.deserialize(mqttMessage.getPayload(), DatastreamEvent.class);
                    publish(datastreamInfo.getDeviceId(), datastreamInfo.getDatastreamId(), datastreamEvent);
                } else {
                    LOGGER.error("Error dispatching device event from MQTT message {}: Datastream {} of device {} does not have read access permission",
                            mqttMessage, datastreamInfo.getDeviceId(), datastreamInfo.getDatastreamId());
                }

            } catch (Exception e) {
                LOGGER.error("Error dispatching device event from MQTT message {}: {}", mqttMessage, e);
            }
        }

        private boolean hasReadPermission(DatastreamInfo datastreamInfo) {
            return mqttDatastreamsPermissionManager
                    .hasReadPermission(datastreamInfo.getDeviceId(), datastreamInfo.getDatastreamId());
        }

        private DatastreamInfo extractDeviceInfoFromTopic(String topic) {
            String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
            return new DatastreamInfo(topicLevels[topicLevels.length - TWO_LEVELS],
                    topicLevels[topicLevels.length - ONE_LEVEL]);
        }

        private void publish(String deviceId, String datastreamId, DatastreamEvent datastreamEvent) {
            MqttDatastreamsEventHandler.this.publish(deviceId, datastreamId, datastreamEvent.getPath(),
                    datastreamEvent.getAt(), datastreamEvent.getValue());
        }

    }

    @Override
    public void publish(String deviceId, String datastreamId, List<String> path, Long at, Object value) {
        String[] pathToPublish = Optional.ofNullable(path).map(list -> list.toArray(new String[0])).orElse(null);
        Event event = new Event(datastreamId, deviceId, pathToPublish, at, value);
        eventDispatcher.publish(event);
    }

    @Override
    public void unregisterFromEventSource() {
        try {
            mqttClient.unsubscribe(deviceEventTopic);
            mqttClient.unsubscribe(datastreamEventTopic);
        } catch (MqttException e) {
            LOGGER.error("Error unsubscribing from MQTT event topics", e);
        }

    }

    @Override
    public void close() {
        unregisterFromEventSource();
    }
}
