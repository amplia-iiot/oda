package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.Serializer;

class MqttDatastreamsFactory {

    private final MqttClient mqttClient;
    private final MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager;
    private final Serializer serializer;
    private final EventPublisher eventPublisher;
    private final String eventTopic;
    private final String readRequestOperationTopic;
    private final String readResponseOperationTopic;
    private final String writeRequestOperationTopic;
    private final String writeResponseOperationTopic;

    MqttDatastreamsFactory(MqttClient mqttClient, MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager,
                           Serializer serializer, EventPublisher eventPublisher, String readRequestOperationTopic,
                           String readResponseOperationTopic, String writeRequestOperationTopic,
                           String writeResponseOperationTopic, String eventTopic) {
        this.mqttClient = mqttClient;
        this.mqttDatastreamsPermissionManager = mqttDatastreamsPermissionManager;
        this.serializer = serializer;
        this.eventPublisher= eventPublisher;
        this.eventTopic = eventTopic;
        this.readRequestOperationTopic = readRequestOperationTopic;
        this.readResponseOperationTopic = readResponseOperationTopic;
        this.writeRequestOperationTopic = writeRequestOperationTopic;
        this.writeResponseOperationTopic = writeResponseOperationTopic;
    }

    MqttDatastreamsEvent createDatastreamsEvent() {
        return new MqttDatastreamsEvent(eventPublisher, mqttClient, mqttDatastreamsPermissionManager, serializer,
                eventTopic);
    }

    MqttDatastreamsGetter createDatastreamGetter(String datastreamId) {
        return new MqttDatastreamsGetter(datastreamId, mqttClient, mqttDatastreamsPermissionManager, serializer,
                readRequestOperationTopic, readResponseOperationTopic);
    }

    MqttDatastreamsSetter createDatastreamSetter(String datastreamId) {
        return new MqttDatastreamsSetter(datastreamId, mqttClient, mqttDatastreamsPermissionManager,
                serializer, writeRequestOperationTopic, writeResponseOperationTopic);
    }
}
