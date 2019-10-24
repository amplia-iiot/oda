package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.osgi.proxies.I2CServiceProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.i2c.configuration.DatastreamI2CConfigurationHandler;

import es.amplia.oda.datastreams.i2c.datastreams.I2CDatastreamsFactoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

	private final Activator testActivator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private I2CServiceProxy mockedI2CService;
	@Mock
	private I2CDatastreamsFactoryImpl mockedFactory;
	@Mock
	private ServiceRegistrationManagerOsgi<DatastreamsGetter> mockedGetterRegistrationManager;
	@Mock
	private ServiceRegistrationManagerOsgi<DatastreamsSetter> mockedSetterRegistrationManager;
	@Mock
	private I2CDatastreamsRegistry mockedRegistry;
	@Mock
	private DatastreamI2CConfigurationHandler mockedConfigHandler;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	private ServiceListenerBundle<I2CService> mockedI2CServiceListener;

	@Test
	public void testStart() throws Exception {
		whenNew(I2CServiceProxy.class).withAnyArguments().thenReturn(mockedI2CService);
		whenNew(I2CDatastreamsFactoryImpl.class).withAnyArguments().thenReturn(mockedFactory);
		whenNew(ServiceRegistrationManagerOsgi.class)
				.withArguments(any(BundleContext.class), eq(DatastreamsGetter.class))
				.thenReturn(mockedGetterRegistrationManager);
		whenNew(ServiceRegistrationManagerOsgi.class)
				.withArguments(any(BundleContext.class), eq(DatastreamsSetter.class))
				.thenReturn(mockedSetterRegistrationManager);
		whenNew(I2CDatastreamsRegistry.class).withAnyArguments().thenReturn(mockedRegistry);
		whenNew(DatastreamI2CConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
		whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedI2CServiceListener);

		testActivator.start(mockedContext);

		verifyNew(I2CServiceProxy.class).withArguments(eq(mockedContext));
		verifyNew(I2CDatastreamsFactoryImpl.class).withArguments(eq(mockedI2CService));
		verifyNew(ServiceRegistrationManagerOsgi.class).withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
		verifyNew(ServiceRegistrationManagerOsgi.class).withArguments(eq(mockedContext), eq(DatastreamsSetter.class));
		verifyNew(I2CDatastreamsRegistry.class).withArguments(eq(mockedFactory), eq(mockedGetterRegistrationManager),
				eq(mockedSetterRegistrationManager));
		verifyNew(DatastreamI2CConfigurationHandler.class).withArguments(eq(mockedRegistry), eq(mockedI2CService));
		verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
		verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(I2CService.class), any());
	}

	@Test
	public void testOnServiceChanged() {
		Whitebox.setInternalState(testActivator, "configurationHandler", mockedConfigHandler);

		testActivator.onServiceChanged();

		verify(mockedConfigHandler).applyConfiguration();
	}

	@Test
	public void testOnServiceChangedExceptionIsCaught() {
		Whitebox.setInternalState(testActivator, "configurationHandler", mockedConfigHandler);

		doThrow(new RuntimeException("")).when(mockedConfigHandler).applyConfiguration();

		testActivator.onServiceChanged();

		assertTrue("Runtime Exception should be caught", true);
		verify(mockedConfigHandler).applyConfiguration();
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "i2cService", mockedI2CService);
		Whitebox.setInternalState(testActivator, "i2cDatastreamsRegistry", mockedRegistry);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(testActivator, "i2cServiceListener", mockedI2CServiceListener);

		testActivator.stop(mockedContext);

		verify(mockedI2CServiceListener).close();
		verify(mockedConfigurableBundle).close();
		verify(mockedRegistry).close();
		verify(mockedI2CService).close();
	}
}