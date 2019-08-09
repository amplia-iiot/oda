package es.amplia.oda.connector.iec104.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Iec104ConnectorConfiguration {
	private String localAddress;
	private int localPort;
	private int originatorAddress;
	private int commonAddress;
	private boolean spontaneousEnabled;
}
