package es.amplia.oda.hardware.diozero;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.diozero.analog.DioZeroAdcService;
import es.amplia.oda.hardware.diozero.configuration.DioZeroConfigurationHandler;
import es.amplia.oda.hardware.diozero.gpio.DioZeroGpioService;

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
import java.util.Arrays;
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
	DioZeroAdcService mockedAdcService;
	@Mock
	DioZeroGpioService mockedGpioService;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	BundleContext mockedContext;
	@Mock
	ServiceRegistration<AdcService> mockedAdcRegistration;
	@Mock
	ServiceRegistration<GpioService> mockedGpioRegistration;
	@Mock
	Bundle mockedBundle;


	@Test
	public void testStart() throws Exception {
		when(mockedContext.registerService(eq(AdcService.class), any(), any())).thenReturn(mockedAdcRegistration);
		when(mockedContext.registerService(eq(GpioService.class), any(), any())).thenReturn(mockedGpioRegistration);
		when(mockedContext.getBundle()).thenReturn(mockedBundle);
		when(mockedBundle.getSymbolicName()).thenReturn("");

		List<List<?>> handlerArgs = new ArrayList<>();
		List<List<?>> configurableBundleArgs = new ArrayList<>();

		try (MockedConstruction<DioZeroAdcService> adcServiceCons = mockConstruction(DioZeroAdcService.class);
			 MockedConstruction<DioZeroGpioService> gpioServiceCons = mockConstruction(DioZeroGpioService.class);
			 MockedConstruction<DioZeroConfigurationHandler> handlerCons =
					 mockConstruction(DioZeroConfigurationHandler.class,
							 (mock, mctx) -> handlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
					 mockConstruction(ConfigurableBundleImpl.class,
							 (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

			testActivator.start(mockedContext);

			assertEquals(1, adcServiceCons.constructed().size());
			assertEquals(1, gpioServiceCons.constructed().size());
			assertEquals(1, handlerCons.constructed().size());
			assertEquals(adcServiceCons.constructed().get(0), handlerArgs.get(0).get(0));
			assertEquals(gpioServiceCons.constructed().get(0), handlerArgs.get(0).get(1));
			verify(mockedContext).registerService(eq(AdcService.class), eq(adcServiceCons.constructed().get(0)), any());
			verify(mockedContext).registerService(eq(GpioService.class), eq(gpioServiceCons.constructed().get(0)), any());
			assertEquals(1, configurableBundleCons.constructed().size());
			assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
			assertEquals(handlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
			assertEquals(Arrays.asList(mockedAdcRegistration, mockedGpioRegistration),
					configurableBundleArgs.get(0).get(2));
		}
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "adcServiceRegistration", mockedAdcRegistration);
		Whitebox.setInternalState(testActivator, "gpioServiceRegistration", mockedGpioRegistration);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(testActivator, "adcService", mockedAdcService);
		Whitebox.setInternalState(testActivator, "gpioService", mockedGpioService);

		testActivator.stop(mockedContext);

		verify(mockedAdcRegistration).unregister();
		verify(mockedGpioRegistration).unregister();
		verify(mockedConfigurableBundle).close();
		verify(mockedAdcService).close();
		verify(mockedGpioService).close();
	}
}
