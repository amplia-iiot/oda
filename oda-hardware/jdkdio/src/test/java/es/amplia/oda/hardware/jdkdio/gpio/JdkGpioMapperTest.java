package es.amplia.oda.hardware.jdkdio.gpio;

import es.amplia.oda.core.commons.gpio.GpioDirection;
import es.amplia.oda.core.commons.gpio.GpioMode;
import es.amplia.oda.core.commons.gpio.GpioTrigger;

import jdk.dio.InvalidDeviceConfigException;
import jdk.dio.gpio.GPIOPinConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JdkGpioMapperTest {
    @Test
    public void testMapGpioDirectionToJdkGpioInputDirection() throws InvalidDeviceConfigException {
        int inputDirection = JdkGpioMapper.mapGpioDirectionToJdkGpioDirection(GpioDirection.INPUT);

        assertEquals(GPIOPinConfig.DIR_INPUT_ONLY, inputDirection);
    }

    @Test
    public void testMapGpioDirectionToJdkGpioOutputDirection() throws InvalidDeviceConfigException {
        int outputDirection = JdkGpioMapper.mapGpioDirectionToJdkGpioDirection(GpioDirection.OUTPUT);

        assertEquals(GPIOPinConfig.DIR_OUTPUT_ONLY, outputDirection);
    }

    @Test
    public void testMapGpioModeToJdkGpioPullDownMode() throws InvalidDeviceConfigException {
        int pullDownMode = JdkGpioMapper.mapGpioModeToJdkGpioMode(GpioMode.PULL_DOWN);

        assertEquals(GPIOPinConfig.MODE_INPUT_PULL_DOWN, pullDownMode);
    }

    @Test
    public void testMapGpioModeToJdkGpioPullUpMode() throws InvalidDeviceConfigException {
        int pullUpMode = JdkGpioMapper.mapGpioModeToJdkGpioMode(GpioMode.PULL_UP);

        assertEquals(GPIOPinConfig.MODE_INPUT_PULL_UP, pullUpMode);
    }

    @Test
    public void testMapGpioModeToJdkGpioOpenDrainMode() throws InvalidDeviceConfigException {
        int openDrainMode = JdkGpioMapper.mapGpioModeToJdkGpioMode(GpioMode.OPEN_DRAIN);

        assertEquals(GPIOPinConfig.MODE_OUTPUT_OPEN_DRAIN, openDrainMode);
    }

    @Test
    public void testMapGpioModeToJdkGpioPushPullMode() throws InvalidDeviceConfigException {
        int pushPullMode = JdkGpioMapper.mapGpioModeToJdkGpioMode(GpioMode.PUSH_PULL);

        assertEquals(GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, pushPullMode);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerNone() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.NONE, false);

        assertEquals(GPIOPinConfig.TRIGGER_NONE, trigger);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerRisingEdge() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.RISING_EDGE, false);

        assertEquals(GPIOPinConfig.TRIGGER_RISING_EDGE, trigger);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerRisingEdgeActiveLow() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.RISING_EDGE, true);

        assertEquals(GPIOPinConfig.TRIGGER_FALLING_EDGE, trigger);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerFallingEdge() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.FALLING_EDGE, false);

        assertEquals(GPIOPinConfig.TRIGGER_FALLING_EDGE, trigger);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerFallingEdgeActiveLow() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.FALLING_EDGE, true);

        assertEquals(GPIOPinConfig.TRIGGER_RISING_EDGE, trigger);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerBothEdges() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.BOTH_EDGES, false);

        assertEquals(GPIOPinConfig.TRIGGER_BOTH_EDGES, trigger);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerHighLevel() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.HIGH_LEVEL, false);

        assertEquals(GPIOPinConfig.TRIGGER_HIGH_LEVEL, trigger);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerHighLevelActiveLow() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.HIGH_LEVEL, true);

        assertEquals(GPIOPinConfig.TRIGGER_LOW_LEVEL, trigger);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerLowLevel() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.LOW_LEVEL, false);

        assertEquals(GPIOPinConfig.TRIGGER_LOW_LEVEL, trigger);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerLowLevelActiveLow() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.LOW_LEVEL, true);

        assertEquals(GPIOPinConfig.TRIGGER_HIGH_LEVEL, trigger);
    }

    @Test
    public void testMapGpioTriggerToJdkGpioTriggerBothLevels() throws InvalidDeviceConfigException {
        int trigger = JdkGpioMapper.mapGpioTriggerToJdkGpioTrigger(GpioTrigger.BOTH_LEVELS, false);

        assertEquals(GPIOPinConfig.TRIGGER_BOTH_LEVELS, trigger);
    }
}