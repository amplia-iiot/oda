package es.amplia.oda.datastreams.lora;

import es.amplia.oda.core.commons.interfaces.EventPublisher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.udp.UdpService;
import es.amplia.oda.datastreams.lora.configuration.LoraDatastreamsConfiguration;
import es.amplia.oda.datastreams.lora.datastreams.LoraDatastreamsEvent;
import es.amplia.oda.datastreams.lora.datastreams.LoraDatastreamsFactory;
import es.amplia.oda.datastreams.lora.datastreams.LoraException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LoraDatastreamsOrchestratorTest {

	private static final String TEST_DEVICE_ID_PROPERTY_VALUE = "testing_gateway";

	@Mock
	UdpService mockedService;
	@Mock
	EventPublisher mockedPublisher;
	@Mock
	Serializer mockedSerializer;
	@InjectMocks
	LoraDatastreamsOrchestrator testOrchestrator;
	@Mock
	LoraDatastreamsEvent mockedDatastreamEvent;

	LoraDatastreamsConfiguration testConfiguration;

	@Before
	public void setUp() {
		testConfiguration = LoraDatastreamsConfiguration.builder().deviceId(TEST_DEVICE_ID_PROPERTY_VALUE).build();
	}

	@Test
	public void testLoadConfiguration() throws Exception {
		List<List<?>> factoryArgs = new ArrayList<>();
		try (MockedConstruction<LoraDatastreamsFactory> factoryCons = mockConstruction(LoraDatastreamsFactory.class,
				(mock, mctx) -> {
					factoryArgs.add(new ArrayList<>(mctx.arguments()));
					when(mock.createLoraDatastreamsEvent(any())).thenReturn(mockedDatastreamEvent);
				})) {

			testOrchestrator.loadConfiguration(testConfiguration);

			assertEquals(1, factoryCons.constructed().size());
			assertEquals(mockedService, factoryArgs.get(0).get(0));
			assertEquals(mockedPublisher, factoryArgs.get(0).get(1));
			assertEquals(mockedSerializer, factoryArgs.get(0).get(2));
			LoraDatastreamsEvent datastreamsEvent = Whitebox.getInternalState(testOrchestrator, "loraDatastreamsEvent");
			assertEquals(mockedDatastreamEvent, datastreamsEvent);
		}
	}

	@Test
	public void testLoadConfigurationWithInitialConfig() throws Exception {
		Whitebox.setInternalState(testOrchestrator, "loraDatastreamsEvent", mockedDatastreamEvent);
		List<List<?>> factoryArgs = new ArrayList<>();
		try (MockedConstruction<LoraDatastreamsFactory> factoryCons = mockConstruction(LoraDatastreamsFactory.class,
				(mock, mctx) -> {
					factoryArgs.add(new ArrayList<>(mctx.arguments()));
					when(mock.createLoraDatastreamsEvent(any())).thenReturn(mockedDatastreamEvent);
				})) {

			testOrchestrator.loadConfiguration(testConfiguration);

			verify(mockedDatastreamEvent).unregisterFromEventSource();
			assertEquals(1, factoryCons.constructed().size());
			assertEquals(mockedService, factoryArgs.get(0).get(0));
			assertEquals(mockedPublisher, factoryArgs.get(0).get(1));
			assertEquals(mockedSerializer, factoryArgs.get(0).get(2));
			LoraDatastreamsEvent datastreamsEvent = Whitebox.getInternalState(testOrchestrator, "loraDatastreamsEvent");
			assertEquals(mockedDatastreamEvent, datastreamsEvent);
		}
	}

	@Test
	public void testLoadConfigurationWithInitialConfigAndException() throws Exception {
		Whitebox.setInternalState(testOrchestrator, "loraDatastreamsEvent", mockedDatastreamEvent);
		doThrow(LoraException.class).when(mockedDatastreamEvent).unregisterFromEventSource();
		List<List<?>> factoryArgs = new ArrayList<>();
		try (MockedConstruction<LoraDatastreamsFactory> factoryCons = mockConstruction(LoraDatastreamsFactory.class,
				(mock, mctx) -> {
					factoryArgs.add(new ArrayList<>(mctx.arguments()));
					when(mock.createLoraDatastreamsEvent(any())).thenReturn(mockedDatastreamEvent);
				})) {

			testOrchestrator.loadConfiguration(testConfiguration);

			verify(mockedDatastreamEvent).unregisterFromEventSource();
			assertEquals(1, factoryCons.constructed().size());
			assertEquals(mockedService, factoryArgs.get(0).get(0));
			assertEquals(mockedPublisher, factoryArgs.get(0).get(1));
			assertEquals(mockedSerializer, factoryArgs.get(0).get(2));
			LoraDatastreamsEvent datastreamsEvent = Whitebox.getInternalState(testOrchestrator, "loraDatastreamsEvent");
			assertEquals(mockedDatastreamEvent, datastreamsEvent);
		}
	}

	@Test
	public void testClose() {
		Whitebox.setInternalState(testOrchestrator, "loraDatastreamsEvent", mockedDatastreamEvent);

		testOrchestrator.close();

		verify(mockedDatastreamEvent).unregisterFromEventSource();
	}
}
