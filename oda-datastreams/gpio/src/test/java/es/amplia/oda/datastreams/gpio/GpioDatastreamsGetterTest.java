package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GpioDatastreamsGetterTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final int TEST_PIN_INDEX = 1;

    @Mock
    private GpioService mockedGpioService;

    private GpioDatastreamsGetter testDatastreamsGetter;

    @BeforeEach
    public void setUp() {
        testDatastreamsGetter =
                new GpioDatastreamsGetter(TEST_DATASTREAM_ID, TEST_PIN_INDEX, mockedGpioService);
    }

    @Test
    public void testGetDatastreamIdSatisfied() {
        assertEquals(TEST_DATASTREAM_ID, testDatastreamsGetter.getDatastreamIdSatisfied());
    }

    @Test
    public void testGetDevicesIdManaged() {
    	assertEquals(Collections.singletonList(""), testDatastreamsGetter.getDevicesIdManaged());
    }
    
    @Test
    @SuppressWarnings("ConstantConditions")
    public void testGet() throws ExecutionException, InterruptedException {
        GpioPin mockedGpioPin = mock(GpioPin.class);
        boolean testValue = true;
        Long timeInitTest = System.currentTimeMillis();

        when(mockedGpioService.getPinByIndex(anyInt())).thenReturn(mockedGpioPin);
        when(mockedGpioPin.isOpen()).thenReturn(false);
        when(mockedGpioPin.getValue()).thenReturn(testValue);

        CompletableFuture<CollectedValue> future = testDatastreamsGetter.get("");
        CollectedValue recollectedValue = future.get();
        Object actualValue = recollectedValue.getValue();
        Long actualAtValue = recollectedValue.getAt();

        assertNotNull(actualValue);
        assertEquals(testValue, actualValue);
        assertTrue(actualAtValue >= timeInitTest);
        assertTrue(actualAtValue <= System.currentTimeMillis());

        verify(mockedGpioService).getPinByIndex(eq(TEST_PIN_INDEX));
        verify(mockedGpioPin).isOpen();
        verify(mockedGpioPin).open();
        verify(mockedGpioPin).getValue();
    }

    @Test
    public void testGetExceptionGettingUniqueValue() throws ExecutionException, InterruptedException {
        when(mockedGpioService.getPinByIndex(anyInt())).thenThrow(new GpioDeviceException("whatever"));

        assertThrows(ExecutionException.class, () -> testDatastreamsGetter.get("").get());
    }
}