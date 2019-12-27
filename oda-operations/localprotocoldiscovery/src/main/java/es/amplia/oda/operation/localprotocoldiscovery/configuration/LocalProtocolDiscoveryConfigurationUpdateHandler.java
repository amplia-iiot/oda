package es.amplia.oda.operation.localprotocoldiscovery.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.operation.localprotocoldiscovery.OperationLocalProtocolDiscoveryImpl;

import java.util.Dictionary;
import java.util.Optional;
import java.util.UUID;

public class LocalProtocolDiscoveryConfigurationUpdateHandler implements ConfigurationUpdateHandler {

	static final String SERVER_URI_PROPERTY_NAME = "brokerURI";
	static final String CLIENT_ID_PROPERTY_NAME = "clientId";
	static final String DISCOVERY_TOPIC_PROPERTY_NAME = "discoverTopic";


	private final OperationLocalProtocolDiscoveryImpl operationDiscover;
	private LocalProtocolDiscoveryConfiguration currentConfiguration;


	public LocalProtocolDiscoveryConfigurationUpdateHandler(OperationLocalProtocolDiscoveryImpl operationDiscover) {
		this.operationDiscover = operationDiscover;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		String brokerURI = Optional.ofNullable((String) props.get(SERVER_URI_PROPERTY_NAME)).orElseThrow(
				() -> new ConfigurationException("Missing required parameter: " + SERVER_URI_PROPERTY_NAME));
		String clientId = Optional.ofNullable((String) props.get(CLIENT_ID_PROPERTY_NAME))
				.orElse(UUID.randomUUID().toString());
		String discoverTopic = Optional.ofNullable((String) props.get(DISCOVERY_TOPIC_PROPERTY_NAME)).orElseThrow(
				() -> new ConfigurationException("Missing required parameter: " + DISCOVERY_TOPIC_PROPERTY_NAME));

		currentConfiguration = new LocalProtocolDiscoveryConfiguration(brokerURI, clientId, discoverTopic);
	}

	@Override
	public void applyConfiguration() {
		operationDiscover.loadConfiguration(currentConfiguration.getServerURI(), currentConfiguration.getClientId(),
				currentConfiguration.getDiscoverTopic());
	}
}
