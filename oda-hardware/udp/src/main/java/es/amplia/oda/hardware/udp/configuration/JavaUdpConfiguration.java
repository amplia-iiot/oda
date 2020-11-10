package es.amplia.oda.hardware.udp.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class JavaUdpConfiguration {
	String host;
	int uplinkPort;
	int downlinkPort;
	int packetSize;
}
