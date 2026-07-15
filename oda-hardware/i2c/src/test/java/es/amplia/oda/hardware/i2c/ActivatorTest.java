package es.amplia.oda.hardware.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.hardware.i2c.configuration.DioZeroI2CConfigurationHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
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
		when(mockedContext.getBundle()).thenReturn(mockedBundle);
		when(mockedBundle.getSymbolicName()).thenReturn(testSymbolicName);

		List<List<?>> handlerArgs = new ArrayList<>();
		List<List<?>> configurableBundleArgs = new ArrayList<>();

		try (MockedConstruction<DioZeroI2CService> serviceCons = mockConstruction(DioZeroI2CService.class);
			 MockedConstruction<DioZeroI2CConfigurationHandler> handlerCons =
					 mockConstruction(DioZeroI2CConfigurationHandler.class,
							 (mock, mctx) -> handlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
					 mockConstruction(ConfigurableBundleImpl.class,
							 (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

			testActivator.start(mockedContext);

			assertEquals(1, serviceCons.constructed().size());
			assertEquals(1, handlerCons.constructed().size());
			assertEquals(serviceCons.constructed().get(0), handlerArgs.get(0).get(0));
			assertEquals(1, configurableBundleCons.constructed().size());
			assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
			assertEquals(handlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
		}
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
