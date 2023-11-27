package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttClient;
import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.comms.mqtt.api.MqttMessage;
import es.amplia.oda.comms.mqtt.api.MqttMessageListener;
import es.amplia.oda.core.commons.interfaces.AbstractDatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.utils.operation.response.OperationResponse;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.*;
import es.amplia.oda.event.api.ResponseDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class MqttDatastreamsEvent extends AbstractDatastreamsEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttDatastreamsEvent.class);
    private static final String TOPIC_SEPARATOR = "/";

    private final MqttClient mqttClient;
    private final Serializer serializer;
    private final String eventTopic;
    private final String responseTopic;
    private final DeviceInfoProviderProxy deviceInfoProvider;
    private final ResponseDispatcher responseDispatcher;


    MqttDatastreamsEvent(EventPublisher eventPublisher, MqttClient mqttClient, Serializer serializer, String eventTopic,
                        DeviceInfoProviderProxy deviceInfoProvider, String responseTopic, ResponseDispatcher respDispatcher) {
        super(eventPublisher);
        this.mqttClient = mqttClient;
        this.serializer = serializer;
        this.eventTopic = eventTopic;
        this.responseTopic = responseTopic;
        this.responseDispatcher = respDispatcher;
        this.deviceInfoProvider = deviceInfoProvider;
        registerToEventSource();
    }

    public void registerToEventSource() {
        mqttClient.subscribe(eventTopic, new EventMessageListener());
        mqttClient.subscribe(responseTopic, new ResponseMessageListener());
    }

    class EventMessageListener implements MqttMessageListener {

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            Map<String, Map<String, Map<Long, Object>>> events = new HashMap<>();
            try {
                LOGGER.info("Message arrived to the {} topic", topic);
                OutputDatastream event =
                        serializer.deserialize(mqttMessage.getPayload(), OutputDatastream.class);
                String deviceId = event.getDevice()!=null?event.getDevice():extractDeviceIdFromTopic(topic);
                long eventAt = System.currentTimeMillis();

                event.getDatastreams().forEach(ds -> {
                    Map<Long,Object> eventInfo = new HashMap<>();
                    Map<String,Map<Long,Object>> eventsByFeed = new HashMap<>();
                    ds.getDatapoints().forEach(dp -> {
                        long at = dp.getAt()!=null?dp.getAt():eventAt;
                        eventInfo.put(at, dp.getValue());
                    });
                    eventsByFeed.put(ds.getFeed(), eventInfo);
                    events.put(ds.getId(), eventsByFeed);
                });

                String[] path = addDeviceIdToPath(event.getPath());
                LOGGER.info("Sending event {} for device {} and path {}", events, deviceId, path);
                publish(deviceId, Arrays.asList(path), events); // Añadir deviceId del ODA al path

            } catch (Exception e) {
                LOGGER.error("Error dispatching device event from MQTT message {}: {}", mqttMessage, e);
            }
        }

        private String extractDeviceIdFromTopic(String topic) {
            String[] topics = topic.split(TOPIC_SEPARATOR);
            return topics[topics.length-1];
        }
    }

    class ResponseMessageListener implements MqttMessageListener {

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                LOGGER.info("Response arrived to the {} topic", topic);
                OperationResponse resp = serializer.deserialize(mqttMessage.getPayload(), OperationResponse.class);

                String[] path = resp.getOperation().getResponse().getPath();
                resp.getOperation().getResponse().setPath(addDeviceIdToPath(path));
                LOGGER.info("Sending response {}", resp);
                responseDispatcher.publishResponse(resp);

            } catch (Exception e) {
                LOGGER.error("Error dispatching device response from MQTT message {}: {}", mqttMessage, e);
            }
        }
    }

    private String[] addDeviceIdToPath(String[] path) {
        String[] newPath;
        String ODAid = deviceInfoProvider.getDeviceId();
        if ( (path == null) || (path.length == 0) ) {
            newPath = new String[1];
        } else {
            newPath = new String[path.length+1];
            System.arraycopy(path, 0, newPath, 1, path.length);
        }
        newPath[0] = ODAid;
        return newPath;
    }

    public void unregisterFromEventSource() {
        try {
            mqttClient.unsubscribe(eventTopic);
            mqttClient.unsubscribe(responseTopic);
        } catch (MqttException e) {
            LOGGER.error("Error unsubscribing from MQTT event topics", e);
        }

    }
}
