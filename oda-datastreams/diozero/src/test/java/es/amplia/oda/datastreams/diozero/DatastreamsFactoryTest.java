package es.amplia.oda.datastreams.diozero;

import es.amplia.oda.core.commons.diozero.AdcService;
import es.amplia.oda.datastreams.diozero.datastreams.adc.AdcDatastreamsEvent;
import es.amplia.oda.datastreams.diozero.datastreams.adc.AdcDatastreamsGetter;
import es.amplia.oda.event.api.EventDispatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
public class DatastreamsFactoryTest {

	@Mock
	AdcService mockedService;
	@Mock
	Executor mockedExecutor;
	@Mock
	EventDispatcher mockedEventDispatcher;

	@Test
	public void testCreateAdcDatastreamsGetter() {
		String datastreamId = "testDatastream";
		int pinIndex = 0;

		AdcDatastreamsGetter getter = DatastreamsFactory.createAdcDatastreamsGetter(datastreamId, pinIndex, mockedService, mockedExecutor);

		assertNotNull(getter);
		assertEquals(datastreamId, getter.getDatastreamIdSatisfied());
	}

	@Test
	public void testCreateAdcDatastreamsEvent() {
		String datastreamId = "testDatastream";
		int pinIndex = 0;

		AdcDatastreamsEvent event = DatastreamsFactory.createAdcDatastreamsEvent(datastreamId, pinIndex, mockedService, mockedEventDispatcher);

		assertNotNull(event);
	}
}
