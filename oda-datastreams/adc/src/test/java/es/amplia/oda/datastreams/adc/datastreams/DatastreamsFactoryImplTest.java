package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.event.api.EventDispatcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DatastreamsFactoryImpl.class)
public class DatastreamsFactoryImplTest {

	private static final String TEST_DATASTREAM = "testDatastream";
	private static final int TEST_PIN_INDEX = 1;
	private static final double TEST_MINIMUM = 0;
	private static final double TEST_MAXIMUM = 1;


	@Mock
	private AdcService mockedService;
	@Mock
	private EventDispatcher mockedEventDispatcher;
	@InjectMocks
	private DatastreamsFactoryImpl testFactory;

	@Mock
	private AdcDatastreamsGetter mockedGetter;
	@Mock
	private AdcDatastreamsEvent mockedEvent;


	@Test
	public void testCreateAdcDatastreamsGetter() throws Exception {
		PowerMockito.whenNew(AdcDatastreamsGetter.class).withAnyArguments().thenReturn(mockedGetter);

		AdcDatastreamsGetter getter = testFactory.createAdcDatastreamsGetter(
				TEST_DATASTREAM, TEST_PIN_INDEX, TEST_MINIMUM, TEST_MAXIMUM);

		assertNotNull(getter);
		PowerMockito.verifyNew(AdcDatastreamsGetter.class).withArguments(eq(TEST_DATASTREAM), eq(TEST_PIN_INDEX),
				eq(mockedService), eq(TEST_MINIMUM), eq(TEST_MAXIMUM));
	}

	@Test
	public void testCreateAdcDatastreamsEvent() throws Exception {
		PowerMockito.whenNew(AdcDatastreamsEvent.class).withAnyArguments().thenReturn(mockedEvent);

		AdcDatastreamsEvent event = testFactory.createAdcDatastreamsEvent(TEST_DATASTREAM, TEST_PIN_INDEX, TEST_MINIMUM, TEST_MAXIMUM);

		assertNotNull(event);
		PowerMockito.verifyNew(AdcDatastreamsEvent.class).withArguments(eq(TEST_DATASTREAM), eq(TEST_PIN_INDEX),
				eq(mockedService), eq(mockedEventDispatcher), eq(TEST_MINIMUM), eq(TEST_MAXIMUM));
	}
}
