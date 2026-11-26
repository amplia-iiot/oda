package es.amplia.oda.hardware.udp;

import es.amplia.oda.core.commons.udp.UdpService;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.udp.configuration.JavaUdpConfigurationUpdateHandler;
import es.amplia.oda.hardware.udp.udp.JavaUdpService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

	private final Activator testActivator = new Activator();

	@Mock
	BundleContext mockedContext;
	@Mock
	JavaUdpService mockedUdpService;
	@Mock
	ServiceRegistration<UdpService> mockedRegistration;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;

	@Test
	public void testStart() throws Exception {
		List<List<?>> configurationHandlerArgs = new ArrayList<>();
		List<List<?>> configurableBundleArgs = new ArrayList<>();

		try (MockedConstruction<JavaUdpService> udpServiceCons = mockConstruction(JavaUdpService.class);
			 MockedConstruction<JavaUdpConfigurationUpdateHandler> configurationHandlerCons =
					 mockConstruction(JavaUdpConfigurationUpdateHandler.class,
							 (mock, mctx) -> configurationHandlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
					 mockConstruction(ConfigurableBundleImpl.class,
							 (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {
			testActivator.start(mockedContext);
			assertEquals(1, udpServiceCons.constructed().size());
			assertEquals(1, configurationHandlerCons.constructed().size());
			assertEquals(udpServiceCons.constructed().get(0), configurationHandlerArgs.get(0).get(0));
			assertEquals(1, configurableBundleCons.constructed().size());
			assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
			assertEquals(configurationHandlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
		}
	}

	@Test
	public void testStop() throws Exception {
		Whitebox.setInternalState(testActivator, "udpService", mockedUdpService);
		Whitebox.setInternalState(testActivator, "udpServiceRegistration", mockedRegistration);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
		testActivator.stop(mockedContext);
		Mockito.verify(mockedConfigurableBundle).close();
		Mockito.verify(mockedRegistration).unregister();
		Mockito.verify(mockedUdpService).stop();
	}
}
