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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LoraDatastreamsOrchestrator.class)
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
	LoraDatastreamsFactory mockedFactory;
	@Mock
	LoraDatastreamsEvent mockedDatastreamEvent;

	LoraDatastreamsConfiguration testConfiguration;

	@Before
	public void setUp() {
		testConfiguration = LoraDatastreamsConfiguration.builder().deviceId(TEST_DEVICE_ID_PROPERTY_VALUE).build();
	}

	@Test
	public void testLoadConfiguration() throws Exception {
		whenNew(LoraDatastreamsFactory.class).withAnyArguments().thenReturn(mockedFactory);
		when(mockedFactory.createLoraDatastreamsEvent(any())).thenReturn(mockedDatastreamEvent);

		testOrchestrator.loadConfiguration(testConfiguration);

		verifyNew(LoraDatastreamsFactory.class).withArguments(mockedService,mockedPublisher, mockedSerializer);
		LoraDatastreamsEvent datastreamsEvent = Whitebox.getInternalState(testOrchestrator, "loraDatastreamsEvent");
		assertEquals(mockedDatastreamEvent, datastreamsEvent);
	}

	@Test
	public void testLoadConfigurationWithInitialConfig() throws Exception {
		Whitebox.setInternalState(testOrchestrator, "loraDatastreamsEvent", mockedDatastreamEvent);
		whenNew(LoraDatastreamsFactory.class).withAnyArguments().thenReturn(mockedFactory);
		when(mockedFactory.createLoraDatastreamsEvent(any())).thenReturn(mockedDatastreamEvent);

		testOrchestrator.loadConfiguration(testConfiguration);

		verify(mockedDatastreamEvent).unregisterFromEventSource();
		verifyNew(LoraDatastreamsFactory.class).withArguments(mockedService,mockedPublisher, mockedSerializer);
		LoraDatastreamsEvent datastreamsEvent = Whitebox.getInternalState(testOrchestrator, "loraDatastreamsEvent");
		assertEquals(mockedDatastreamEvent, datastreamsEvent);
	}

	@Test
	public void testLoadConfigurationWithInitialConfigAndException() throws Exception {
		Whitebox.setInternalState(testOrchestrator, "loraDatastreamsEvent", mockedDatastreamEvent);
		whenNew(LoraDatastreamsFactory.class).withAnyArguments().thenReturn(mockedFactory);
		doThrow(LoraException.class).when(mockedDatastreamEvent).unregisterFromEventSource();
		when(mockedFactory.createLoraDatastreamsEvent(any())).thenReturn(mockedDatastreamEvent);

		testOrchestrator.loadConfiguration(testConfiguration);

		verify(mockedDatastreamEvent).unregisterFromEventSource();
		verifyNew(LoraDatastreamsFactory.class).withArguments(mockedService,mockedPublisher, mockedSerializer);
		LoraDatastreamsEvent datastreamsEvent = Whitebox.getInternalState(testOrchestrator, "loraDatastreamsEvent");
		assertEquals(mockedDatastreamEvent, datastreamsEvent);
	}

	@Test
	public void testClose() {
		Whitebox.setInternalState(testOrchestrator, "loraDatastreamsEvent", mockedDatastreamEvent);

		testOrchestrator.close();

		verify(mockedDatastreamEvent).unregisterFromEventSource();
	}
}
