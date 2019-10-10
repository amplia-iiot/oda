package es.amplia.oda.datastreams.adc;

import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.adc.datastreams.AdcDatastreamsEvent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatastreamsRegistryTest {

	private static final int TEST_INDEX = 1;
	private static final String TEST_DATASTREAM = "testDatastream";


	@Mock
	private DatastreamsFactory mockedFactory;
	@Mock
	private ServiceRegistrationManagerOsgi<DatastreamsGetter> mockedDatastreamsGetterRegistrationManager;
	@InjectMocks
	private DatastreamsRegistry testRegistry;

	@Mock
	private DatastreamsGetter mockedGetter;
	@Mock
	private DatastreamsEvent mockedEvent;


	@Test
	public void testAddAdcDatastreamsGetter() {
		when(mockedFactory.createAdcDatastreamsGetter(anyString(), anyInt())).thenReturn(mockedGetter);

		testRegistry.addAdcDatastreamGetter(TEST_INDEX, TEST_DATASTREAM);

		verify(mockedFactory).createAdcDatastreamsGetter(eq(TEST_DATASTREAM), eq(TEST_INDEX));
		verify(mockedDatastreamsGetterRegistrationManager).register(eq(mockedGetter));
	}

	@Test
	public void testAddAdcDatastreamsEvent() {
		when(mockedFactory.createAdcDatastreamsEvent(anyString(), anyInt())).thenReturn(mockedEvent);

		testRegistry.addAdcDatastreamEvent(TEST_INDEX, TEST_DATASTREAM);

		verify(mockedFactory).createAdcDatastreamsEvent(eq(TEST_DATASTREAM), eq(TEST_INDEX));
		verify(mockedEvent).registerToEventSource();
		Map<String, AdcDatastreamsEvent> datastreamsEvents = getDatastreamsEvents();
		assertEquals(1, datastreamsEvents.size());
		assertTrue(datastreamsEvents.containsKey(TEST_DATASTREAM));
		assertEquals(mockedEvent, datastreamsEvents.get(TEST_DATASTREAM));
	}

	@SuppressWarnings("unchecked")
	private Map<String, AdcDatastreamsEvent> getDatastreamsEvents() {
		return (Map<String, AdcDatastreamsEvent>) Whitebox.getInternalState(testRegistry, "datastreamsEvents");
	}

	@Test
	public void testAddAdcDatastreamsEventUnregisteringFirst() {
		AdcDatastreamsEvent oldEvent = mock(AdcDatastreamsEvent.class);
		Map<String, DatastreamsEvent> datastreamsEvents = new HashMap<>();
		datastreamsEvents.put(TEST_DATASTREAM, oldEvent);

		Whitebox.setInternalState(testRegistry, "datastreamsEvents", datastreamsEvents);

		when(mockedFactory.createAdcDatastreamsEvent(anyString(), anyInt())).thenReturn(mockedEvent);

		testRegistry.addAdcDatastreamEvent(TEST_INDEX, TEST_DATASTREAM);

		verify(oldEvent).unregisterFromEventSource();
		verify(mockedFactory).createAdcDatastreamsEvent(eq(TEST_DATASTREAM), eq(TEST_INDEX));
		verify(mockedEvent).registerToEventSource();
		assertEquals(1, datastreamsEvents.size());
		assertTrue(datastreamsEvents.containsKey(TEST_DATASTREAM));
		assertEquals(mockedEvent, datastreamsEvents.get(TEST_DATASTREAM));
	}

	@Test
	public void testClose() {
		Map<String, DatastreamsEvent> datastreamsEvents = new HashMap<>();
		datastreamsEvents.put(TEST_DATASTREAM, mockedEvent);

		Whitebox.setInternalState(testRegistry, "datastreamsEvents", datastreamsEvents);

		testRegistry.close();

		verify(mockedDatastreamsGetterRegistrationManager).unregister();
		verify(mockedEvent).unregisterFromEventSource();
		assertTrue(datastreamsEvents.isEmpty());
	}
}
