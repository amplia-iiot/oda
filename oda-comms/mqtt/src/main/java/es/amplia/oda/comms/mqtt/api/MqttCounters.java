package es.amplia.oda.comms.mqtt.api;

import es.amplia.oda.core.commons.countermanager.CounterManager;
import es.amplia.oda.core.commons.countermanager.Counters;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class MqttCounters extends Counters {

    private static CounterManager counterManager;

    public enum MqttTopicType {
        RESPONSE,
        REQUEST,
        EVENT,
        IOT
    }

    public enum MqttCounterType {
        MQTT_DATASTREAMS_RECEIVED("MQTT/DATASTREAMS/RECEIVED/topicType"),
        MQTT_DATASTREAMS_SENT("MQTT/DATASTREAMS/SENT/topicType"),
        MQTT_CONNECTOR_RECEIVED("MQTT/CONNECTOR/RECEIVED/topicType"),
        MQTT_CONNECTOR_SENT("MQTT/CONNECTOR/SENT/topicType");

        private final String m_name;

        MqttCounterType(String _nameString){
            m_name = _nameString;
        }

        public String getCounterString(MqttTopicType topicType) {

            String res = m_name;

            if (topicType != null) {
                res = Pattern.compile("topicType").matcher(res).replaceAll(String.valueOf(topicType));
            } else {
                log.warn("topicType is null");
            }

            if (log.isTraceEnabled()) {
                log.trace("counter string retrieved: " + res);
            }

            return res;
        }
    }

    public MqttCounters(CounterManager _counterManager) {
        counterManager = _counterManager;
    }

    public static void incrCounter(MqttCounterType counter, MqttTopicType topicType, int number) {
        counterManager.incrementCounter(counter.getCounterString(topicType), number);
    }

    public static boolean compareTopics(String topicTemplate, String topicToCheck){
        if (topicTemplate == null && topicToCheck == null) {
            return true;
        }
        if (topicTemplate == null || topicToCheck == null) {
            return false;
        }

        String[] topicTemplateSplit = topicTemplate.split("/");
        String[] topicToCheckSplit = topicToCheck.split("/");

        for (int i = 0; i < topicToCheckSplit.length; i++) {
            String topicToCheckPart = topicToCheckSplit[i];
            String topicTemplatePart = topicTemplateSplit[i];

            // wildcard
            // # is a wildcard equivalent to n topic levels
            if (topicTemplatePart.equals("#")) {
                return true;
            } else {
                // + is a wildcard equivalent to one topic level
                if (!topicTemplatePart.equals("+")) {
                    // if it is not a wildcard, check if they are equals
                    if (!topicToCheckPart.equals(topicTemplatePart)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
