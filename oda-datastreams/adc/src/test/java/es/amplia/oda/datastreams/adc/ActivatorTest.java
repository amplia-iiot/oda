package es.amplia.oda.datastreams.adc;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.AdcServiceProxy;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.adc.configuration.DatastreamsAdcConfigurationHandler;
import es.amplia.oda.datastreams.adc.datastreams.DatastreamsFactoryImpl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ActivatorTest {

	private final Activator activator = new Activator();

	@Mock
	private AdcServiceProxy mockedService;
	@Mock
	private EventPublisherProxy mockedEventPublisher;
	@Mock
	private DatastreamsFactoryImpl mockedDatastreamsFactory;
	@Mock
	private ServiceRegistrationManagerOsgi<DatastreamsGetter> mockedDatastreamsGetterRegistrationManager;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	private DatastreamsRegistry mockedRegistry;
	@Mock
	private DatastreamsAdcConfigurationHandler mockedHandler;
	@Mock
	private ServiceListenerBundle mockedListener;
	@Mock
	private BundleContext mockedContext;
	@Mock
	private ConfigurationUpdateHandler mockedConfigurationUpdateHandler;

	@Test
	public void testStart() throws Exception {
		whenNew(AdcServiceProxy.class).withAnyArguments().thenReturn(mockedService);
		whenNew(EventPublisherProxy.class).withAnyArguments().thenReturn(mockedEventPublisher);
		whenNew(DatastreamsFactoryImpl.class).withAnyArguments().thenReturn(mockedDatastreamsFactory);
		whenNew(ServiceRegistrationManagerOsgi.class).withAnyArguments()
				.thenReturn(mockedDatastreamsGetterRegistrationManager);
		whenNew(DatastreamsRegistry.class).withAnyArguments().thenReturn(mockedRegistry);
		whenNew(DatastreamsAdcConfigurationHandler.class).withAnyArguments().thenReturn(mockedHandler);
		whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedListener);
		
		activator.start(mockedContext);
		
		verifyNew(AdcServiceProxy.class).withArguments(eq(mockedContext));
		verifyNew(EventPublisherProxy.class).withArguments(eq(mockedContext));
		verifyNew(DatastreamsFactoryImpl.class).withArguments(eq(mockedService), eq(mockedEventPublisher));
		verifyNew(ServiceRegistrationManagerOsgi.class).withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
		verifyNew(DatastreamsRegistry.class)
				.withArguments(eq(mockedDatastreamsFactory), eq(mockedDatastreamsGetterRegistrationManager));
		verifyNew(DatastreamsAdcConfigurationHandler.class).withArguments(eq(mockedRegistry), eq(mockedService));
		verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedHandler));
		verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(AdcService.class), any());
		verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(DeviceInfoProvider.class), any());
	}

	@Test
	public void testOnServiceChanged() {
		Whitebox.setInternalState(activator, "configurationUpdateHandler", mockedConfigurationUpdateHandler);

		activator.onServiceChanged();

		verify(mockedConfigurationUpdateHandler).applyConfiguration();
	}

	@Test
	public void testOnServiceChangedExceptionIsCaught() {
		doThrow(new NumberFormatException()).when(mockedConfigurationUpdateHandler).applyConfiguration();

		activator.onServiceChanged();

		assertTrue("Exception should be caught", true);
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(activator, "deviceInfoProviderServiceListener", mockedListener);
		Whitebox.setInternalState(activator, "adcServiceListener", mockedListener);
		Whitebox.setInternalState(activator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(activator, "adcService", mockedService);
		Whitebox.setInternalState(activator, "eventPublisher", mockedEventPublisher);

		activator.stop(mockedContext);

		verify(mockedListener, times(2)).close();
		verify(mockedConfigurableBundle).close();
		verify(mockedService).close();
		verify(mockedEventPublisher).close();
	}
}
