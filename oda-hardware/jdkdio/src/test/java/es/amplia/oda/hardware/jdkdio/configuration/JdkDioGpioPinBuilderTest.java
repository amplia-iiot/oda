package es.amplia.oda.hardware.jdkdio.configuration;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioDirection;
import es.amplia.oda.core.commons.gpio.GpioMode;
import es.amplia.oda.core.commons.gpio.GpioTrigger;
import es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioPin;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class JdkDioGpioPinBuilderTest {

    private static final int TEST_INDEX = 1;
    private static final String TEST_NAME = "testGPIO";
    private static final GpioDirection TEST_DIRECTION = GpioDirection.OUTPUT;
    private static final GpioMode TEST_MODE = GpioMode.PUSH_PULL;
    private static final GpioTrigger TEST_TRIGGER = GpioTrigger.NONE;
    private static final boolean TEST_ACTIVE_LOW = true;
    private static final boolean TEST_INITIAL_VALUE = true;

    private static final String INVALID_DEVICE_CONFIGURATION_EXCEPTION_MESSAGE =
            "Invalid device configuration exception must be thrown";

    private JdkDioGpioPinBuilder builder;

    @Before
    public void setUp() {
        builder = JdkDioGpioPinBuilder.newBuilder();
    }

    @Test
    public void testNewBuilder() {
        assertNotNull(builder);
    }

    @Test
    public void testCompleteBuild() {
        builder.setIndex(TEST_INDEX);
        builder.setName(TEST_NAME);
        builder.setDirection(GpioDirection.OUTPUT);
        builder.setMode(GpioMode.PUSH_PULL);
        builder.setTrigger(GpioTrigger.NONE);

        JdkDioGpioPin testPin = builder.build();

        assertNotNull(testPin);
    }

    @Test
    public void testMinimumBuild() {
        builder.setIndex(TEST_INDEX);

        JdkDioGpioPin testPin = builder.build();

        assertNotNull(testPin);
    }

    @Test
    public void testDefaultDirection() {
        builder.setIndex(TEST_INDEX);

        JdkDioGpioPin testPin = builder.build();

        assertEquals(JdkDioGpioPinBuilder.DEFAULT_DIRECTION, testPin.getDirection());
    }

    @Test
    public void testDefaultMode() {
        builder.setIndex(TEST_INDEX);

        JdkDioGpioPin testPin = builder.build();

        assertEquals(JdkDioGpioPinBuilder.DEFAULT_MODE, testPin.getMode());
    }

    @Test
    public void testDefaultTrigger() {
        builder.setIndex(TEST_INDEX);

        JdkDioGpioPin testPin = builder.build();

        assertEquals(JdkDioGpioPinBuilder.DEFAULT_TRIGGER, testPin.getTrigger());
    }

    @Test
    public void testDefaultActiveLow() {
        builder.setIndex(TEST_INDEX);

        JdkDioGpioPin testPin = builder.build();

        assertEquals(JdkDioGpioPinBuilder.DEFAULT_ACTIVE_LOW, testPin.isActiveLow());
    }

    @Test
    public void testDefaultInitialValue() {
        builder.setIndex(TEST_INDEX);

        JdkDioGpioPin testPin = builder.build();

        assertEquals(JdkDioGpioPinBuilder.DEFAULT_INITIAL_VALUE, testPin.getInitialValue());
    }

    @Test(expected = GpioDeviceException.class)
    public void testIncompleteBuild() {
        builder.setName(TEST_NAME);
        builder.setDirection(GpioDirection.OUTPUT);
        builder.setMode(GpioMode.PUSH_PULL);
        builder.setTrigger(GpioTrigger.NONE);

        builder.build();

        fail(INVALID_DEVICE_CONFIGURATION_EXCEPTION_MESSAGE);
    }

    @Test(expected = GpioDeviceException.class)
    public void testSetIncompatibleInputModeBuild() {
        builder.setIndex(TEST_INDEX);
        builder.setName(TEST_NAME);
        builder.setDirection(GpioDirection.INPUT);
        builder.setMode(GpioMode.PUSH_PULL);
        builder.setTrigger(GpioTrigger.NONE);

        builder.build();

        fail(INVALID_DEVICE_CONFIGURATION_EXCEPTION_MESSAGE);
    }

    @Test(expected = GpioDeviceException.class)
    public void testSetIncompatibleOutputModeBuild() {
        builder.setIndex(TEST_INDEX);
        builder.setName(TEST_NAME);
        builder.setDirection(GpioDirection.OUTPUT);
        builder.setMode(GpioMode.PULL_UP);
        builder.setTrigger(GpioTrigger.NONE);

        builder.build();

        fail(INVALID_DEVICE_CONFIGURATION_EXCEPTION_MESSAGE);
    }

    @Test(expected = GpioDeviceException.class)
    public void testSetIncompatibleOutputWithTriggerBuild() {
        builder.setIndex(TEST_INDEX);
        builder.setName(TEST_NAME);
        builder.setDirection(GpioDirection.OUTPUT);
        builder.setMode(GpioMode.OPEN_DRAIN);
        builder.setTrigger(GpioTrigger.RISING_EDGE);

        builder.build();

        fail(INVALID_DEVICE_CONFIGURATION_EXCEPTION_MESSAGE);
    }

    @Test
    public void testSetIndex() {
        builder.setIndex(TEST_INDEX);
        JdkDioGpioPin testPin = builder.build();

        assertEquals(TEST_INDEX, testPin.getIndex());
    }

    @Test
    public void testSetName() {
        builder.setIndex(TEST_INDEX);
        builder.setName(TEST_NAME);
        JdkDioGpioPin testPin = builder.build();

        assertEquals(TEST_NAME, testPin.getName());
    }

    @Test
    public void testSetDirection() {
        builder.setIndex(TEST_INDEX);
        builder.setDirection(TEST_DIRECTION);
        JdkDioGpioPin testPin = builder.build();

        assertEquals(TEST_DIRECTION, testPin.getDirection());
    }

    @Test
    public void testSetMode() {
        builder.setIndex(TEST_INDEX);
        builder.setMode(TEST_MODE);
        JdkDioGpioPin testPin = builder.build();

        assertEquals(TEST_MODE, testPin.getMode());
    }

    @Test
    public void testSetTrigger() {
        builder.setIndex(TEST_INDEX);
        builder.setTrigger(TEST_TRIGGER);
        JdkDioGpioPin testPin = builder.build();

        assertEquals(TEST_TRIGGER, testPin.getTrigger());
    }

    @Test
    public void testSetActiveLow() {
        builder.setIndex(TEST_INDEX);
        builder.setActiveLow(TEST_ACTIVE_LOW);
        JdkDioGpioPin testPin = builder.build();

        assertEquals(TEST_ACTIVE_LOW, testPin.isActiveLow());
    }

    @Test
    public void testSetInitialValue() {
        builder.setIndex(TEST_INDEX);
        builder.setInitialValue(TEST_INITIAL_VALUE);
        JdkDioGpioPin testPin = builder.build();

        assertEquals(TEST_INITIAL_VALUE, testPin.getInitialValue());
    }
}