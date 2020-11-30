package es.amplia.oda.operation.localprotocoldiscovery.configuration;

import lombok.Value;

@Value
public class LocalProtocolDiscoveryConfiguration {
	String serverURI;
	String clientId;
	String discoverTopic;
}
