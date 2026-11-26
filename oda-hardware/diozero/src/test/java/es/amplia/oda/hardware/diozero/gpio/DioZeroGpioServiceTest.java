package es.amplia.oda.hardware.diozero.gpio;

import es.amplia.oda.core.commons.gpio.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DioZeroGpioServiceTest {

    private static final String TEST_NAME = "testPin";
    private static final int TEST_INDEX = 3;
    private static final GpioDirection TEST_DIRECTION = GpioDirection.OUTPUT;
    private static final GpioMode TEST_MODE = GpioMode.OPEN_DRAIN;
    private static final GpioTrigger TEST_TRIGGER = GpioTrigger.NONE;
    private static final boolean TEST_ACTIVE_LOW = true;
    private static final boolean TEST_INITIAL_VALUE = true;

    private static final String GPIO_DEVICE_EXCEPTION_MESSAGE = "Gpio Device Exception must be thrown";

    @Mock
    private DioZeroGpioPin mockedPin1;
    @Mock
    private DioZeroGpioPin mockedPin2;
    @Mock
    private DioZeroGpioPin mockedPin3;
    @Mock
    private DioZeroGpioPin mockedPin4;
    private Map<Integer, DioZeroGpioPin> spiedAvailablePins;

    private DioZeroGpioService testService;

    @BeforeEach
    public void setUp() {
        testService = new DioZeroGpioService();

        Map<Integer, DioZeroGpioPin> availablePins = new HashMap<>();
        availablePins.put(1, mockedPin1);
        availablePins.put(2, mockedPin2);
        availablePins.put(3, mockedPin3);
        availablePins.put(4, mockedPin4);

        spiedAvailablePins = availablePins;
        Whitebox.setInternalState(testService, "pins", spiedAvailablePins);
    }

    @Test
    public void testGetPinByName() {
        when(mockedPin3.getName()).thenReturn(TEST_NAME);

        GpioPin resultPin = testService.getPinByName(TEST_NAME);

        assertEquals(mockedPin3, resultPin);
    }

    @Test
    public void testGetPinByNamePinNotFound() {
        when(mockedPin1.getName()).thenReturn("GPIO1");
        when(mockedPin2.getName()).thenReturn("GPIO2");
        when(mockedPin3.getName()).thenReturn("GPIO3");
        when(mockedPin4.getName()).thenReturn("GPIO4");

        assertThrows(GpioDeviceException.class, () -> testService.getPinByName(TEST_NAME));
    }

    @Test
    public void testGetPinByNameWithConfigDifferentConfig() {
        when(mockedPin3.getIndex()).thenReturn(TEST_INDEX);
        when(mockedPin3.getName()).thenReturn(TEST_NAME);
        when(mockedPin3.getDirection()).thenReturn(TEST_DIRECTION);
        when(mockedPin3.getMode()).thenReturn(GpioMode.PUSH_PULL);
        when(mockedPin3.getTrigger()).thenReturn(TEST_TRIGGER);
        when(mockedPin3.isActiveLow()).thenReturn(false);
        when(mockedPin3.getInitialValue()).thenReturn(false);

        List<List<?>> newPinArgs = new ArrayList<>();
        try (MockedConstruction<DioZeroGpioPin> newPinCons = mockConstruction(DioZeroGpioPin.class,
                (mock, mctx) -> newPinArgs.add(new ArrayList<>(mctx.arguments())))) {

            GpioPin resultPin = testService.getPinByName(TEST_NAME, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER,
                    TEST_ACTIVE_LOW, TEST_INITIAL_VALUE);

            assertEquals(1, newPinCons.constructed().size());
            assertEquals(newPinCons.constructed().get(0), resultPin);
            assertEquals(Arrays.asList(TEST_INDEX, TEST_NAME, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER,
                    TEST_ACTIVE_LOW, TEST_INITIAL_VALUE), newPinArgs.get(0));
            assertEquals(newPinCons.constructed().get(0), spiedAvailablePins.get(TEST_INDEX));
        }
    }

    @Test
    public void testGetPinByIndex() {
        GpioPin resultPin = testService.getPinByIndex(TEST_INDEX);

        assertEquals(mockedPin3, resultPin);
    }

    @Test
    public void testGetPinByIndexPinNotFound() {
        assertThrows(GpioDeviceException.class, () -> testService.getPinByIndex(99));
    }

    @Test
    public void testGetPinByIndexWithConfigNewPin() {
        int newIndex = 99;

        List<List<?>> newPinArgs = new ArrayList<>();
        try (MockedConstruction<DioZeroGpioPin> newPinCons = mockConstruction(DioZeroGpioPin.class,
                (mock, mctx) -> newPinArgs.add(new ArrayList<>(mctx.arguments())))) {

            GpioPin resultPin = testService.getPinByIndex(newIndex, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER,
                    TEST_ACTIVE_LOW, TEST_INITIAL_VALUE);

            assertEquals(newPinCons.constructed().get(0), resultPin);
            assertEquals(Arrays.asList(newIndex, null, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER,
                    TEST_ACTIVE_LOW, TEST_INITIAL_VALUE), newPinArgs.get(0));
            assertEquals(newPinCons.constructed().get(0), spiedAvailablePins.get(newIndex));
        }
    }

    @Test
    public void testGetAvailablePins() {
        Map<Integer, GpioPin> availablePins = testService.getAvailablePins();

        assertEquals(spiedAvailablePins.size(), availablePins.size());
        for (Map.Entry<Integer, DioZeroGpioPin> availablePin : spiedAvailablePins.entrySet()) {
            assertTrue(availablePins.containsKey(availablePin.getKey()));
            assertEquals(availablePin.getValue(), availablePins.get(availablePin.getKey()));
        }
    }

    @Test
    public void testLoadConfigurationInputPin() {
        Whitebox.setInternalState(testService, "pins", new HashMap<Integer, DioZeroGpioPin>());
        DioZeroGpioPin newPin = mock(DioZeroGpioPin.class);
        when(newPin.getIndex()).thenReturn(TEST_INDEX);
        when(newPin.getDirection()).thenReturn(GpioDirection.INPUT);

        testService.loadConfiguration(Collections.singletonList(newPin));

        verify(newPin, never()).open();
        assertEquals(newPin, testService.getAvailablePins().get(TEST_INDEX));
    }

    @Test
    public void testLoadConfigurationOutputPinWithInitialValue() {
        Whitebox.setInternalState(testService, "pins", new HashMap<Integer, DioZeroGpioPin>());
        DioZeroGpioPin newPin = mock(DioZeroGpioPin.class);
        when(newPin.getIndex()).thenReturn(TEST_INDEX);
        when(newPin.getDirection()).thenReturn(GpioDirection.OUTPUT);
        when(newPin.getInitialValue()).thenReturn(TEST_INITIAL_VALUE);

        testService.loadConfiguration(Collections.singletonList(newPin));

        verify(newPin).open();
        verify(newPin).setValue(eq(TEST_INITIAL_VALUE));
        assertEquals(newPin, testService.getAvailablePins().get(TEST_INDEX));
    }

    @Test
    public void testClose() {
        when(mockedPin2.isOpen()).thenReturn(true);
        when(mockedPin4.isOpen()).thenReturn(true);

        testService.close();

        verify(mockedPin2).close();
        verify(mockedPin4).close();
        verify(mockedPin1, never()).close();
        verify(mockedPin3, never()).close();
        assertTrue(testService.getAvailablePins().isEmpty());
    }

    @Test
    public void testCloseCloseException() {
        when(mockedPin2.isOpen()).thenReturn(true);
        doThrow(GpioDeviceException.class).when(mockedPin2).close();
        when(mockedPin4.isOpen()).thenReturn(true);

        testService.close();

        verify(mockedPin2).close();
        verify(mockedPin4).close();
        assertTrue(testService.getAvailablePins().isEmpty());
    }
}
