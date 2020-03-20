package es.amplia.oda.connector.iec104.configuration;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Iec104ConnectorConfiguration {
	@NonNull
	private String localAddress;
	@NonNull
	private int localPort;
	@NonNull
	private int originatorAddress;
	@NonNull
	private int commonAddress;
	@NonNull
	private boolean spontaneousEnabled;
}
