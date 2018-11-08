package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioPinListener;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GpioDatastreamsEventTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_PIN_INDEX = 1;

    private static final String PIN_FIELD_NAME = "pin";

    @Mock
    private GpioService mockedGpioService;
    @Mock
    private EventDispatcher mockedEventDispatcher;

    private GpioDatastreamsEvent testGpioDatastreamsEvent;

    @Mock
    private GpioPin mockedPin;

    @Before
    public void setUp() {
        testGpioDatastreamsEvent =
                new GpioDatastreamsEvent(TEST_DATASTREAM_ID, TEST_PIN_INDEX, mockedGpioService, mockedEventDispatcher);
    }

    @Test
    public void testInit() {
        when(mockedGpioService.getPinByIndex(anyInt())).thenReturn(mockedPin);
        when(mockedPin.isOpen()).thenReturn(false);

        testGpioDatastreamsEvent.init();

        verify(mockedGpioService).getPinByIndex(eq(TEST_PIN_INDEX));
        verify(mockedPin).isOpen();
        verify(mockedPin).open();
        verify(mockedPin).addGpioPinListener(any(GpioPinListener.class));
        assertEquals(mockedPin, Whitebox.getInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME));
    }

    @Test
    public void testInitAlreadyOpenPin()  {
        when(mockedGpioService.getPinByIndex(anyInt())).thenReturn(mockedPin);
        when(mockedPin.isOpen()).thenReturn(true);

        testGpioDatastreamsEvent.init();

        verify(mockedGpioService).getPinByIndex(eq(TEST_PIN_INDEX));
        verify(mockedPin).isOpen();
        verify(mockedPin, never()).open();
        verify(mockedPin).addGpioPinListener(any(GpioPinListener.class));
        assertEquals(mockedPin, Whitebox.getInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME));
    }

    @Test
    public void testInitGpioDeviceExceptionCaught() {
        when(mockedGpioService.getPinByIndex(anyInt())).thenReturn(mockedPin);
        when(mockedPin.isOpen()).thenReturn(false);
        doThrow(GpioDeviceException.class).when(mockedPin).addGpioPinListener(any(GpioPinListener.class));

        testGpioDatastreamsEvent.init();

        verify(mockedGpioService).getPinByIndex(eq(TEST_PIN_INDEX));
        verify(mockedPin).addGpioPinListener(any(GpioPinListener.class));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testPublishEvent() {
        boolean testValue = true;
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        testGpioDatastreamsEvent.publishEvent(testValue);

        verify(mockedEventDispatcher).publish(eventCaptor.capture());
        Event generatedEvent = eventCaptor.getValue();
        assertEquals(TEST_DATASTREAM_ID, generatedEvent.getDatastreamId());
        assertEquals("", generatedEvent.getDeviceId());
        assertEquals(testValue, generatedEvent.getValue());
    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME, mockedPin);

        testGpioDatastreamsEvent.close();

        verify(mockedPin).removeGpioPinListener();
    }

    @Test
    public void testCloseGpioDeviceExceptionCaught() {
        Whitebox.setInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME, mockedPin);

        doThrow(new GpioDeviceException("")).when(mockedPin).removeGpioPinListener();

        testGpioDatastreamsEvent.close();

        verify(mockedPin).removeGpioPinListener();
    }

    @Test
    public void testCloseGeneralExceptionCaught() {
        Whitebox.setInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME, mockedPin);

        doThrow(new RuntimeException()).when(mockedPin).removeGpioPinListener();

        testGpioDatastreamsEvent.close();

        verify(mockedPin).removeGpioPinListener();
    }
}