package es.amplia.oda.comms.mqtt.api;

import es.amplia.oda.core.commons.countermanager.CounterManager;
import es.amplia.oda.core.commons.countermanager.Counters;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MqttCounters extends Counters {

    private static CounterManager counterManager;

    public enum MqttCounterType {
        MQTT_RECEIVED("MQTT/RECEIVED/topic"),
        MQTT_SENT("MQTT/SENT/topic");

        private final String m_name;

        MqttCounterType(String _nameString){
            m_name = _nameString;
        }

        public String getCounterString(String topic) {

            String res = m_name;

            if (topic != null) {
                res = Pattern.compile("topic").matcher(Matcher.quoteReplacement(res)).replaceAll(topic);
            } else {
                log.warn("topic is null");
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

    public static void incrCounter(MqttCounterType counter, String topic, int number) {
        counterManager.incrementCounter(counter.getCounterString(topic), number);
    }
}
