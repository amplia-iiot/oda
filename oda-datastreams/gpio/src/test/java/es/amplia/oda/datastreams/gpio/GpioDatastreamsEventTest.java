package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioPinListener;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.EventPublisher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GpioDatastreamsEventTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_PIN_INDEX = 1;

    private static final String PIN_FIELD_NAME = "pin";


    @Mock
    private GpioService mockedGpioService;
    @Mock
    private EventPublisher mockedEventPublisher;

    private GpioDatastreamsEvent testGpioDatastreamsEvent;

    @Mock
    private GpioPin mockedPin;


    @Before
    public void setUp() {
        when(mockedGpioService.getPinByIndex(anyInt())).thenReturn(mockedPin);
        when(mockedPin.isOpen()).thenReturn(false);

        testGpioDatastreamsEvent = new GpioDatastreamsEvent(mockedEventPublisher, TEST_DATASTREAM_ID,
                TEST_PIN_INDEX, mockedGpioService);
    }

    @Test
    public void testConstructor() {
        verify(mockedGpioService).getPinByIndex(eq(TEST_PIN_INDEX));
        verify(mockedPin).isOpen();
        verify(mockedPin).open();
        verify(mockedPin).addGpioPinListener(any(GpioPinListener.class));
        assertEquals(mockedPin, Whitebox.getInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME));
    }

    @Test
    public void testRegisterToEventSource() {
        reset(mockedGpioService, mockedPin);

        when(mockedGpioService.getPinByIndex(anyInt())).thenReturn(mockedPin);
        when(mockedPin.isOpen()).thenReturn(false);

        testGpioDatastreamsEvent.registerToEventSource();

        verify(mockedGpioService).getPinByIndex(eq(TEST_PIN_INDEX));
        verify(mockedPin).isOpen();
        verify(mockedPin).open();
        verify(mockedPin).addGpioPinListener(any(GpioPinListener.class));
        assertEquals(mockedPin, Whitebox.getInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME));
    }

    @Test
    public void testRegisterToEventSourceAlreadyOpenPin()  {
        reset(mockedGpioService, mockedPin);

        when(mockedGpioService.getPinByIndex(anyInt())).thenReturn(mockedPin);
        when(mockedPin.isOpen()).thenReturn(true);

        testGpioDatastreamsEvent.registerToEventSource();

        verify(mockedGpioService).getPinByIndex(eq(TEST_PIN_INDEX));
        verify(mockedPin).isOpen();
        verify(mockedPin, never()).open();
        verify(mockedPin).addGpioPinListener(any(GpioPinListener.class));
        assertEquals(mockedPin, Whitebox.getInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME));
    }

    @Test
    public void testRegisterToEventSourceGpioDeviceExceptionCaught() {
        reset(mockedGpioService, mockedPin);

        when(mockedGpioService.getPinByIndex(anyInt())).thenReturn(mockedPin);
        when(mockedPin.isOpen()).thenReturn(false);
        doThrow(GpioDeviceException.class).when(mockedPin).addGpioPinListener(any(GpioPinListener.class));

        testGpioDatastreamsEvent.registerToEventSource();

        verify(mockedGpioService).getPinByIndex(eq(TEST_PIN_INDEX));
        verify(mockedPin).addGpioPinListener(any(GpioPinListener.class));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testPublishEvent() {
        boolean testValue = true;

        testGpioDatastreamsEvent.publishValue(testValue);

        verify(mockedEventPublisher).publishEvents(eq(""), eq(null), any());
    }

    @Test
    public void testUnregisterFromEventSource() {
        Whitebox.setInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME, mockedPin);

        testGpioDatastreamsEvent.unregisterFromEventSource();

        verify(mockedPin).removeGpioPinListener();
    }

    @Test
    public void testUnregisterFromEventSourceGpioDeviceExceptionCaught() {
        Whitebox.setInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME, mockedPin);

        doThrow(new GpioDeviceException("")).when(mockedPin).removeGpioPinListener();

        testGpioDatastreamsEvent.unregisterFromEventSource();

        verify(mockedPin).removeGpioPinListener();
    }

    @Test
    public void testUnregisterFromEventSourceGeneralExceptionCaught() {
        Whitebox.setInternalState(testGpioDatastreamsEvent, PIN_FIELD_NAME, mockedPin);

        doThrow(new RuntimeException()).when(mockedPin).removeGpioPinListener();

        testGpioDatastreamsEvent.unregisterFromEventSource();

        verify(mockedPin).removeGpioPinListener();
    }
}