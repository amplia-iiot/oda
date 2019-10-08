package es.amplia.oda.datastreams.diozero;

import es.amplia.oda.core.commons.diozero.AdcChannel;
import es.amplia.oda.core.commons.diozero.AdcService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.datastreams.diozero.datastreams.adc.AdcDatastreamsEvent;
import es.amplia.oda.event.api.EventDispatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class DatastreamsRegistryTest {

	@Mock
	BundleContext mockedContext;
	@Mock
	AdcService mockedService;
	@Mock
	EventDispatcher mockedDispatcher;
	@InjectMocks
	DatastreamsRegistry registry;

	private Map<String, AdcDatastreamsEvent> adcDatastreamsEvents;
	private List<ServiceRegistration<?>> datastreamsServiceRegistrations;

	@Mock
	AdcChannel mockedChannel;
	@Mock
	ServiceRegistration<DatastreamsGetter> mockedRegistration;

	@Before
	public void prepareForTest() {
		adcDatastreamsEvents = new HashMap<>();
		datastreamsServiceRegistrations = new ArrayList<>();
		Whitebox.setInternalState(registry, "adcDatastreamsEvents", adcDatastreamsEvents);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAddGpioDatastreamGetter() {
		registry.addGpioDatastreamGetter(0, "");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAddGpioDatastreamSetter() {
		registry.addGpioDatastreamSetter(0, "");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testAddGpioDatastreamEvent() {
		registry.addGpioDatastreamEvent(0, "");
	}

	@Test
	public void testAddAdcDatastreamsGetter() {
		String datastreamId = "testDatastream";
		int pinIndex = 0;
		when(mockedService.getChannelByIndex(pinIndex)).thenReturn(mockedChannel);
		when(mockedContext.registerService(eq(DatastreamsGetter.class), any(), any())).thenReturn(mockedRegistration);

		registry.addAdcDatastreamGetter(pinIndex, datastreamId);

		datastreamsServiceRegistrations = (List<ServiceRegistration<?>>) Whitebox.getInternalState(registry, "datastreamsServiceRegistrations");
		assertEquals(1, datastreamsServiceRegistrations.size());
		assertNotNull(datastreamsServiceRegistrations.get(pinIndex));
	}

	@Test
	public void testAddAdcDatastreamsEvent() {
		String datastreamId = "testDatastream";
		int pinIndex = 0;
		when(mockedService.getChannelByIndex(pinIndex)).thenReturn(mockedChannel);

		registry.addAdcDatastreamEvent(pinIndex, datastreamId);

		adcDatastreamsEvents = (Map<String, AdcDatastreamsEvent>) Whitebox.getInternalState(registry, "adcDatastreamsEvents");
		assertEquals(1, adcDatastreamsEvents.size());
		assertNotNull(adcDatastreamsEvents.get(datastreamId));
	}

	@Test
	public void testAddAdcDatastreamsEventUnregisteringFirst() {
		String datastreamId = "testDatastream";
		int pinIndex = 0;
		when(mockedService.getChannelByIndex(pinIndex)).thenReturn(mockedChannel);

		registry.addAdcDatastreamEvent(pinIndex, datastreamId);
		registry.addAdcDatastreamEvent(pinIndex, datastreamId);

		adcDatastreamsEvents = (Map<String, AdcDatastreamsEvent>) Whitebox.getInternalState(registry, "adcDatastreamsEvents");
		assertEquals(1, adcDatastreamsEvents.size());
		assertNotNull(adcDatastreamsEvents.get(datastreamId));
	}

	@Test
	public void testClose() {
		String datastreamId = "testDatastream";
		int pinIndex = 0;
		datastreamsServiceRegistrations.add(mockedRegistration);
		adcDatastreamsEvents.put(datastreamId, new AdcDatastreamsEvent(datastreamId, pinIndex, mockedService, mockedDispatcher));
		Whitebox.setInternalState(registry, "datastreamsServiceRegistrations", datastreamsServiceRegistrations);
		Whitebox.setInternalState(registry, "adcDatastreamsEvents", adcDatastreamsEvents);

		registry.close();

		assertEquals(0, datastreamsServiceRegistrations.size());
		assertEquals(0, adcDatastreamsEvents.size());
	}
}
