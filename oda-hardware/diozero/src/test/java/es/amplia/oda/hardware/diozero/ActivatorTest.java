package es.amplia.oda.hardware.diozero;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcService;
import es.amplia.oda.hardware.diozero.configuration.DioZeroConfigurationHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

	private final Activator testActivator = new Activator();

	@Mock
	DioZeroAdcService mockedService;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	BundleContext mockedContext;
	@Mock
	ServiceRegistration<AdcService> mockedRegistration;
	@Mock
	Bundle mockedBundle;


	@Test
	public void testStart() throws Exception {
		when(mockedContext.registerService(eq(AdcService.class), any(), any())).thenReturn(mockedRegistration);
		when(mockedContext.getBundle()).thenReturn(mockedBundle);
		when(mockedBundle.getSymbolicName()).thenReturn("");

		List<List<?>> handlerArgs = new ArrayList<>();
		List<List<?>> configurableBundleArgs = new ArrayList<>();

		try (MockedConstruction<DioZeroAdcService> serviceCons = mockConstruction(DioZeroAdcService.class);
			 MockedConstruction<DioZeroConfigurationHandler> handlerCons =
					 mockConstruction(DioZeroConfigurationHandler.class,
							 (mock, mctx) -> handlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
					 mockConstruction(ConfigurableBundleImpl.class,
							 (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

			testActivator.start(mockedContext);

			assertEquals(1, serviceCons.constructed().size());
			assertEquals(1, handlerCons.constructed().size());
			assertEquals(serviceCons.constructed().get(0), handlerArgs.get(0).get(0));
			verify(mockedContext).registerService(eq(AdcService.class), eq(serviceCons.constructed().get(0)), any());
			assertEquals(1, configurableBundleCons.constructed().size());
			assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
			assertEquals(handlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
			assertEquals(Collections.singletonList(mockedRegistration), configurableBundleArgs.get(0).get(2));
		}
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator,"adcServiceRegistration", mockedRegistration);
		Whitebox.setInternalState(testActivator,"configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(testActivator,"adcService", mockedService);

		testActivator.stop(mockedContext);

		verify(mockedRegistration).unregister();
		verify(mockedConfigurableBundle).close();
		verify(mockedService).close();
	}
}
