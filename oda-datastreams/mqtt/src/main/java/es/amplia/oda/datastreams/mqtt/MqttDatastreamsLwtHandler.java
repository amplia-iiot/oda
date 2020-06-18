package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.interfaces.Serializer;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

class MqttDatastreamsLwtHandler implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsLwtHandler.class);

    private final MqttClient mqttClient;
    private final Serializer serializer;
    private final MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager;
    private final MqttDatastreamsManager mqttDatastreamsManager;
    private final String lwtTopic;


    MqttDatastreamsLwtHandler(MqttClient mqttClient, Serializer serializer,
                              MqttDatastreamsPermissionManager mqttDatastreamsPermissionManager,
                              MqttDatastreamsManager mqttDatastreamsManager, String lwtTopic) {
        this.mqttClient = mqttClient;
        this.serializer = serializer;
        this.mqttDatastreamsPermissionManager = mqttDatastreamsPermissionManager;
        this.mqttDatastreamsManager = mqttDatastreamsManager;
        this.lwtTopic = lwtTopic;
        subscribeToLwtTopic();
    }

    private void subscribeToLwtTopic() {
        mqttClient.subscribe(lwtTopic, new LwtMessageListener());
    }

    @Value
    static class LwtMessage {
        private List<String> devices;
    }


    class LwtMessageListener implements MqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                LOGGER.info("Message arrived to the {} topic", topic);
                LwtMessage message = serializer.deserialize(mqttMessage.getPayload(), LwtMessage.class);
                message.getDevices().forEach(this::disableDevice);
            } catch(IOException e) {
                LOGGER.warn("Error processing last will message after external client disconnects: ", e);
            }
        }

        void disableDevice(String deviceId) {
            mqttDatastreamsPermissionManager.removeDevicePermissions(deviceId);
            mqttDatastreamsManager.removeDevice(deviceId);
        }
    }

    @Override
    public void close() {
        mqttClient.unsubscribe(lwtTopic);
    }
}
