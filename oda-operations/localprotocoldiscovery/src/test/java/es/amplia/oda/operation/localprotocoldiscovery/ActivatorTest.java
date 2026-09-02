package es.amplia.oda.operation.localprotocoldiscovery;

import es.amplia.oda.comms.mqtt.api.MqttClientFactory;
import es.amplia.oda.comms.mqtt.api.MqttClientFactoryProxy;
import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.mqtt.MqttDatastreamsService;
import es.amplia.oda.core.commons.osgi.proxies.MqttDatastreamsServiceProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.operation.api.OperationDiscover;
import es.amplia.oda.operation.localprotocoldiscovery.configuration.LocalProtocolDiscoveryConfigurationUpdateHandler;

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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

	private final Activator testActivator = new Activator();

	@Mock
	private BundleContext mockedContext;
	@Mock
	private MqttClientFactoryProxy mockedFactory;
	@Mock
	private MqttDatastreamsServiceProxy mockedMqttDatastreamsService;
	@Mock
	private SerializerProxy mockedSerializer;
	@Mock
	private LocalProtocolDiscoveryConfigurationUpdateHandler mockedConfigHandler;
	@Mock
	private ConfigurableBundleImpl mockedConfigBundle;
	@Mock
	private ServiceListenerBundle<MqttClientFactory> mockedListener;
	@Mock
	private ServiceListenerBundle<MqttDatastreamsService> mockedDatastreamsListener;
	@Mock
	private ServiceListenerBundle<Serializer> mockedSerializerListener;
	@Mock
	private ServiceRegistration<OperationDiscover> mockedRegistration;

	@Test
	public void testStart() throws Exception {
		List<List<?>> factoryArgs = new ArrayList<>();
		List<List<?>> datastreamsServiceArgs = new ArrayList<>();
		List<List<?>> serializerArgs = new ArrayList<>();
		List<List<?>> operationArgs = new ArrayList<>();
		List<List<?>> configHandlerArgs = new ArrayList<>();
		List<List<?>> configBundleArgs = new ArrayList<>();
		List<List<?>> listenerArgs = new ArrayList<>();
		try (MockedConstruction<MqttClientFactoryProxy> factoryCons = mockConstruction(MqttClientFactoryProxy.class,
					 (mock, mctx) -> factoryArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<MqttDatastreamsServiceProxy> datastreamsServiceCons =
					 mockConstruction(MqttDatastreamsServiceProxy.class,
							 (mock, mctx) -> datastreamsServiceArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<SerializerProxy> serializerCons = mockConstruction(SerializerProxy.class,
					 (mock, mctx) -> serializerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<OperationLocalProtocolDiscoveryImpl> operationCons =
					 mockConstruction(OperationLocalProtocolDiscoveryImpl.class,
							 (mock, mctx) -> operationArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<LocalProtocolDiscoveryConfigurationUpdateHandler> configHandlerCons =
					 mockConstruction(LocalProtocolDiscoveryConfigurationUpdateHandler.class,
							 (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configBundleCons =
					 mockConstruction(ConfigurableBundleImpl.class,
							 (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ServiceListenerBundle> listenerCons = mockConstruction(ServiceListenerBundle.class,
					 (mock, mctx) -> listenerArgs.add(new ArrayList<>(mctx.arguments())))) {

			testActivator.start(mockedContext);

			assertEquals(1, factoryCons.constructed().size());
			assertEquals(mockedContext, factoryArgs.get(0).get(0));
			assertEquals(1, datastreamsServiceCons.constructed().size());
			assertEquals(mockedContext, datastreamsServiceArgs.get(0).get(0));
			assertEquals(1, serializerCons.constructed().size());
			assertEquals(mockedContext, serializerArgs.get(0).get(0));
			assertEquals(ContentType.CBOR, serializerArgs.get(0).get(1));
			assertEquals(1, operationCons.constructed().size());
			assertEquals(factoryCons.constructed().get(0), operationArgs.get(0).get(0));
			assertEquals(datastreamsServiceCons.constructed().get(0), operationArgs.get(0).get(1));
			assertEquals(serializerCons.constructed().get(0), operationArgs.get(0).get(2));
			assertEquals(1, configHandlerCons.constructed().size());
			assertEquals(operationCons.constructed().get(0), configHandlerArgs.get(0).get(0));
			assertEquals(1, configBundleCons.constructed().size());
			assertEquals(mockedContext, configBundleArgs.get(0).get(0));
			assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
			verify(mockedContext).registerService(eq(OperationDiscover.class),
					eq(operationCons.constructed().get(0)), any());
			assertEquals(3, listenerCons.constructed().size());
			assertEquals(mockedContext, listenerArgs.get(0).get(0));
			assertEquals(MqttClientFactory.class, listenerArgs.get(0).get(1));
			assertTrue(listenerArgs.get(0).get(2) instanceof Runnable);
			assertEquals(mockedContext, listenerArgs.get(1).get(0));
			assertEquals(MqttDatastreamsService.class, listenerArgs.get(1).get(1));
			assertTrue(listenerArgs.get(1).get(2) instanceof Runnable);
			assertEquals(mockedContext, listenerArgs.get(2).get(0));
			assertEquals(Serializer.class, listenerArgs.get(2).get(1));
			assertTrue(listenerArgs.get(2).get(2) instanceof Runnable);
		}
	}

	@Test
	public void testOnServiceChanged() {
		Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);

		testActivator.onServiceChanged();

		verify(mockedConfigHandler).applyConfiguration();
	}

	@Test
	public void testOnServiceChangedWithExceptionIsCaught() {
		Whitebox.setInternalState(testActivator, "configHandler", mockedConfigHandler);
		Mockito.doThrow(RuntimeException.class).when(mockedConfigHandler).applyConfiguration();

		testActivator.onServiceChanged();

		assertTrue("Exception should be caught", true);
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "serializerServiceListener", mockedSerializerListener);
		Whitebox.setInternalState(testActivator, "mqttDatastreamsServiceListener", mockedDatastreamsListener);
		Whitebox.setInternalState(testActivator, "mqttClientFactoryListener", mockedListener);
		Whitebox.setInternalState(testActivator, "registration", mockedRegistration);
		Whitebox.setInternalState(testActivator, "mqttClientFactory", mockedFactory);
		Whitebox.setInternalState(testActivator, "mqttDatastreamsService", mockedMqttDatastreamsService);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
		Whitebox.setInternalState(testActivator, "serializer", mockedSerializer);

		testActivator.stop(mockedContext);

		verify(mockedSerializerListener).close();
		verify(mockedDatastreamsListener).close();
		verify(mockedListener).close();
		verify(mockedRegistration).unregister();
		verify(mockedFactory).close();
		verify(mockedMqttDatastreamsService).close();
		verify(mockedConfigBundle).close();
		verify(mockedSerializer).close();
	}
}
