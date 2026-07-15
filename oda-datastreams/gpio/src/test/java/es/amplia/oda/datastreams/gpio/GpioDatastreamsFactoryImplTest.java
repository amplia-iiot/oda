package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.EventPublisher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockConstruction;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GpioDatastreamsFactoryImplTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_PIN_INDEX = 1;

    @Mock
    private GpioService mockedGpioService;
    @Mock
    private EventPublisher mockedEventPublisher;
    @InjectMocks
    private GpioDatastreamsFactoryImpl testFactory;

    @Test
    public void testCreateGpioDatastreamsGetter() throws Exception {
        List<List<?>> getterArgs = new ArrayList<>();
        try (MockedConstruction<GpioDatastreamsGetter> getterCons =
                     mockConstruction(GpioDatastreamsGetter.class,
                             (mock, mctx) -> getterArgs.add(new ArrayList<>(mctx.arguments())))) {

            DatastreamsGetter getter = testFactory.createGpioDatastreamsGetter(TEST_DATASTREAM_ID, TEST_PIN_INDEX);

            assertEquals(1, getterCons.constructed().size());
            assertEquals(getterCons.constructed().get(0), getter);
            assertEquals(Arrays.asList(TEST_DATASTREAM_ID, TEST_PIN_INDEX, mockedGpioService), getterArgs.get(0));
        }
    }

    @Test
    public void testCreateGpioDatastreamsSetter() throws Exception {
        List<List<?>> setterArgs = new ArrayList<>();
        try (MockedConstruction<GpioDatastreamsSetter> setterCons =
                     mockConstruction(GpioDatastreamsSetter.class,
                             (mock, mctx) -> setterArgs.add(new ArrayList<>(mctx.arguments())))) {

            DatastreamsSetter setter = testFactory.createGpioDatastreamsSetter(TEST_DATASTREAM_ID, TEST_PIN_INDEX);

            assertEquals(1, setterCons.constructed().size());
            assertEquals(setterCons.constructed().get(0), setter);
            assertEquals(Arrays.asList(TEST_DATASTREAM_ID, TEST_PIN_INDEX, mockedGpioService), setterArgs.get(0));
        }
    }

    @Test
    public void testCreateGpioDatastreamsEvent() throws Exception {
        List<List<?>> eventArgs = new ArrayList<>();
        try (MockedConstruction<GpioDatastreamsEvent> eventCons =
                     mockConstruction(GpioDatastreamsEvent.class,
                             (mock, mctx) -> eventArgs.add(new ArrayList<>(mctx.arguments())))) {

            DatastreamsEvent event = testFactory.createGpioDatastreamsEvent(TEST_DATASTREAM_ID, TEST_PIN_INDEX);

            assertEquals(1, eventCons.constructed().size());
            assertEquals(eventCons.constructed().get(0), event);
            assertEquals(Arrays.asList(mockedEventPublisher, TEST_DATASTREAM_ID, TEST_PIN_INDEX, mockedGpioService),
                    eventArgs.get(0));
        }
    }
}