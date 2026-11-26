package es.amplia.oda.hardware.diozero.configuration;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioDirection;
import es.amplia.oda.core.commons.gpio.GpioMode;
import es.amplia.oda.core.commons.gpio.GpioTrigger;
import es.amplia.oda.hardware.diozero.gpio.DioZeroGpioPin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class DioZeroGpioPinBuilderTest {

    private static final int TEST_INDEX = 1;
    private static final String TEST_NAME = "testGPIO";
    private static final GpioDirection TEST_DIRECTION = GpioDirection.OUTPUT;
    private static final GpioMode TEST_MODE = GpioMode.PUSH_PULL;
    private static final GpioTrigger TEST_TRIGGER = GpioTrigger.NONE;
    private static final boolean TEST_ACTIVE_LOW = true;
    private static final boolean TEST_INITIAL_VALUE = true;

    private static final String INVALID_DEVICE_CONFIGURATION_EXCEPTION_MESSAGE =
            "Invalid device configuration exception must be thrown";

    private DioZeroGpioPinBuilder builder;

    @BeforeEach
    public void setUp() {
        builder = DioZeroGpioPinBuilder.newBuilder();
    }

    @Test
    public void testCompleteBuild() {
        builder.setIndex(TEST_INDEX);
        builder.setName(TEST_NAME);
        builder.setDirection(GpioDirection.OUTPUT);
        builder.setMode(GpioMode.PUSH_PULL);
        builder.setTrigger(GpioTrigger.NONE);

        DioZeroGpioPin testPin = builder.build();

        assertNotNull(testPin);
    }

    @Test
    public void testMinimumBuild() {
        builder.setIndex(TEST_INDEX);

        DioZeroGpioPin testPin = builder.build();

        assertNotNull(testPin);
        assertEquals(DioZeroGpioPinBuilder.DEFAULT_DIRECTION, testPin.getDirection());
        assertEquals(DioZeroGpioPinBuilder.DEFAULT_MODE, testPin.getMode());
        assertEquals(DioZeroGpioPinBuilder.DEFAULT_TRIGGER, testPin.getTrigger());
        assertEquals(DioZeroGpioPinBuilder.DEFAULT_ACTIVE_LOW, testPin.isActiveLow());
        assertEquals(DioZeroGpioPinBuilder.DEFAULT_INITIAL_VALUE, testPin.getInitialValue());
    }

    @Test
    public void testIncompleteBuild() {
        builder.setName(TEST_NAME);
        builder.setDirection(GpioDirection.OUTPUT);


        assertThrows(GpioDeviceException.class, () -> builder.build());
    }

    @Test
    public void testSetIncompatibleInputModeBuild() {
        builder.setIndex(TEST_INDEX);
        builder.setDirection(GpioDirection.INPUT);
        builder.setMode(GpioMode.PUSH_PULL);


        assertThrows(GpioDeviceException.class, () -> builder.build());
    }

    @Test
    public void testSetIncompatibleOutputModeBuild() {
        builder.setIndex(TEST_INDEX);
        builder.setDirection(GpioDirection.OUTPUT);
        builder.setMode(GpioMode.PULL_UP);


        assertThrows(GpioDeviceException.class, () -> builder.build());
    }

    @Test
    public void testSetIncompatibleOutputWithTriggerBuild() {
        builder.setIndex(TEST_INDEX);
        builder.setDirection(GpioDirection.OUTPUT);
        builder.setMode(GpioMode.OPEN_DRAIN);
        builder.setTrigger(GpioTrigger.RISING_EDGE);


        assertThrows(GpioDeviceException.class, () -> builder.build());
    }

    @Test
    public void testSetters() {
        builder.setIndex(TEST_INDEX);
        builder.setName(TEST_NAME);
        builder.setDirection(TEST_DIRECTION);
        builder.setMode(TEST_MODE);
        builder.setTrigger(TEST_TRIGGER);
        builder.setActiveLow(TEST_ACTIVE_LOW);
        builder.setInitialValue(TEST_INITIAL_VALUE);

        DioZeroGpioPin testPin = builder.build();

        assertEquals(TEST_INDEX, testPin.getIndex());
        assertEquals(TEST_NAME, testPin.getName());
        assertEquals(TEST_DIRECTION, testPin.getDirection());
        assertEquals(TEST_MODE, testPin.getMode());
        assertEquals(TEST_TRIGGER, testPin.getTrigger());
        assertEquals(TEST_ACTIVE_LOW, testPin.isActiveLow());
        assertEquals(TEST_INITIAL_VALUE, testPin.getInitialValue());
    }
}
