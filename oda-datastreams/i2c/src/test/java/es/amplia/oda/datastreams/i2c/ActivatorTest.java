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
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

	private final Activator testActivator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private I2CServiceProxy mockedI2CService;
	@Mock
	private I2CDatastreamsRegistry mockedRegistry;
	@Mock
	private DatastreamI2CConfigurationHandler mockedConfigHandler;
	@Mock
	private ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	private ServiceListenerBundle<I2CService> mockedI2CServiceListener;

	@Test
	public void testStart() {
		List<List<?>> factoryArgs = new ArrayList<>();
		List<List<?>> registrationManagerArgs = new ArrayList<>();
		List<List<?>> registryArgs = new ArrayList<>();
		List<List<?>> configHandlerArgs = new ArrayList<>();
		List<List<?>> configurableBundleArgs = new ArrayList<>();
		List<List<?>> serviceListenerArgs = new ArrayList<>();

		try (MockedConstruction<I2CServiceProxy> proxyCons = mockConstruction(I2CServiceProxy.class);
			 MockedConstruction<I2CDatastreamsFactoryImpl> factoryCons = mockConstruction(I2CDatastreamsFactoryImpl.class,
					 (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
					 mockConstruction(ServiceRegistrationManagerOsgi.class,
							 (mock, mctx) -> registrationManagerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<I2CDatastreamsRegistry> registryCons = mockConstruction(I2CDatastreamsRegistry.class,
					 (mock, mctx) -> registryArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<DatastreamI2CConfigurationHandler> configHandlerCons =
					 mockConstruction(DatastreamI2CConfigurationHandler.class,
							 (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
					 mockConstruction(ConfigurableBundleImpl.class,
							 (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ServiceListenerBundle> serviceListenerCons =
					 mockConstruction(ServiceListenerBundle.class,
							 (mock, mctx) -> serviceListenerArgs.add(new ArrayList<>(mctx.arguments())))) {

			testActivator.start(mockedContext);

			assertEquals(1, proxyCons.constructed().size());
			assertEquals(1, factoryCons.constructed().size());
			assertEquals(proxyCons.constructed().get(0), factoryArgs.get(0).get(0));
			assertEquals(2, registrationManagerCons.constructed().size());
			assertEquals(mockedContext, registrationManagerArgs.get(0).get(0));
			assertEquals(DatastreamsGetter.class, registrationManagerArgs.get(0).get(1));
			assertEquals(mockedContext, registrationManagerArgs.get(1).get(0));
			assertEquals(DatastreamsSetter.class, registrationManagerArgs.get(1).get(1));
			assertEquals(1, registryCons.constructed().size());
			assertEquals(factoryCons.constructed().get(0), registryArgs.get(0).get(0));
			assertEquals(registrationManagerCons.constructed().get(0), registryArgs.get(0).get(1));
			assertEquals(registrationManagerCons.constructed().get(1), registryArgs.get(0).get(2));
			assertEquals(1, configHandlerCons.constructed().size());
			assertEquals(registryCons.constructed().get(0), configHandlerArgs.get(0).get(0));
			assertEquals(proxyCons.constructed().get(0), configHandlerArgs.get(0).get(1));
			assertEquals(1, configurableBundleCons.constructed().size());
			assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
			assertEquals(configHandlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
			assertEquals(1, serviceListenerCons.constructed().size());
			assertEquals(mockedContext, serviceListenerArgs.get(0).get(0));
			assertEquals(I2CService.class, serviceListenerArgs.get(0).get(1));
		}
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
