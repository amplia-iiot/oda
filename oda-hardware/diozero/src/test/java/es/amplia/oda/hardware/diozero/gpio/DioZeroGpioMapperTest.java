package es.amplia.oda.hardware.diozero.gpio;

import es.amplia.oda.core.commons.gpio.GpioMode;
import es.amplia.oda.core.commons.gpio.GpioTrigger;

import com.diozero.api.GpioEventTrigger;
import com.diozero.api.GpioPullUpDown;
import org.junit.Test;

import static es.amplia.oda.hardware.diozero.gpio.DioZeroGpioMapper.*;
import static org.junit.Assert.assertEquals;

public class DioZeroGpioMapperTest {

    @Test
    public void testMapMode() {
        assertEquals(GpioPullUpDown.PULL_UP, mapGpioModeToDioZeroPullUpDown(GpioMode.PULL_UP));
        assertEquals(GpioPullUpDown.PULL_DOWN, mapGpioModeToDioZeroPullUpDown(GpioMode.PULL_DOWN));
        assertEquals(GpioPullUpDown.NONE, mapGpioModeToDioZeroPullUpDown(GpioMode.OPEN_DRAIN));
        assertEquals(GpioPullUpDown.NONE, mapGpioModeToDioZeroPullUpDown(GpioMode.PUSH_PULL));
    }

    @Test
    public void testMapTriggerActiveHigh() {
        assertEquals(GpioEventTrigger.NONE, mapGpioTriggerToDioZeroEventTrigger(GpioTrigger.NONE, false));
        assertEquals(GpioEventTrigger.RISING, mapGpioTriggerToDioZeroEventTrigger(GpioTrigger.RISING_EDGE, false));
        assertEquals(GpioEventTrigger.FALLING, mapGpioTriggerToDioZeroEventTrigger(GpioTrigger.FALLING_EDGE, false));
        assertEquals(GpioEventTrigger.BOTH, mapGpioTriggerToDioZeroEventTrigger(GpioTrigger.BOTH_EDGES, false));
        assertEquals(GpioEventTrigger.RISING, mapGpioTriggerToDioZeroEventTrigger(GpioTrigger.HIGH_LEVEL, false));
        assertEquals(GpioEventTrigger.FALLING, mapGpioTriggerToDioZeroEventTrigger(GpioTrigger.LOW_LEVEL, false));
        assertEquals(GpioEventTrigger.BOTH, mapGpioTriggerToDioZeroEventTrigger(GpioTrigger.BOTH_LEVELS, false));
    }

    @Test
    public void testMapTriggerActiveLowInvertsEdges() {
        assertEquals(GpioEventTrigger.FALLING, mapGpioTriggerToDioZeroEventTrigger(GpioTrigger.RISING_EDGE, true));
        assertEquals(GpioEventTrigger.RISING, mapGpioTriggerToDioZeroEventTrigger(GpioTrigger.FALLING_EDGE, true));
        assertEquals(GpioEventTrigger.BOTH, mapGpioTriggerToDioZeroEventTrigger(GpioTrigger.BOTH_EDGES, true));
    }
}
