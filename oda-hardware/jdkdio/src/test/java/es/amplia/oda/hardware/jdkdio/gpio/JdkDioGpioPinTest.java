package es.amplia.oda.hardware.jdkdio.gpio;

import es.amplia.oda.core.commons.gpio.*;

import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.gpio.PinListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioPin.GPIO_BASE_PATH;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JdkDioGpioPin.class, GPIOPinConfig.class, GPIOPinConfig.Builder.class, JdkDioGpioPinFactory.class,
                     Files.class })
@PowerMockIgnore("jdk.internal.reflect.*")
public class JdkDioGpioPinTest {

    private static final int TEST_INDEX = 1;
    private static final String TEST_NAME = "testGPIO";
    private static final GpioDirection TEST_DIRECTION = GpioDirection.OUTPUT;
    private static final GpioMode TEST_MODE = GpioMode.PUSH_PULL;
    private static final GpioTrigger TEST_TRIGGER = GpioTrigger.NONE;
    private static final boolean TEST_ACTIVE_LOW = true;
    private static final boolean TEST_INITIAL_VALUE = true;

    private static final String JDK_DIO_PIN_FIELD_NAME = "jdkDioPin";
    private static final String GPIO_DEVICE_EXCEPTION_MESSAGE = "GPIO Device exception must be thrown";

    private JdkDioGpioPin testJdkDioGpioPin;

    @Before
    public void setUp() {
        testJdkDioGpioPin = new JdkDioGpioPin(TEST_INDEX, TEST_NAME, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER,
                TEST_ACTIVE_LOW, TEST_INITIAL_VALUE);
    }

    @Test
    public void testGetIndex() {
        assertEquals(TEST_INDEX, testJdkDioGpioPin.getIndex());
    }

    @Test
    public void testGetName() {
        assertEquals(TEST_NAME, testJdkDioGpioPin.getName());
    }

    @Test
    public void testGetDirection() {
        assertEquals(TEST_DIRECTION, testJdkDioGpioPin.getDirection());
    }

    @Test
    public void testGetMode() {
        assertEquals(TEST_MODE, testJdkDioGpioPin.getMode());
    }

    @Test
    public void testGetTrigger() {
        assertEquals(TEST_TRIGGER, testJdkDioGpioPin.getTrigger());
    }

    @Test
    public void testIsActiveLow() {
        assertEquals(TEST_INITIAL_VALUE, testJdkDioGpioPin.isActiveLow());
    }

    @Test
    public void testGetInitialValue() {
        assertEquals(TEST_INITIAL_VALUE, testJdkDioGpioPin.getInitialValue());
    }

    @Test
    public void testIsOpenWhenInternalPinIsNull() {
        assertFalse(testJdkDioGpioPin.isOpen());
    }

