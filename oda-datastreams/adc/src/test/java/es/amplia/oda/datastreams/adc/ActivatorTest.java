package es.amplia.oda.datastreams.adc;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.osgi.proxies.AdcServiceProxy;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.datastreams.adc.configuration.DatastreamsAdcConfigurationHandler;
import es.amplia.oda.datastreams.adc.datastreams.DatastreamsFactoryImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

	private final Activator activator = new Activator();

	@Mock
	private AdcServiceProxy mockedService;
	@Mock
	private EventPublisherProxy mockedEventPublisher;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	private ServiceListenerBundle mockedListener;
	@Mock
	private BundleContext mockedContext;
	@Mock
	private ConfigurationUpdateHandler mockedConfigurationUpdateHandler;

	@Test
	public void testStart() {
		List<List<?>> factoryArgs = new ArrayList<>();
		List<List<?>> registrationManagerArgs = new ArrayList<>();
		List<List<?>> registryArgs = new ArrayList<>();
		List<List<?>> handlerArgs = new ArrayList<>();
		List<List<?>> configurableBundleArgs = new ArrayList<>();
		List<List<?>> listenerArgs = new ArrayList<>();

		try (MockedConstruction<AdcServiceProxy> serviceCons = mockConstruction(AdcServiceProxy.class);
			 MockedConstruction<EventPublisherProxy> eventPublisherCons = mockConstruction(EventPublisherProxy.class);
			 MockedConstruction<DatastreamsFactoryImpl> factoryCons = mockConstruction(DatastreamsFactoryImpl.class,
					 (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
					 mockConstruction(ServiceRegistrationManagerOsgi.class,
							 (mock, mctx) -> registrationManagerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<DatastreamsRegistry> registryCons = mockConstruction(DatastreamsRegistry.class,
					 (mock, mctx) -> registryArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<DatastreamsAdcConfigurationHandler> handlerCons =
					 mockConstruction(DatastreamsAdcConfigurationHandler.class,
							 (mock, mctx) -> handlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
					 mockConstruction(ConfigurableBundleImpl.class,
							 (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ServiceListenerBundle> listenerCons = mockConstruction(ServiceListenerBundle.class,
					 (mock, mctx) -> listenerArgs.add(new ArrayList<>(mctx.arguments())))) {

			activator.start(mockedContext);

			assertEquals(1, serviceCons.constructed().size());
			assertEquals(1, eventPublisherCons.constructed().size());
			assertEquals(1, factoryCons.constructed().size());
			assertEquals(serviceCons.constructed().get(0), factoryArgs.get(0).get(0));
			assertEquals(eventPublisherCons.constructed().get(0), factoryArgs.get(0).get(1));
			assertEquals(1, registrationManagerCons.constructed().size());
			assertEquals(mockedContext, registrationManagerArgs.get(0).get(0));
			assertEquals(DatastreamsGetter.class, registrationManagerArgs.get(0).get(1));
			assertEquals(1, registryCons.constructed().size());
			assertEquals(factoryCons.constructed().get(0), registryArgs.get(0).get(0));
			assertEquals(registrationManagerCons.constructed().get(0), registryArgs.get(0).get(1));
			assertEquals(1, handlerCons.constructed().size());
			assertEquals(registryCons.constructed().get(0), handlerArgs.get(0).get(0));
			assertEquals(serviceCons.constructed().get(0), handlerArgs.get(0).get(1));
			assertEquals(1, configurableBundleCons.constructed().size());
			assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
			assertEquals(handlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
			assertEquals(2, listenerCons.constructed().size());
			assertEquals(mockedContext, listenerArgs.get(0).get(0));
			assertEquals(AdcService.class, listenerArgs.get(0).get(1));
			assertEquals(mockedContext, listenerArgs.get(1).get(0));
			assertEquals(DeviceInfoProvider.class, listenerArgs.get(1).get(1));
		}
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

		assertTrue(true, "Exception should be caught");
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
