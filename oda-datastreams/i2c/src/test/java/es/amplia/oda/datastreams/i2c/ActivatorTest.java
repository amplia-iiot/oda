package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.I2CServiceProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.datastreams.i2c.configuration.DatastreamI2CConfigurationHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {
	private Activator testActivator = new Activator();

	@Mock
	I2CServiceProxy mockedProxy;
	@Mock
	I2CDatastreamsRegistry mockedRegistry;
	@Mock
	DatastreamI2CConfigurationHandler mockedHandler;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	ServiceListenerBundle mockedServiceListener;
	@Mock
	ServiceListenerBundle mockedProviderListener;
	@Mock
	BundleContext mockedContext;

	@Test
	public void testStart() throws Exception {
		whenNew(I2CServiceProxy.class).withAnyArguments().thenReturn(mockedProxy);
		whenNew(I2CDatastreamsRegistry.class).withAnyArguments().thenReturn(mockedRegistry);
		whenNew(DatastreamI2CConfigurationHandler.class).withAnyArguments().thenReturn(mockedHandler);
		whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		whenNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(I2CService.class), any()).thenReturn(mockedServiceListener);
		whenNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(DeviceInfoProvider.class), any()).thenReturn(mockedProviderListener);

		testActivator.start(mockedContext);

		verifyNew(I2CServiceProxy.class).withArguments(eq(mockedContext));
		verifyNew(I2CDatastreamsRegistry.class).withArguments(eq(mockedContext), eq(mockedProxy));
		verifyNew(DatastreamI2CConfigurationHandler.class).withArguments(eq(mockedRegistry), eq(mockedProxy));
		verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedHandler));
		verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(I2CService.class), any());
		verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(DeviceInfoProvider.class), any());
	}

	@Test
	public void testOnServiceChanged() {
		doNothing().when(mockedHandler).applyConfiguration();

		testActivator.onServiceChanged(mockedHandler);

		verify(mockedHandler).applyConfiguration();
	}

	@Test
	public void testOnServiceChangedWithException() {
		doThrow(new NullPointerException("")).when(mockedHandler).applyConfiguration();

		testActivator.onServiceChanged(mockedHandler);
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "deviceInfoProviderServiceListener", mockedProviderListener);
		Whitebox.setInternalState(testActivator, "i2cDatastreamsRegistry", mockedRegistry);
		Whitebox.setInternalState(testActivator, "i2cServiceListener", mockedServiceListener);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(testActivator, "i2cService", mockedProxy);

		testActivator.stop(mockedContext);

		verify(mockedProviderListener).close();
		verify(mockedRegistry).close();
		verify(mockedServiceListener).close();
		verify(mockedConfigurableBundle).close();
		verify(mockedProxy).close();
	}
}