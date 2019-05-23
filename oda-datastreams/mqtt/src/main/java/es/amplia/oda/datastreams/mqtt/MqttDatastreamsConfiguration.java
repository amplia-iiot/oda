package es.amplia.oda.datastreams.mqtt;

import lombok.Value;

@Value
class MqttDatastreamsConfiguration {
    private String serverURI;
    private String clientId;
    private String enableDatastreamTopic;
    private String disableDatastreamTopic;
    private String eventTopic;
    private String readRequestTopic;
    private String readResponseTopic;
    private String writeRequestTopic;
    private String writeResponseTopic;
    private String lwtTopic;
}
