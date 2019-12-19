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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GpioDatastreamsFactoryImpl.class)
public class GpioDatastreamsFactoryImplTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_PIN_INDEX = 1;

    @Mock
    private GpioService mockedGpioService;
    @Mock
    private EventPublisher mockedEventPublisher;
    @InjectMocks
    private GpioDatastreamsFactoryImpl testFactory;

    @Mock
    private GpioDatastreamsGetter mockedGpioDatastreamsGetter;
    @Mock
    private GpioDatastreamsSetter mockedGpioDatastreamsSetter;
    @Mock
    private GpioDatastreamsEvent mockedGpioDatastreamsEvent;

    @Test
    public void testCreateGpioDatastreamsGetter() throws Exception {
        PowerMockito.whenNew(GpioDatastreamsGetter.class).withAnyArguments().thenReturn(mockedGpioDatastreamsGetter);

        DatastreamsGetter getter = testFactory.createGpioDatastreamsGetter(TEST_DATASTREAM_ID, TEST_PIN_INDEX);

        assertEquals(mockedGpioDatastreamsGetter, getter);
        PowerMockito.verifyNew(GpioDatastreamsGetter.class)
                .withArguments(eq(TEST_DATASTREAM_ID), eq(TEST_PIN_INDEX), eq(mockedGpioService));
    }

    @Test
    public void testCreateGpioDatastreamsSetter() throws Exception {
        PowerMockito.whenNew(GpioDatastreamsSetter.class).withAnyArguments().thenReturn(mockedGpioDatastreamsSetter);

        DatastreamsSetter setter = testFactory.createGpioDatastreamsSetter(TEST_DATASTREAM_ID, TEST_PIN_INDEX);

        assertEquals(mockedGpioDatastreamsSetter, setter);
        PowerMockito.verifyNew(GpioDatastreamsSetter.class)
                .withArguments(eq(TEST_DATASTREAM_ID), eq(TEST_PIN_INDEX), eq(mockedGpioService));
    }

    @Test
    public void testCreateGpioDatastreamsEvent() throws Exception {
        PowerMockito.whenNew(GpioDatastreamsEvent.class).withAnyArguments().thenReturn(mockedGpioDatastreamsEvent);

        DatastreamsEvent event = testFactory.createGpioDatastreamsEvent(TEST_DATASTREAM_ID, TEST_PIN_INDEX);

        assertEquals(mockedGpioDatastreamsEvent, event);
        PowerMockito.verifyNew(GpioDatastreamsEvent.class).withArguments(eq(mockedEventPublisher),
                eq(TEST_DATASTREAM_ID), eq(TEST_PIN_INDEX), eq(mockedGpioService));
    }
}