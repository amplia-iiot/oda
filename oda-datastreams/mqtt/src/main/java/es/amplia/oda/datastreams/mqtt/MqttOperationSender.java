package es.amplia.oda.datastreams.mqtt;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.OperationSender;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.operation.request.OperationRequest;

public class MqttOperationSender implements OperationSender{

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttOperationSender.class);
    private static final String TOPIC_SEPARATOR = "/";

    private final MqttClient mqttClient;
    private final String requestTopic;
    private final int qos;
    private final boolean retained;
    private final Serializer serializer;
    private final HashSet<String> nextLevelOdaIds;

    public MqttOperationSender(MqttClient client, Serializer serializer, String requestTopic, int qos, boolean retained, HashSet<String> odaList) {
        this.mqttClient = client;
        this.requestTopic = requestTopic;
        this.qos = qos;
        this.retained = retained;
        this.serializer = serializer;
        this.nextLevelOdaIds = odaList;
    }
    
    @Override
    public void downlink(OperationRequest<Object> operation) {
        try {
            String[] path = operation.getOperation().getRequest().getPath();
            // Si el path tiene longitud 1 quiere decir que el mensaje es para deviceId, si no es así es para el siguiente del path (path[1])
            String deviceToTopic = ( (path != null) && (path.length == 1) )?operation.getOperation().getRequest().getDeviceId():path[1];
            String finalTopic = requestTopic + TOPIC_SEPARATOR + deviceToTopic;
            operation.getOperation().getRequest().setPath(Arrays.copyOfRange(path, 1, path.length));
            MqttMessage message = MqttMessage.newInstance(serializer.serialize(operation), qos, retained);
            LOGGER.debug("Sending message to topic {}", finalTopic);
            LOGGER.trace("Mqtt message content: {}", message);
            this.mqttClient.publish(finalTopic, message, ContentType.JSON);
        } catch (IOException e) {
            LOGGER.error("Error parsing downlink message {}", operation, e);
        }
    }

    @Override
    public boolean isForNextLevel(String[] path, String deviceId) {
        if (this.nextLevelOdaIds.contains(deviceId)) return true;
        if ( (path.length == 2) && (this.nextLevelOdaIds.contains(path[1])) ) return true;
        return false;
    }
    
}
