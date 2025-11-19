package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.*;
import es.amplia.oda.core.commons.interfaces.AbstractDatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.ResponseDispatcher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.osgi.proxies.DeviceInfoProviderProxy;
import es.amplia.oda.core.commons.utils.operation.response.OperationResponse;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
    private final HashSet<String> nextLevelOdaIds;


    MqttDatastreamsEvent(EventPublisher eventPublisher, MqttClient mqttClient, Serializer serializer, String eventTopic,
                        DeviceInfoProviderProxy deviceInfoProvider, String responseTopic, ResponseDispatcher respDispatcher, HashSet<String> odaList) {
        super(eventPublisher);
        this.mqttClient = mqttClient;
        this.serializer = serializer;
        this.eventTopic = eventTopic;
        this.responseTopic = responseTopic;
        this.responseDispatcher = respDispatcher;
        this.deviceInfoProvider = deviceInfoProvider;
        this.nextLevelOdaIds = odaList;
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
                LOGGER.debug("Message arrived to the {} topic", topic);
                // increase counter
                MqttCounters.incrCounter(MqttCounters.MqttCounterType.MQTT_DATASTREAMS_RECEIVED, MqttCounters.MqttTopicType.EVENT,1);

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

                String[] path = addDeviceIdToPath(event.getPath(), deviceId);
                LOGGER.debug("Sending event {} for device {} and path {}", events, deviceId, path);
                publish(deviceId, Arrays.asList(path), events); // Añadir deviceId del ODA al path

            } catch (Exception e) {
                LOGGER.error("Error dispatching device event from MQTT message {}: {}", mqttMessage, e.getMessage());
            }
        }

        private String extractDeviceIdFromTopic(String topic) {
            String[] topics = topic.split(TOPIC_SEPARATOR);
            return topics[topics.length-1];
        }

        @Override
        public void onFailure(Throwable err) {
            LOGGER.error("Error subscribing to event topic: " + eventTopic, err);
        }

        @Override
        public void onSuccess() {
            LOGGER.info("Subscribed to event topic: " + eventTopic);
        }
    }

    class ResponseMessageListener implements MqttMessageListener {

        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {
            try {
                LOGGER.debug("Response arrived to the {} topic", topic);
                // increase counter
                MqttCounters.incrCounter(MqttCounters.MqttCounterType.MQTT_DATASTREAMS_RECEIVED, MqttCounters.MqttTopicType.RESPONSE, 1);

                OperationResponse resp = serializer.deserialize(mqttMessage.getPayload(), OperationResponse.class);
                String[] path = resp.getOperation().getResponse().getPath();
                String deviceId = resp.getOperation().getResponse().getDeviceId();
                resp.getOperation().getResponse().setPath(addDeviceIdToPath(path, deviceId));
                LOGGER.debug("Sending response {}", resp);
                responseDispatcher.publishResponse(resp);

            } catch (Exception e) {
                LOGGER.error("Error dispatching device response from MQTT message {}: {}", mqttMessage, e.getMessage());
            }
        }

        @Override
        public void onFailure(Throwable err) {
            LOGGER.error("Error subscribing to response topic: " + responseTopic, err);
        }

        @Override
        public void onSuccess() {
            LOGGER.info("Subscribed to response topic: " + responseTopic);
        }
    }

    private String[] addDeviceIdToPath(String[] path, String deviceId) {
        String[] newPath;
        String ODAid = deviceInfoProvider.getDeviceId();
        String nextODAid = deviceId;
        if ( (path == null) || (path.length == 0) ) {
            newPath = new String[1];
        } else {
            newPath = new String[path.length+1];
            System.arraycopy(path, 0, newPath, 1, path.length);
            nextODAid = path[0];
        }
        newPath[0] = ODAid;
        // Añadimos el deviceId del ODA del nivel anterior a la lista para registrarlo
        this.nextLevelOdaIds.add(nextODAid);
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
