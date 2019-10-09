package es.amplia.oda.datastreams.diozero;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.AdcServiceProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.datastreams.diozero.configuration.DatastreamsAdcConfigurationHandler;
import es.amplia.oda.event.api.EventDispatcherProxy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

	private Activator activator = new Activator();

	@Mock
	AdcServiceProxy mockedService;
	@Mock
	EventDispatcherProxy mockedEventDispatcher;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	DatastreamsRegistry mockedRegistry;
	@Mock
	DatastreamsAdcConfigurationHandler mockedHandler;
	@Mock
	ServiceListenerBundle mockedListener;
	@Mock
	BundleContext mockedContext;
	@Mock
	ConfigurationUpdateHandler mockedConfigurationUpdateHandler;

	@Test
	public void testStart() throws Exception {
		whenNew(AdcServiceProxy.class).withAnyArguments().thenReturn(mockedService);
		whenNew(EventDispatcherProxy.class).withAnyArguments().thenReturn(mockedEventDispatcher);
		whenNew(DatastreamsRegistry.class).withAnyArguments().thenReturn(mockedRegistry);
		whenNew(DatastreamsAdcConfigurationHandler.class).withAnyArguments().thenReturn(mockedHandler);
		whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
		whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedListener);
		
		activator.start(mockedContext);
		
		verifyNew(AdcServiceProxy.class).withArguments(eq(mockedContext));
		verifyNew(EventDispatcherProxy.class).withArguments(eq(mockedContext));
		verifyNew(DatastreamsRegistry.class).withArguments(eq(mockedContext), eq(mockedService), eq(mockedEventDispatcher));
		verifyNew(DatastreamsAdcConfigurationHandler.class).withArguments(eq(mockedRegistry), eq(mockedService));
		verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedHandler));
		verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(AdcService.class), any());
		verifyNew(ServiceListenerBundle.class).withArguments(eq(mockedContext), eq(DeviceInfoProvider.class), any());
	}

	@Test
	public void testOnServiceChanged() throws Exception {
		activator.onServiceChanged(mockedConfigurationUpdateHandler);

		verify(mockedConfigurationUpdateHandler).applyConfiguration();
	}

	@Test
	public void testOnServiceChangedWithException() throws Exception {
		doThrow(new NumberFormatException()).when(mockedConfigurationUpdateHandler).applyConfiguration();

		activator.onServiceChanged(mockedConfigurationUpdateHandler);
	}

	@Test
	public void testStop() throws Exception {
		Whitebox.setInternalState(activator, "deviceInfoProviderServiceListener", mockedListener);
		Whitebox.setInternalState(activator, "adcServiceListener", mockedListener);
		Whitebox.setInternalState(activator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(activator, "adcService", mockedService);
		Whitebox.setInternalState(activator, "eventDispatcher", mockedEventDispatcher);

		activator.stop(mockedContext);

		verify(mockedListener, times(2)).close();
		verify(mockedConfigurableBundle).close();
		verify(mockedService).close();
		verify(mockedEventDispatcher).close();
	}
}
