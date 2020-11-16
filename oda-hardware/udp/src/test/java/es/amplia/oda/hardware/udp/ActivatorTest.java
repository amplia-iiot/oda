package es.amplia.oda.hardware.udp;

import es.amplia.oda.core.commons.udp.UdpService;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.udp.configuration.JavaUdpConfigurationUpdateHandler;
import es.amplia.oda.hardware.udp.udp.JavaUdpService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
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
	@Mock
	JavaUdpConfigurationUpdateHandler mockedConfigurationHandler;

	@Test
	public void testStart() throws Exception {
		PowerMockito.whenNew(JavaUdpService.class).withNoArguments().thenReturn(mockedUdpService);
		PowerMockito.whenNew(JavaUdpConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedConfigurationHandler);
		PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		testActivator.start(mockedContext);
		PowerMockito.verifyNew(JavaUdpService.class).withNoArguments();
		PowerMockito.verifyNew(JavaUdpConfigurationUpdateHandler.class).withArguments(eq(mockedUdpService));
		PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigurationHandler));
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
