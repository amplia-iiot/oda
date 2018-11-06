package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GpioDatastreamsSetterTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_PIN_INDEX = 1;
    private static final boolean TEST_VALUE = true;
    private static final Executor executor = Executors.newSingleThreadExecutor();

    @Mock
    private GpioService mockedGpioService;

    private GpioDatastreamsSetter testDatastreamsSetter;

    @Before
    public void setUp() {
        testDatastreamsSetter =
                new GpioDatastreamsSetter(TEST_DATASTREAM_ID, TEST_PIN_INDEX, mockedGpioService, executor);
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

    @Test(expected = RuntimeException.class)
    public void testSetInvalidValueForDatastream() throws ExecutionException, InterruptedException {
        Object testValue = "invalidValue";

        CompletableFuture<Void> future = testDatastreamsSetter.set("", testValue);
        future.get();

        fail("Invalid value for datastream runtime exception must be thrown");
    }

    @Test(expected = ExecutionException.class)
    public void testSetGpioDeviceException() throws ExecutionException, InterruptedException {
        when(mockedGpioService.getPinByIndex(anyInt())).thenThrow(new GpioDeviceException(""));

        CompletableFuture<Void> future = testDatastreamsSetter.set("", TEST_VALUE);
        future.get();

        fail("Gpio operation error execution exception must be thrown");
    }
}