package es.amplia.oda.connector.iec104;

import es.amplia.oda.comms.iec104.Iec104Cache;
import es.amplia.oda.connector.iec104.configuration.Iec104ConnectorConfigurationUpdateHandler;
import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.osgi.proxies.ScadaDispatcherProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mockConstruction;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {
	private final Activator testActivator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private ScadaDispatcherProxy mockedDispatcher;
	@Mock
	private Iec104Connector mockedConnector;
	@Mock
	private ServiceRegistration<ScadaTableInfo> mockedRegistration;
	@Mock
	private ServiceListenerBundle<?> mockedListener;
	@Mock
	private ConfigurableBundleImpl mockedConfigurable;
	@Mock
	private Iec104ConnectorConfigurationUpdateHandler mockedConfigurationHandler;

	@Test
	public void testStart() throws Exception {
		List<List<?>> dispatcherArgs = new ArrayList<>();
		List<List<?>> cacheArgs = new ArrayList<>();
		List<List<?>> connectorArgs = new ArrayList<>();
		List<List<?>> configHandlerArgs = new ArrayList<>();
		List<List<?>> configurableArgs = new ArrayList<>();
		List<List<?>> serviceListenerArgs = new ArrayList<>();

		try (MockedConstruction<ScadaDispatcherProxy> dispatcherCons =
					 mockConstruction(ScadaDispatcherProxy.class,
							 (mock, mctx) -> dispatcherArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<Iec104Cache> cacheCons =
					 mockConstruction(Iec104Cache.class,
							 (mock, mctx) -> cacheArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<Iec104Connector> connectorCons =
					 mockConstruction(Iec104Connector.class,
							 (mock, mctx) -> connectorArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<Iec104ConnectorConfigurationUpdateHandler> configHandlerCons =
					 mockConstruction(Iec104ConnectorConfigurationUpdateHandler.class,
							 (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configurableCons =
					 mockConstruction(ConfigurableBundleImpl.class,
							 (mock, mctx) -> configurableArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ServiceListenerBundle> serviceListenerCons =
					 mockConstruction(ServiceListenerBundle.class,
							 (mock, mctx) -> serviceListenerArgs.add(new ArrayList<>(mctx.arguments())))) {

			testActivator.start(mockedContext);

			assertEquals(1, dispatcherCons.constructed().size());
			assertEquals(mockedContext, dispatcherArgs.get(0).get(0));
			assertEquals(1, cacheCons.constructed().size());
			assertNull(cacheArgs.get(0).get(0));
			assertEquals(1, connectorCons.constructed().size());
			assertEquals(cacheCons.constructed().get(0), connectorArgs.get(0).get(0));
			assertEquals(dispatcherCons.constructed().get(0), connectorArgs.get(0).get(1));
			assertEquals(1, configHandlerCons.constructed().size());
			assertEquals(connectorCons.constructed().get(0), configHandlerArgs.get(0).get(0));
			assertEquals(1, configurableCons.constructed().size());
			assertEquals(mockedContext, configurableArgs.get(0).get(0));
			assertEquals(configHandlerCons.constructed().get(0), configurableArgs.get(0).get(1));
			assertEquals(1, serviceListenerCons.constructed().size());
			assertEquals(mockedContext, serviceListenerArgs.get(0).get(0));
			assertEquals(ScadaTableInfo.class, serviceListenerArgs.get(0).get(1));
			assertTrue(serviceListenerArgs.get(0).get(2) instanceof Runnable);
		}
	}

	@Test
	public void testOnServiceChange() {
		Whitebox.setInternalState(testActivator, "configHandler", mockedConfigurationHandler);

		testActivator.onServiceChange();

		Mockito.verify(mockedConfigurationHandler).applyConfiguration();
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "scadaConnectorServiceRegistration", mockedRegistration);
		Whitebox.setInternalState(testActivator, "serviceListenerBundle", mockedListener);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurable);
		Whitebox.setInternalState(testActivator, "connector", mockedConnector);
		Whitebox.setInternalState(testActivator, "dispatcher", mockedDispatcher);

		testActivator.stop(mockedContext);

		Mockito.verify(mockedRegistration).unregister();
		Mockito.verify(mockedListener).close();
		Mockito.verify(mockedConfigurable).close();
		Mockito.verify(mockedConnector).close();
		Mockito.verify(mockedDispatcher).close();
	}
}
