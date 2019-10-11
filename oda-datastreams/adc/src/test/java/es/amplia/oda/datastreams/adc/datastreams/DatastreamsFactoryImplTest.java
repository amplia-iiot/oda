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

		AdcDatastreamsGetter getter = testFactory.createAdcDatastreamsGetter(TEST_DATASTREAM, TEST_PIN_INDEX);

		assertNotNull(getter);
		PowerMockito.verifyNew(AdcDatastreamsGetter.class).withArguments(eq(TEST_DATASTREAM), eq(TEST_PIN_INDEX),
				eq(mockedService));
	}

	@Test
	public void testCreateAdcDatastreamsEvent() throws Exception {
		PowerMockito.whenNew(AdcDatastreamsEvent.class).withAnyArguments().thenReturn(mockedEvent);

		AdcDatastreamsEvent event = testFactory.createAdcDatastreamsEvent(TEST_DATASTREAM, TEST_PIN_INDEX);

		assertNotNull(event);
		PowerMockito.verifyNew(AdcDatastreamsEvent.class).withArguments(eq(TEST_DATASTREAM), eq(TEST_PIN_INDEX),
				eq(mockedService), eq(mockedEventDispatcher));
	}
}
