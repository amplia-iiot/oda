package es.amplia.oda.datastreams.mqtt;

final class MqttDatastreams {

    static final String TOPIC_LEVEL_SEPARATOR = "/";
    static final String ONE_TOPIC_LEVEL_WILDCARD = "/+";
    static final String TWO_TOPIC_LEVELS_WILDCARD = "/+/+";
    static final int ONE_LEVEL = 1;
    static final int TWO_LEVELS = 2;

    private MqttDatastreams() {
    }
}