    @Test
    public void testIsOpenWhenInternalPinIsClose() {
        GPIOPin mockedPin = mock(GPIOPin.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        assertFalse(testJdkDioGpioPin.isOpen());
    }

    @Test
    public void testIsOpenWhenPinIsOpen() {
        GPIOPin mockedPin = mock(GPIOPin.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);
        when(mockedPin.isOpen()).thenReturn(true);

        assertTrue(testJdkDioGpioPin.isOpen());
    }

    @Test
    public void testOpen() throws Exception {
        GPIOPinConfig.Builder mockedBuilder = mock(GPIOPinConfig.Builder.class);
        GPIOPinConfig mockedPinConfig = PowerMockito.mock(GPIOPinConfig.class);
        GPIOPin mockedPin = mock(GPIOPin.class);
        PowerMockito.mockStatic(JdkDioGpioPinFactory.class);

        PowerMockito.whenNew(GPIOPinConfig.Builder.class).withNoArguments().thenReturn(mockedBuilder);
        when(mockedBuilder.setPinNumber(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setDirection(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setDriveMode(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setTrigger(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setInitValue(anyBoolean())).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedPinConfig);
        PowerMockito.when(JdkDioGpioPinFactory.createAndOpen(any(GPIOPinConfig.class))).thenReturn(mockedPin);

        testJdkDioGpioPin.open();

        verify(mockedBuilder).build();
        PowerMockito.verifyStatic(JdkDioGpioPinFactory.class);
        JdkDioGpioPinFactory.createAndOpen(eq(mockedPinConfig));
        assertEquals(mockedPin, Whitebox.getInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME));
    }

    @Test
    public void testOpenPullModeInitializationNeeded() throws Exception {
        JdkDioGpioPin pinWithInitializationNeeded =
                new JdkDioGpioPin(TEST_INDEX, TEST_NAME, GpioDirection.INPUT, GpioMode.PULL_UP, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);
        String pinPullFilePath = GPIO_BASE_PATH + "/gpio" + TEST_INDEX + "/pull";

        GPIOPinConfig.Builder mockedBuilder = mock(GPIOPinConfig.Builder.class);
        GPIOPinConfig mockedPinConfig = PowerMockito.mock(GPIOPinConfig.class);
        GPIOPin mockedPin = mock(GPIOPin.class);
        PowerMockito.mockStatic(JdkDioGpioPinFactory.class);
        PowerMockito.mockStatic(Files.class);

        PowerMockito.whenNew(GPIOPinConfig.Builder.class).withNoArguments().thenReturn(mockedBuilder);
        when(mockedBuilder.setPinNumber(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setDirection(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setDriveMode(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setTrigger(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setInitValue(anyBoolean())).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedPinConfig);
        PowerMockito.when(JdkDioGpioPinFactory.createAndOpen(any(GPIOPinConfig.class))).thenReturn(mockedPin);

        pinWithInitializationNeeded.open();

        verify(mockedBuilder).build();
        PowerMockito.verifyStatic(JdkDioGpioPinFactory.class);
        JdkDioGpioPinFactory.createAndOpen(eq(mockedPinConfig));
        PowerMockito.verifyStatic(Files.class);
        Files.write(eq(Paths.get(pinPullFilePath)), eq("up".getBytes()));
        assertEquals(mockedPin, Whitebox.getInternalState(pinWithInitializationNeeded, JDK_DIO_PIN_FIELD_NAME));
    }

    @Test(expected = GpioDeviceException.class)
    public void testOpenIOException() throws Exception {
        GPIOPinConfig.Builder mockedBuilder = PowerMockito.mock(GPIOPinConfig.Builder.class);
        GPIOPinConfig mockedPinConfig = mock(GPIOPinConfig.class);
        PowerMockito.mockStatic(JdkDioGpioPinFactory.class);

        PowerMockito.whenNew(GPIOPinConfig.Builder.class).withNoArguments().thenReturn(mockedBuilder);
        when(mockedBuilder.setPinNumber(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setDirection(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setDriveMode(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setTrigger(anyInt())).thenReturn(mockedBuilder);
        when(mockedBuilder.setInitValue(anyBoolean())).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedPinConfig);
        PowerMockito.when(JdkDioGpioPinFactory.createAndOpen(any(GPIOPinConfig.class))).thenThrow(new IOException());

        testJdkDioGpioPin.open();

        fail("GPIO device exception must be thrown");
    }

    @Test
    public void testClose() throws IOException {
        GPIOPin mockedPin = mock(GPIOPin.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        testJdkDioGpioPin.close();

        verify(mockedPin).close();
        assertNull(Whitebox.getInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME));
    }

    @Test(expected = GpioDeviceException.class)
    public void testCloseIOException() throws IOException {
        GPIOPin mockedPin = mock(GPIOPin.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        doThrow(IOException.class).when(mockedPin).close();

        testJdkDioGpioPin.close();

        fail("GPIO device exception must be thrown");
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testGetValue() throws IOException {
        GPIOPin mockedPin = mock(GPIOPin.class);
        boolean testValue = true;

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(true);
        when(mockedPin.getValue()).thenReturn(testValue);

        assertEquals(testValue ^ TEST_ACTIVE_LOW, testJdkDioGpioPin.getValue());
        verify(mockedPin).getValue();
    }

    @Test(expected =  GpioDeviceException.class)
    public void testGetValuePinClosed() {
        GPIOPin mockedPin = mock(GPIOPin.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(false);

        testJdkDioGpioPin.getValue();

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test(expected =  GpioDeviceException.class)
    public void testGetValueIOException() throws IOException {
        GPIOPin mockedPin = mock(GPIOPin.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(true);
        when(mockedPin.getValue()).thenThrow(new IOException());

        testJdkDioGpioPin.getValue();

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testSetValue() throws IOException {
        GPIOPin mockedPin = mock(GPIOPin.class);
        boolean testValue = true;

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(true);

        testJdkDioGpioPin.setValue(testValue);

        verify(mockedPin).setValue(eq(testValue ^ TEST_ACTIVE_LOW));
    }

    @Test(expected = GpioDeviceException.class)
    @SuppressWarnings("ConstantConditions")
    public void testSetValuePinClosed() {
        GPIOPin mockedPin = mock(GPIOPin.class);
        boolean testValue = true;

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(false);

        testJdkDioGpioPin.setValue(testValue);

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test(expected = GpioDeviceException.class)
    @SuppressWarnings("ConstantConditions")
    public void testSetValueIOException() throws IOException {
        GPIOPin mockedPin = mock(GPIOPin.class);
        boolean testValue = true;

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(true);
        doThrow(IOException.class).when(mockedPin).setValue(anyBoolean());

        testJdkDioGpioPin.setValue(testValue);

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test
    public void testAddGpioPinListener() throws Exception {
        GPIOPin mockedPin = mock(GPIOPin.class);
        GpioPinListener mockedListener = mock(GpioPinListener.class);
        JdkDioGpioPinListenerBridge mockedBridgeListener = mock(JdkDioGpioPinListenerBridge.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(true);
        PowerMockito.whenNew(JdkDioGpioPinListenerBridge.class).withAnyArguments().thenReturn(mockedBridgeListener);

        testJdkDioGpioPin.addGpioPinListener(mockedListener);

        PowerMockito.verifyNew(JdkDioGpioPinListenerBridge.class).withArguments(eq(mockedListener), eq(TEST_ACTIVE_LOW));
        verify(mockedPin).setInputListener(eq(mockedBridgeListener));
    }

    @Test(expected = GpioDeviceException.class)
    public void testAddGpioPinListenerPinClosed() {
        GPIOPin mockedPin = mock(GPIOPin.class);
        GpioPinListener mockedListener = mock(GpioPinListener.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(false);

        testJdkDioGpioPin.addGpioPinListener(mockedListener);

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test(expected = GpioDeviceException.class)
    public void testAddGpioPinListenerIOException() throws Exception {
        GPIOPin mockedPin = mock(GPIOPin.class);
        GpioPinListener mockedListener = mock(GpioPinListener.class);
        JdkDioGpioPinListenerBridge mockedBridgeListener = mock(JdkDioGpioPinListenerBridge.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(true);
        PowerMockito.whenNew(JdkDioGpioPinListenerBridge.class).withAnyArguments().thenReturn(mockedBridgeListener);
        doThrow(IOException.class).when(mockedPin).setInputListener(any(PinListener.class));

        testJdkDioGpioPin.addGpioPinListener(mockedListener);

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test
    public void testRemoveGpioPinListener() throws IOException {
        GPIOPin mockedPin = mock(GPIOPin.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(true);

        testJdkDioGpioPin.removeGpioPinListener();

        verify(mockedPin).setInputListener(eq(null));
    }

    @Test(expected = GpioDeviceException.class)
    public void testRemoveGpioPinListenerPinClosed() {
        GPIOPin mockedPin = mock(GPIOPin.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(false);

        testJdkDioGpioPin.removeGpioPinListener();

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test(expected = GpioDeviceException.class)
    public void testRemoveGpioPinListenerIOException() throws IOException {
        GPIOPin mockedPin = mock(GPIOPin.class);

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(true);
        doThrow(IOException.class).when(mockedPin).setInputListener(any(PinListener.class));

        testJdkDioGpioPin.removeGpioPinListener();

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

}