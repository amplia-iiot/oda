package es.amplia.oda.hardware.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.i2c.configuration.DioZeroI2CConfigurationHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ActivatorTest {
	private final Activator testActivator = new Activator();

	@Mock
	DioZeroI2CService mockedService;
	@Mock
	DioZeroI2CConfigurationHandler mockedHandler;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	BundleContext mockedContext;
	@Mock
	Bundle mockedBundle;
	@Mock
	ServiceRegistration<I2CService> mockedServiceRegistration;

	@Test
	public void testStart() throws Exception {
		String testSymbolicName = "Symme";
		whenNew(DioZeroI2CService.class).withAnyArguments().thenReturn(mockedService);
		whenNew(DioZeroI2CConfigurationHandler.class).withAnyArguments().thenReturn(mockedHandler);
		whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		when(mockedContext.getBundle()).thenReturn(mockedBundle);
		when(mockedBundle.getSymbolicName()).thenReturn(testSymbolicName);

		testActivator.start(mockedContext);

		verifyNew(DioZeroI2CService.class).withNoArguments();
		verifyNew(DioZeroI2CConfigurationHandler.class).withArguments(eq(mockedService));
		verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedHandler), any());
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "i2cService", mockedService);
		Whitebox.setInternalState(testActivator, "i2cServiceRegistration", mockedServiceRegistration);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);

		testActivator.stop(mockedContext);

		verify(mockedService).close();
		verify(mockedServiceRegistration).unregister();
		verify(mockedConfigurableBundle).close();
	}
}
