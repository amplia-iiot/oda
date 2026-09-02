package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GpioDatastreamsSetterTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_PIN_INDEX = 1;
    private static final boolean TEST_VALUE = true;

    @Mock
    private GpioService mockedGpioService;

    private GpioDatastreamsSetter testDatastreamsSetter;

    @BeforeEach
    public void setUp() {
        testDatastreamsSetter =
                new GpioDatastreamsSetter(TEST_DATASTREAM_ID, TEST_PIN_INDEX, mockedGpioService);
    }

    @Test
    public void testGetDatastreamIdSatisfied() {
        assertEquals(TEST_DATASTREAM_ID, testDatastreamsSetter.getDatastreamIdSatisfied());
    }

    @Test
    public void testDevicesIdManaged() {
        assertEquals(Collections.singletonList(""), testDatastreamsSetter.getDevicesIdManaged());
    }

    @Test
    public void testGetDatastreamType() {
        assertEquals(Boolean.class, testDatastreamsSetter.getDatastreamType());
    }

    @Test
    public void testSet() throws ExecutionException, InterruptedException {
        GpioPin mockedGpioPin = mock(GpioPin.class);

        when(mockedGpioService.getPinByIndex(anyInt())).thenReturn(mockedGpioPin);
        when(mockedGpioPin.isOpen()).thenReturn(false);

        CompletableFuture<Void> future = testDatastreamsSetter.set("", TEST_VALUE);
        future.get();

        verify(mockedGpioService).getPinByIndex(eq(TEST_PIN_INDEX));
        verify(mockedGpioPin).isOpen();
        verify(mockedGpioPin).open();
        verify(mockedGpioPin).setValue(eq(TEST_VALUE));
    }

    @Test
    public void testSetAlreadyOpenPin() throws ExecutionException, InterruptedException {
        GpioPin mockedGpioPin = mock(GpioPin.class);

        when(mockedGpioService.getPinByIndex(anyInt())).thenReturn(mockedGpioPin);
        when(mockedGpioPin.isOpen()).thenReturn(true);

        CompletableFuture<Void> future = testDatastreamsSetter.set("", TEST_VALUE);
        future.get();

        verify(mockedGpioService).getPinByIndex(eq(TEST_PIN_INDEX));
        verify(mockedGpioPin).isOpen();
        verify(mockedGpioPin, never()).open();
        verify(mockedGpioPin).setValue(eq(TEST_VALUE));
    }

    @Test
    public void testSetInvalidValueForDatastream() throws ExecutionException, InterruptedException {
        Object testValue = "invalidValue";

        assertThrows(RuntimeException.class, () -> testDatastreamsSetter.set("", testValue));
    }

    @Test
    public void testSetGpioDeviceException() throws ExecutionException, InterruptedException {
        when(mockedGpioService.getPinByIndex(anyInt())).thenThrow(new GpioDeviceException(""));

        CompletableFuture<Void> future = testDatastreamsSetter.set("", TEST_VALUE);

        assertThrows(ExecutionException.class, future::get);
    }
}