package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.event.api.EventDispatcher;

class MqttDatastreamsFactory {

    private final MqttClient mqttClient;
    private final MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager;
    private final Serializer serializer;
    private final EventDispatcher eventDispatcher;
    private final String eventTopic;
    private final String readRequestOperationTopic;
    private final String readResponseOperationTopic;
    private final String writeRequestOperationTopic;
    private final String writeResponseOperationTopic;

    MqttDatastreamsFactory(MqttClient mqttClient, MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager,
                           Serializer serializer, EventDispatcher eventDispatcher, String readRequestOperationTopic,
                           String readResponseOperationTopic, String writeRequestOperationTopic,
                           String writeResponseOperationTopic, String eventTopic) {
        this.mqttClient = mqttClient;
        this.mqttDatastreamsPermissionManager = mqttDatastreamsPermissionManager;
        this.serializer = serializer;
        this.eventDispatcher = eventDispatcher;
        this.eventTopic = eventTopic;
        this.readRequestOperationTopic = readRequestOperationTopic;
        this.readResponseOperationTopic = readResponseOperationTopic;
        this.writeRequestOperationTopic = writeRequestOperationTopic;
        this.writeResponseOperationTopic = writeResponseOperationTopic;
    }

    MqttDatastreamsEventHandler createDatastreamsEventHandler() {
        return new MqttDatastreamsEventHandler(mqttClient, mqttDatastreamsPermissionManager, serializer,
                eventDispatcher, eventTopic);
    }

    MqttDatastreamsGetter createDatastreamGetter(String datastreamId) throws MqttException {
        return new MqttDatastreamsGetter(datastreamId, mqttClient, mqttDatastreamsPermissionManager, serializer,
                readRequestOperationTopic, readResponseOperationTopic);
    }

    MqttDatastreamsSetter createDatastreamSetter(String datastreamId) throws MqttException {
        return new MqttDatastreamsSetter(datastreamId, mqttClient, mqttDatastreamsPermissionManager,
                serializer, writeRequestOperationTopic, writeResponseOperationTopic);
    }
}
