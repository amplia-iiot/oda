package es.amplia.oda.hardware.diozero.gpio;

import es.amplia.oda.core.commons.gpio.*;

import com.diozero.api.DigitalInputDevice;
import com.diozero.api.DigitalOutputDevice;
import com.diozero.api.GpioEventTrigger;
import com.diozero.api.GpioPullUpDown;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DioZeroGpioPinTest {

    private static final int TEST_INDEX = 5;
    private static final String TEST_NAME = "GPIO5";

    private DioZeroGpioPin outputPin(boolean activeLow, boolean initialValue) {
        return new DioZeroGpioPin(TEST_INDEX, TEST_NAME, GpioDirection.OUTPUT, GpioMode.PUSH_PULL,
                GpioTrigger.NONE, activeLow, initialValue);
    }

    private DioZeroGpioPin inputPin(boolean activeLow) {
        return new DioZeroGpioPin(TEST_INDEX, TEST_NAME, GpioDirection.INPUT, GpioMode.PULL_UP,
                GpioTrigger.RISING_EDGE, activeLow, false);
    }

    @Test
    public void testGettersAndDefaultName() {
        DioZeroGpioPin pin = new DioZeroGpioPin(TEST_INDEX, null, GpioDirection.INPUT, GpioMode.PULL_DOWN,
                GpioTrigger.BOTH_EDGES, false, false);

        assertEquals(TEST_INDEX, pin.getIndex());
        assertEquals(String.valueOf(TEST_INDEX), pin.getName());
        assertEquals(GpioDirection.INPUT, pin.getDirection());
        assertEquals(GpioMode.PULL_DOWN, pin.getMode());
        assertEquals(GpioTrigger.BOTH_EDGES, pin.getTrigger());
        assertFalse(pin.isActiveLow());
        assertFalse(pin.getInitialValue());
        assertFalse(pin.isOpen());
    }

    @Test
    public void testOpenOutput() {
        List<List<?>> args = new ArrayList<>();
        try (MockedConstruction<DigitalOutputDevice> cons = mockConstruction(DigitalOutputDevice.class,
                (mock, ctx) -> args.add(new ArrayList<>(ctx.arguments())))) {
            DioZeroGpioPin pin = outputPin(true, true);
            pin.open();

            assertTrue(pin.isOpen());
            assertEquals(1, cons.constructed().size());
            // index, activeHigh=true, physical initial = initialValue ^ activeLow = true ^ true = false
            assertEquals(TEST_INDEX, args.get(0).get(0));
            assertEquals(true, args.get(0).get(1));
            assertEquals(false, args.get(0).get(2));
        }
    }

    @Test
    public void testOpenInput() {
        List<List<?>> args = new ArrayList<>();
        try (MockedConstruction<DigitalInputDevice> cons = mockConstruction(DigitalInputDevice.class,
                (mock, ctx) -> args.add(new ArrayList<>(ctx.arguments())))) {
            DioZeroGpioPin pin = inputPin(false);
            pin.open();

            assertTrue(pin.isOpen());
            assertEquals(1, cons.constructed().size());
            assertEquals(TEST_INDEX, args.get(0).get(0));
            assertEquals(GpioPullUpDown.PULL_UP, args.get(0).get(1));
            assertEquals(GpioEventTrigger.RISING, args.get(0).get(2));
        }
    }

    @Test
    public void testSetAndGetValueOutput() {
        try (MockedConstruction<DigitalOutputDevice> cons = mockConstruction(DigitalOutputDevice.class)) {
            DioZeroGpioPin pin = outputPin(false, false);
            pin.open();
            DigitalOutputDevice device = cons.constructed().get(0);

            pin.setValue(true);
            verify(device).setOn(eq(true));

            when(device.isOn()).thenReturn(true);
            assertTrue(pin.getValue());
        }
    }

    @Test
    public void testSetValueOutputActiveLowInverts() {
        try (MockedConstruction<DigitalOutputDevice> cons = mockConstruction(DigitalOutputDevice.class)) {
            DioZeroGpioPin pin = outputPin(true, false);
            pin.open();
            DigitalOutputDevice device = cons.constructed().get(0);

            pin.setValue(true);
            // active low -> physical is inverted
            verify(device).setOn(eq(false));
        }
    }

    @Test
    public void testGetValueInputActiveLowInverts() {
        try (MockedConstruction<DigitalInputDevice> cons = mockConstruction(DigitalInputDevice.class)) {
            DioZeroGpioPin pin = inputPin(true);
            pin.open();
            DigitalInputDevice device = cons.constructed().get(0);
            when(device.getValue()).thenReturn(true);

            // physical true, active low -> logical false
            assertFalse(pin.getValue());
        }
    }

    @Test
    public void testSetValueOnInputThrows() {
        try (MockedConstruction<DigitalInputDevice> ignored = mockConstruction(DigitalInputDevice.class)) {
            DioZeroGpioPin pin = inputPin(false);
            pin.open();

            assertThrows(GpioDeviceException.class, () -> pin.setValue(true));
        }
    }

    @Test
    public void testGetValueClosedThrows() {
        assertThrows(GpioDeviceException.class, () -> outputPin(false, false).getValue());
    }

    @Test
    public void testAddAndRemoveListenerInput() {
        try (MockedConstruction<DigitalInputDevice> cons = mockConstruction(DigitalInputDevice.class)) {
            DioZeroGpioPin pin = inputPin(false);
            pin.open();
            DigitalInputDevice device = cons.constructed().get(0);
            GpioPinListener listener = mock(GpioPinListener.class);

            pin.addGpioPinListener(listener);
            verify(device).removeAllListeners();
            verify(device).addListener(any());

            pin.removeGpioPinListener();
            verify(device, times(2)).removeAllListeners();
        }
    }

    @Test
    public void testAddListenerOnOutputThrows() {
        try (MockedConstruction<DigitalOutputDevice> ignored = mockConstruction(DigitalOutputDevice.class)) {
            DioZeroGpioPin pin = outputPin(false, false);
            pin.open();

            assertThrows(GpioDeviceException.class, () -> pin.addGpioPinListener(mock(GpioPinListener.class)));
        }
    }

    @Test
    public void testCloseInput() {
        try (MockedConstruction<DigitalInputDevice> cons = mockConstruction(DigitalInputDevice.class)) {
            DioZeroGpioPin pin = inputPin(false);
            pin.open();
            DigitalInputDevice device = cons.constructed().get(0);

            pin.close();

            verify(device).close();
            assertFalse(pin.isOpen());
        }
    }
}
