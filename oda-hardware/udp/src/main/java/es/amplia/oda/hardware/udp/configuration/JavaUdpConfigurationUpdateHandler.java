package es.amplia.oda.hardware.udp.configuration;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.udp.udp.JavaUdpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.Dictionary;
import java.util.Optional;

public class JavaUdpConfigurationUpdateHandler implements ConfigurationUpdateHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaUdpConfigurationUpdateHandler.class);

	static final String HOST_DIRECTION_PROPERTY_NAME = "host";
	static final String UPLINK_PORT_PROPERTY_NAME = "uplinkPort";
	static final String DOWNLINK_PORT_PROPERTY_NAME = "downlinkPort";
	static final String PACKET_MAX_SIZE_PROPERTY_NAME = "packetSize";
	private static final int DEFAULT_PACKET_MAX_SIZE = 512;

	private final JavaUdpService javaUdpService;
	private JavaUdpConfiguration currentConfiguration;

	public JavaUdpConfigurationUpdateHandler(JavaUdpService javaUdpService) {
		this.javaUdpService = javaUdpService;
	}

	@Override
	public void loadConfiguration(Dictionary<String, ?> props) {
		LOGGER.info("Loading new configuration");
		String host = Optional.ofNullable(props.get(HOST_DIRECTION_PROPERTY_NAME)).map(String::valueOf)
				.orElse("localhost");
		int uplinkPort = Optional.ofNullable(props.get(UPLINK_PORT_PROPERTY_NAME)).map(String::valueOf).map(Integer::parseInt)
				.orElseThrow(() -> new IllegalArgumentException("Missing required property " + UPLINK_PORT_PROPERTY_NAME));
		int downlinkPort = Optional.ofNullable(props.get(DOWNLINK_PORT_PROPERTY_NAME)).map(String::valueOf).map(Integer::parseInt)
				.orElseThrow(() -> new IllegalArgumentException("Missing required property " + DOWNLINK_PORT_PROPERTY_NAME));
		int packetSize = Optional.ofNullable(props.get(PACKET_MAX_SIZE_PROPERTY_NAME)).map(String::valueOf)
				.map(Integer::parseInt).orElse(DEFAULT_PACKET_MAX_SIZE);

		currentConfiguration = new JavaUdpConfiguration(host, uplinkPort, downlinkPort, packetSize);
		LOGGER.info("New configuration loaded");
	}

	@Override
	public void applyConfiguration() {
		javaUdpService.loadConfiguration(currentConfiguration.getHost(), currentConfiguration.getUplinkPort(), currentConfiguration.getDownlinkPort(),
				currentConfiguration.getPacketSize());
	}
}
