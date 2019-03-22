package es.amplia.oda.datastreams.mqtt;

import es.amplia.oda.comms.mqtt.api.MqttException;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class MqttDatastreamsConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    static final String SERVER_URI_PROPERTY_NAME = "brokerURI";
    static final String CLIENT_ID_PROPERTY_NAME = "clientId";
    static final String ENABLE_DATASTREAM_TOPIC_PROPERTY_NAME = "enableDatastreamTopic";
    static final String DISABLE_DATASTREAM_TOPIC_PROPERTY_NAME = "disableDatastreamTopic";
    static final String EVENT_TOPIC_PROPERTY_NAME = "eventTopic";
    static final String READ_REQUEST_TOPIC_PROPERTY_NAME = "readRequestTopic";
    static final String READ_RESPONSE_TOPIC_PROPERTY_NAME = "readResponseTopic";
    static final String WRITE_REQUEST_TOPIC_PROPERTY_NAME = "writeRequestTopic";
    static final String WRITE_RESPONSE_TOPIC_PROPERTY_NAME = "writeResponseTopic";
    static final String LWT_TOPIC_PROPERTY_NAME = "lwtTopic";

    private static final String DATASTREAMS_CONFIGURATION_REGEX = "(\\w+);(\\w+)";
    private static final Pattern DATASTREAMS_CONFIGURATION_PATTERN = Pattern.compile(DATASTREAMS_CONFIGURATION_REGEX);

    private final MqttDatastreamsOrchestrator mqttDatastreamsOrchestrator;

    private MqttDatastreamsConfiguration currentConfiguration;
    private List<DatastreamInfoWithPermission> initialDatastreamsConfiguration = new ArrayList<>();

    MqttDatastreamsConfigurationUpdateHandler(MqttDatastreamsOrchestrator mqttDatastreamsOrchestrator) {
        this.mqttDatastreamsOrchestrator = mqttDatastreamsOrchestrator;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        String brokerURI = getRequiredConfigurationParameter(props, SERVER_URI_PROPERTY_NAME);
        String clientId =
                Optional.ofNullable((String) props.get(CLIENT_ID_PROPERTY_NAME)).orElse(UUID.randomUUID().toString());
        String enableTopic = getRequiredConfigurationParameter(props, ENABLE_DATASTREAM_TOPIC_PROPERTY_NAME);
        String disableTopic = getRequiredConfigurationParameter(props, DISABLE_DATASTREAM_TOPIC_PROPERTY_NAME);
        String eventTopic = getRequiredConfigurationParameter(props, EVENT_TOPIC_PROPERTY_NAME);
        String readRequestTopic = getRequiredConfigurationParameter(props, READ_REQUEST_TOPIC_PROPERTY_NAME);
        String readResponseTopic = getRequiredConfigurationParameter(props, READ_RESPONSE_TOPIC_PROPERTY_NAME);
        String writeRequestTopic = getRequiredConfigurationParameter(props, WRITE_REQUEST_TOPIC_PROPERTY_NAME);
        String writeResponseTopic = getRequiredConfigurationParameter(props, WRITE_RESPONSE_TOPIC_PROPERTY_NAME);
        String lwtTopic = getRequiredConfigurationParameter(props, LWT_TOPIC_PROPERTY_NAME);

        currentConfiguration = new MqttDatastreamsConfiguration(brokerURI, clientId, enableTopic, disableTopic,
                eventTopic, readRequestTopic, readResponseTopic, writeRequestTopic, writeResponseTopic, lwtTopic);

        initialDatastreamsConfiguration = loadInitialDatastreamsConfiguration(props);
    }

    private List<DatastreamInfoWithPermission> loadInitialDatastreamsConfiguration(Dictionary<String, ?> props) {
        Map<String, ?> mapProperties = Collections.dictionaryToMap(props);

        return mapProperties.entrySet().stream()
                .map(entry -> {
                    Matcher matcher = DATASTREAMS_CONFIGURATION_PATTERN.matcher(entry.getKey());
                    if (matcher.matches()) {
                        return new DatastreamInfoWithPermission(matcher.group(1), matcher.group(2),
                                MqttDatastreamPermission.valueOf((String) entry.getValue()));
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String getRequiredConfigurationParameter(Dictionary<String, ?> props, String parameterName) {
        return Optional.ofNullable((String) props.get(parameterName))
                .orElseThrow(() -> new ConfigurationException("Missing required parameter: " + parameterName));
    }

    @Override
    public void applyConfiguration() throws MqttException {
        mqttDatastreamsOrchestrator.loadConfiguration(currentConfiguration, initialDatastreamsConfiguration);
    }
}
