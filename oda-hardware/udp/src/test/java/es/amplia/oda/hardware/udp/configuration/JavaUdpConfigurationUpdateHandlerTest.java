package es.amplia.oda.hardware.udp.configuration;

import es.amplia.oda.hardware.udp.udp.JavaUdpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import java.util.Dictionary;
import java.util.Hashtable;

import static es.amplia.oda.hardware.udp.configuration.JavaUdpConfigurationUpdateHandler.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class JavaUdpConfigurationUpdateHandlerTest {

	private static final String TEST_HOST_VALUE = "localhost";
	private static final int TEST_UPLINK_PORT_VALUE = 1002;
	private static final int TEST_DOWNLINK_PORT_VALUE = 1008;
	private static final int TEST_PACKET_SIZE_VALUE = 2048;
	private static final JavaUdpConfiguration TEST_JAVA_UDP_CONFIGURATION =
			JavaUdpConfiguration.builder().host(TEST_HOST_VALUE).uplinkPort(TEST_UPLINK_PORT_VALUE)
				.downlinkPort(TEST_DOWNLINK_PORT_VALUE).packetSize(TEST_PACKET_SIZE_VALUE).build();

	@Mock
	JavaUdpService mockedService;
	@InjectMocks
	JavaUdpConfigurationUpdateHandler testHandler;

	@Test
	public void testLoadConfiguration() throws Exception {
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(HOST_DIRECTION_PROPERTY_NAME, TEST_HOST_VALUE);
		props.put(UPLINK_PORT_PROPERTY_NAME, TEST_UPLINK_PORT_VALUE);
		props.put(DOWNLINK_PORT_PROPERTY_NAME, TEST_DOWNLINK_PORT_VALUE);
		props.put(PACKET_MAX_SIZE_PROPERTY_NAME, TEST_PACKET_SIZE_VALUE);

		testHandler.loadConfiguration(props);

		JavaUdpConfiguration config = Whitebox.getInternalState(testHandler, "currentConfiguration");
		assertEquals(TEST_JAVA_UDP_CONFIGURATION, config);
	}

	@Test
	public void applyConfiguration() {
		Whitebox.setInternalState(testHandler, "currentConfiguration", TEST_JAVA_UDP_CONFIGURATION);

		testHandler.applyConfiguration();

		verify(mockedService).loadConfiguration(eq(TEST_HOST_VALUE), eq(TEST_UPLINK_PORT_VALUE), eq(TEST_DOWNLINK_PORT_VALUE), eq(TEST_PACKET_SIZE_VALUE));
	}
}
