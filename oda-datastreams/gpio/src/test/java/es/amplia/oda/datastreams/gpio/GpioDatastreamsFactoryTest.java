package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.event.api.EventDispatcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GpioDatastreamsFactory.class)
public class GpioDatastreamsFactoryTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_PIN_INDEX = 1;

    @Mock
    private GpioService mockedGpioService;
    @Mock
    private EventDispatcher mockedEventDispatcher;
    @Mock
    private Executor mockedExecutor;

    @Mock
    private GpioDatastreamsGetter mockedGpioDatastreamsGetter;
    @Mock
    private GpioDatastreamsSetter mockedGpioDatastreamsSetter;
    @Mock
    private GpioDatastreamsEvent mockedGpioDatastreamsEvent;

    @Test
    public void testCreateGpioDatastreamsGetter() throws Exception {
        PowerMockito.whenNew(GpioDatastreamsGetter.class).withAnyArguments().thenReturn(mockedGpioDatastreamsGetter);

        GpioDatastreamsGetter getter =
                GpioDatastreamsFactory.createGpioDatastreamsGetter(TEST_DATASTREAM_ID, TEST_PIN_INDEX,
                        mockedGpioService, mockedExecutor);

        assertEquals(mockedGpioDatastreamsGetter, getter);
        PowerMockito.verifyNew(GpioDatastreamsGetter.class)
                .withArguments(eq(TEST_DATASTREAM_ID), eq(TEST_PIN_INDEX), eq(mockedGpioService), eq(mockedExecutor));
    }

    @Test
    public void testCreateGpioDatastreamsSetter() throws Exception {
        PowerMockito.whenNew(GpioDatastreamsSetter.class).withAnyArguments().thenReturn(mockedGpioDatastreamsSetter);

        GpioDatastreamsSetter setter =
                GpioDatastreamsFactory.createGpioDatastreamsSetter(TEST_DATASTREAM_ID, TEST_PIN_INDEX,
                        mockedGpioService, mockedExecutor);

        assertEquals(mockedGpioDatastreamsSetter, setter);
        PowerMockito.verifyNew(GpioDatastreamsSetter.class)
                .withArguments(eq(TEST_DATASTREAM_ID), eq(TEST_PIN_INDEX), eq(mockedGpioService), eq(mockedExecutor));
    }

    @Test
    public void testCreateGpioDatastreamsEvent() throws Exception {
        PowerMockito.whenNew(GpioDatastreamsEvent.class).withAnyArguments().thenReturn(mockedGpioDatastreamsEvent);

        GpioDatastreamsEvent event =
                GpioDatastreamsFactory.createGpioDatastreamsEvent(TEST_DATASTREAM_ID, TEST_PIN_INDEX,
                        mockedGpioService, mockedEventDispatcher);

        assertEquals(mockedGpioDatastreamsEvent, event);
        PowerMockito.verifyNew(GpioDatastreamsEvent.class)
                .withArguments(eq(TEST_DATASTREAM_ID), eq(TEST_PIN_INDEX), eq(mockedGpioService),
                        eq(mockedEventDispatcher));
    }
}