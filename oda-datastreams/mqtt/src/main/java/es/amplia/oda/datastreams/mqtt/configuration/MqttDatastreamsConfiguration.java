package es.amplia.oda.datastreams.mqtt.configuration;

import java.util.List;

import es.amplia.oda.comms.mqtt.api.MqttConnectOptions.KeyStoreType;
import lombok.Data;
import lombok.Setter;

@Data
public class MqttDatastreamsConfiguration {
    private final String serverURI;
    private final String clientId;
    private final String password;
    private final String eventTopic;
    private final String requestTopic;
    private final String responseTopic;
    private final int Qos;
    private final boolean retained;
    @Setter private String keyStore = null;
    @Setter private KeyStoreType keyStoreType = null;
    @Setter private char[] keyStorePassword = null;
    @Setter private String trustStore = null;
    @Setter private KeyStoreType trustStoreType = null;
    @Setter private char[] trustStorePassword = null;
    @Setter private List<String> nextLevelOdaIds = null;
}
