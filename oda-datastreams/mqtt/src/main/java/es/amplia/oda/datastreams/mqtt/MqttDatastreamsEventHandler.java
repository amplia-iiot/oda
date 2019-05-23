package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static es.amplia.oda.datastreams.mqtt.MqttDatastreams.*;

class MqttDatastreamsEventHandler implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsEventHandler.class);

    private final MqttClient mqttClient;
    private final MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager;
    private final Serializer serializer;
    private final EventDispatcher eventDispatcher;
    private final String deviceEventTopic;
    private final String datastreamEventTopic;

    MqttDatastreamsEventHandler(MqttClient mqttClient, MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager,
                                Serializer serializer, EventDispatcher eventDispatcher, String eventTopic)
            throws MqttException {
        this.mqttClient = mqttClient;
        this.mqttDatastreamsPermissionManager = mqttDatastreamsPermissionManager;
        this.serializer = serializer;
        this.eventDispatcher = eventDispatcher;
        this.deviceEventTopic = eventTopic + ONE_TOPIC_LEVEL_WILDCARD;
        this.datastreamEventTopic = eventTopic + TWO_TOPIC_LEVELS_WILDCARD;
        subscribeToEventTopics();
    }

    private void subscribeToEventTopics() throws MqttException {
        mqttClient.subscribe(deviceEventTopic, new DeviceEventMessageListener());
        mqttClient.subscribe(datastreamEventTopic, new DatastreamEventMessageListener());
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
                String[] path = Optional.ofNullable(deviceEvent.getPath())
                        .map(list -> list.toArray(new String[0])).orElse(null);
                deviceEvent.getDatastreams().stream()
                        .map(innerDatastreamEvent -> toOptionalEvent(deviceId, path, innerDatastreamEvent))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(eventDispatcher::publish);
            } catch (Exception e) {
                LOGGER.error("Error dispatching device event from MQTT message {}: {}", mqttMessage, e);
            }
        }

        private String extractDeviceIdFromTopic(String topic) {
            return topic.substring(topic.lastIndexOf(TOPIC_LEVEL_SEPARATOR) + 1);
        }

        private Optional<Event> toOptionalEvent(String deviceId, String[] path,
                                                InnerDatastreamEvent innerDatastreamEvent) {
            if (mqttDatastreamsPermissionManager.hasReadPermission(deviceId, innerDatastreamEvent.getDatastreamId())) {
                return Optional.of(new Event(innerDatastreamEvent.getDatastreamId(), deviceId, path,
                        innerDatastreamEvent.getAt(), innerDatastreamEvent.getValue()));
            } else {
                LOGGER.error("\"Error dispatching device event from MQTT message: Datastream {} of device {} does not have read access permission",
                        deviceId, innerDatastreamEvent.getDatastreamId());
                return Optional.empty();
            }

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
                if (mqttDatastreamsPermissionManager.hasReadPermission(datastreamInfo.getDeviceId(),
                        datastreamInfo.getDatastreamId())) {
                    DatastreamEvent datastreamEvent =
                            serializer.deserialize(mqttMessage.getPayload(), DatastreamEvent.class);
                    eventDispatcher.publish(
                            toEvent(datastreamInfo.getDatastreamId(), datastreamInfo.getDeviceId(), datastreamEvent));
                } else {
                    LOGGER.error("Error dispatching device event from MQTT message {}: Datastream {} of device {} does not have read access permission",
                            mqttMessage, datastreamInfo.getDeviceId(), datastreamInfo.getDatastreamId());
                }

            } catch (Exception e) {
                LOGGER.error("Error dispatching device event from MQTT message {}: {}", mqttMessage, e);
            }
        }

        private DatastreamInfo extractDeviceInfoFromTopic(String topic) {
            String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
            return new DatastreamInfo(topicLevels[topicLevels.length - TWO_LEVELS],
                    topicLevels[topicLevels.length - ONE_LEVEL]);
        }

        private Event toEvent(String deviceId, String datastreamId, DatastreamEvent datastreamEvent) {
            String[] path = Optional.ofNullable(datastreamEvent.getPath()).map(list -> list.toArray(new String[0]))
                    .orElse(null);
            return new Event(deviceId, datastreamId, path, datastreamEvent.getAt(), datastreamEvent.getValue());
        }
    }

    @Override
    public void close() {
        try {
            unsubscribeFromEventTopics();
        } catch (MqttException e) {
            LOGGER.error("Error closing mqtt datastream event handler: {0}", e);
        }
    }

    private void unsubscribeFromEventTopics() throws MqttException {
        mqttClient.unsubscribe(deviceEventTopic);
        mqttClient.unsubscribe(datastreamEventTopic);
    }
}
