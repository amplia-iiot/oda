package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.interfaces.AbstractDatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.Serializer;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static es.amplia.oda.datastreams.mqtt.MqttDatastreams.*;

class MqttDatastreamsEvent extends AbstractDatastreamsEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsEvent.class);


    private final MqttClient mqttClient;
    private final MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager;
    private final Serializer serializer;
    private final String deviceEventTopic;
    private final String datastreamEventTopic;


    MqttDatastreamsEvent(EventPublisher eventPublisher, MqttClient mqttClient,
                         MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager,
                         Serializer serializer, String eventTopic) {
        super(eventPublisher);
        this.mqttClient = mqttClient;
        this.mqttDatastreamsPermissionManager = mqttDatastreamsPermissionManager;
        this.serializer = serializer;
        this.deviceEventTopic = eventTopic + ONE_TOPIC_LEVEL_WILDCARD;
        this.datastreamEventTopic = eventTopic + TWO_TOPIC_LEVELS_WILDCARD;
        registerToEventSource();
    }

    @Override
    public void registerToEventSource() {
        mqttClient.subscribe(deviceEventTopic, new DeviceEventMessageListener());
        mqttClient.subscribe(datastreamEventTopic, new DatastreamEventMessageListener());
    }

    @Value
    static class InnerDatastreamEvent {
        String datastreamId;
        Long at;
        Object value;
    }

    @Value
    static class DeviceEventMessage {
        List<String> path;
        List<InnerDatastreamEvent> datastreams;
    }

    class DeviceEventMessageListener implements MqttMessageListener {

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                LOGGER.info("Message arrived to the {} topic", topic);
                String deviceId = extractDeviceIdFromTopic(topic);
                DeviceEventMessage deviceEvent =
                        serializer.deserialize(mqttMessage.getPayload(), DeviceEventMessage.class);
                deviceEvent.getDatastreams().stream()
                        .filter(event -> hasPermission(deviceId, event.getDatastreamId()))
                        .forEach(event -> publish(deviceId, event.getDatastreamId(), deviceEvent.getPath(),
                                event.getAt(), event.getValue()));
            } catch (Exception e) {
                LOGGER.error("Error dispatching device event from MQTT message {}: {}", mqttMessage, e.getMessage());
            }
        }

        private String extractDeviceIdFromTopic(String topic) {
            return topic.substring(topic.lastIndexOf(TOPIC_LEVEL_SEPARATOR) + 1);
        }
    }

    @Value
    static class DatastreamEvent {
        List<String> path;
        Long at;
        Object value;
    }

    class DatastreamEventMessageListener implements MqttMessageListener {

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                LOGGER.info("Message arrived to the {} topic", topic);
                DatastreamInfo datastreamInfo = extractDeviceInfoFromTopic(topic);
                if (hasPermission(datastreamInfo.getDeviceId(), datastreamInfo.getDatastreamId())) {
                    DatastreamEvent event =
                            serializer.deserialize(mqttMessage.getPayload(), DatastreamEvent.class);
                    publish(datastreamInfo.getDeviceId(), datastreamInfo.getDatastreamId(), event.getPath(),
                            event.getAt(), event.getValue());
                }

            } catch (Exception e) {
                LOGGER.error("Error dispatching device event from MQTT message {}: {}", mqttMessage, e.getMessage());
            }
        }

        private DatastreamInfo extractDeviceInfoFromTopic(String topic) {
            String[] topicLevels = topic.split(TOPIC_LEVEL_SEPARATOR);
            return new DatastreamInfo(topicLevels[topicLevels.length - TWO_LEVELS],
                    topicLevels[topicLevels.length - ONE_LEVEL]);
        }
    }

    private boolean hasPermission(String deviceId, String datastreamId) {
        return mqttDatastreamsPermissionManager.hasReadPermission(deviceId, datastreamId);
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
}
