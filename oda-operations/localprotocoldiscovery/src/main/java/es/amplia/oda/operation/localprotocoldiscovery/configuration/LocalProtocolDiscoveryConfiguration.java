package es.amplia.oda.operation.localprotocoldiscovery.configuration;

import lombok.Value;

@Value
public class LocalProtocolDiscoveryConfiguration {
	private String serverURI;
	private String clientId;
	private String discoverTopic;
}
