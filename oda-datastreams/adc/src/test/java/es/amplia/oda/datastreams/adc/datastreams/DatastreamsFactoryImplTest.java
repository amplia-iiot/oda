package es.amplia.oda.datastreams.adc.datastreams;

import es.amplia.oda.core.commons.adc.AdcService;
import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.EventPublisher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockConstruction;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DatastreamsFactoryImplTest {

	private static final String TEST_DATASTREAM = "testDatastream";
	private static final int TEST_PIN_INDEX = 1;
	private static final double TEST_MINIMUM = 0;
	private static final double TEST_MAXIMUM = 1;


	@Mock
	private AdcService mockedService;
	@Mock
	private EventPublisher mockedEventPublisher;
	@InjectMocks
	private DatastreamsFactoryImpl testFactory;


	@Test
	public void testCreateAdcDatastreamsGetter() {
		List<List<?>> getterArgs = new ArrayList<>();
		try (MockedConstruction<AdcDatastreamsGetter> getterCons = mockConstruction(AdcDatastreamsGetter.class,
				(mock, mctx) -> getterArgs.add(new ArrayList<>(mctx.arguments())))) {

			DatastreamsGetter getter =
					testFactory.createAdcDatastreamsGetter(TEST_DATASTREAM, TEST_PIN_INDEX, TEST_MINIMUM, TEST_MAXIMUM);

			assertNotNull(getter);
			assertEquals(1, getterCons.constructed().size());
			assertEquals(TEST_DATASTREAM, getterArgs.get(0).get(0));
			assertEquals(TEST_PIN_INDEX, getterArgs.get(0).get(1));
			assertEquals(mockedService, getterArgs.get(0).get(2));
			assertEquals(TEST_MINIMUM, getterArgs.get(0).get(3));
			assertEquals(TEST_MAXIMUM, getterArgs.get(0).get(4));
		}
	}

	@Test
	public void testCreateAdcDatastreamsEvent() {
		List<List<?>> eventArgs = new ArrayList<>();
		try (MockedConstruction<AdcDatastreamsEvent> eventCons = mockConstruction(AdcDatastreamsEvent.class,
				(mock, mctx) -> eventArgs.add(new ArrayList<>(mctx.arguments())))) {

			DatastreamsEvent event =
					testFactory.createAdcDatastreamsEvent(TEST_DATASTREAM, TEST_PIN_INDEX, TEST_MINIMUM, TEST_MAXIMUM);

			assertNotNull(event);
			assertEquals(1, eventCons.constructed().size());
			assertEquals(TEST_DATASTREAM, eventArgs.get(0).get(0));
			assertEquals(TEST_PIN_INDEX, eventArgs.get(0).get(1));
			assertEquals(mockedService, eventArgs.get(0).get(2));
			assertEquals(mockedEventPublisher, eventArgs.get(0).get(3));
			assertEquals(TEST_MINIMUM, eventArgs.get(0).get(4));
			assertEquals(TEST_MAXIMUM, eventArgs.get(0).get(5));
		}
	}
}
