package es.amplia.oda.datastreams.lora;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.osgi.proxies.EventPublisherProxy;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;
import es.amplia.oda.core.commons.osgi.proxies.UdpServiceProxy;
import es.amplia.oda.core.commons.udp.UdpService;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.datastreams.lora.configuration.LoraDatastreamsConfigurationHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

	@Mock
	UdpServiceProxy mockedService;
	@Mock
	SerializerProxy mockedSerializer;
	@Mock
	EventPublisherProxy mockedPublisher;
	@Mock
	LoraDatastreamsOrchestrator mockedOrchestrator;
	@Mock
	LoraDatastreamsConfigurationHandler mockedHandler;
	@Mock
	ServiceListenerBundle<UdpService> mockedServiceListener;
	@Mock
	ConfigurableBundleImpl mockedConfigurableBundle;
	@Mock
	BundleContext mockedContext;

	private final Activator testActivator = new Activator();

	@Test
	public void testStart() throws Exception {
		List<List<?>> udpServiceArgs = new ArrayList<>();
		List<List<?>> serializerArgs = new ArrayList<>();
		List<List<?>> publisherArgs = new ArrayList<>();
		List<List<?>> orchestratorArgs = new ArrayList<>();
		List<List<?>> handlerArgs = new ArrayList<>();
		List<List<?>> serviceListenerArgs = new ArrayList<>();
		List<List<?>> configurableBundleArgs = new ArrayList<>();

		try (MockedConstruction<UdpServiceProxy> udpServiceCons = mockConstruction(UdpServiceProxy.class,
					(mock, mctx) -> udpServiceArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<SerializerProxy> serializerCons = mockConstruction(SerializerProxy.class,
					(mock, mctx) -> serializerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<EventPublisherProxy> publisherCons = mockConstruction(EventPublisherProxy.class,
					(mock, mctx) -> publisherArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<LoraDatastreamsOrchestrator> orchestratorCons = mockConstruction(LoraDatastreamsOrchestrator.class,
					(mock, mctx) -> orchestratorArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<LoraDatastreamsConfigurationHandler> handlerCons = mockConstruction(LoraDatastreamsConfigurationHandler.class,
					(mock, mctx) -> handlerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ServiceListenerBundle> serviceListenerCons = mockConstruction(ServiceListenerBundle.class,
					(mock, mctx) -> serviceListenerArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<ConfigurableBundleImpl> configurableBundleCons = mockConstruction(ConfigurableBundleImpl.class,
					(mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

			testActivator.start(mockedContext);

			assertEquals(1, udpServiceCons.constructed().size());
			assertEquals(mockedContext, udpServiceArgs.get(0).get(0));
			assertEquals(1, serializerCons.constructed().size());
			assertEquals(mockedContext, serializerArgs.get(0).get(0));
			assertEquals(ContentType.JSON, serializerArgs.get(0).get(1));
			assertEquals(1, publisherCons.constructed().size());
			assertEquals(mockedContext, publisherArgs.get(0).get(0));
			assertEquals(1, orchestratorCons.constructed().size());
			assertEquals(udpServiceCons.constructed().get(0), orchestratorArgs.get(0).get(0));
			assertEquals(publisherCons.constructed().get(0), orchestratorArgs.get(0).get(1));
			assertEquals(serializerCons.constructed().get(0), orchestratorArgs.get(0).get(2));
			assertEquals(1, handlerCons.constructed().size());
			assertEquals(orchestratorCons.constructed().get(0), handlerArgs.get(0).get(0));
			assertEquals(1, configurableBundleCons.constructed().size());
			assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
			assertEquals(handlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
			assertEquals(1, serviceListenerCons.constructed().size());
			assertEquals(mockedContext, serviceListenerArgs.get(0).get(0));
			assertEquals(UdpService.class, serviceListenerArgs.get(0).get(1));
		}
	}

	@Test
	public void testOnServiceChanged() {
		Whitebox.setInternalState(testActivator, "configurationHandler", mockedHandler);

		testActivator.onServiceChanged();

		verify(mockedHandler).applyConfiguration();
	}

	@Test
	public void testOnServiceChangedWithException() {
		Whitebox.setInternalState(testActivator, "configurationHandler", mockedHandler);
		doThrow(RuntimeException.class).when(mockedHandler).applyConfiguration();

		testActivator.onServiceChanged();

		verify(mockedHandler).applyConfiguration();
	}

	@Test
	public void testStop() {
		Whitebox.setInternalState(testActivator, "udpServiceServiceListener", mockedServiceListener);
		Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
		Whitebox.setInternalState(testActivator, "loraDatastreamsOrchestrator", mockedOrchestrator);
		Whitebox.setInternalState(testActivator, "eventPublisher", mockedPublisher);
		Whitebox.setInternalState(testActivator, "serializer", mockedSerializer);
		Whitebox.setInternalState(testActivator, "udpService", mockedService);

		testActivator.stop(mockedContext);

		verify(mockedServiceListener).close();
		verify(mockedConfigurableBundle).close();
		verify(mockedOrchestrator).close();
		verify(mockedPublisher).close();
		verify(mockedSerializer).close();
		verify(mockedService).close();
	}
}
