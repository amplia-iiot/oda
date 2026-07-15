package es.amplia.oda.hardware.jdkdio.gpio;

import es.amplia.oda.core.commons.gpio.*;

import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;
import jdk.dio.gpio.PinListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioPin.GPIO_BASE_PATH;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
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
        GPIOPinConfig mockedPinConfig = mock(GPIOPinConfig.class);
        GPIOPin mockedPin = mock(GPIOPin.class);

        try (MockedConstruction<GPIOPinConfig.Builder> builderCons = mockConstruction(GPIOPinConfig.Builder.class,
                (mock, mctx) -> {
                    when(mock.setPinNumber(anyInt())).thenReturn(mock);
                    when(mock.setDirection(anyInt())).thenReturn(mock);
                    when(mock.setDriveMode(anyInt())).thenReturn(mock);
                    when(mock.setTrigger(anyInt())).thenReturn(mock);
                    when(mock.setInitValue(anyBoolean())).thenReturn(mock);
                    when(mock.build()).thenReturn(mockedPinConfig);
                });
             MockedStatic<JdkDioGpioPinFactory> mockedFactory = mockStatic(JdkDioGpioPinFactory.class)) {
            mockedFactory.when(() -> JdkDioGpioPinFactory.createAndOpen(any(GPIOPinConfig.class)))
                    .thenReturn(mockedPin);

            testJdkDioGpioPin.open();

            assertEquals(1, builderCons.constructed().size());
            verify(builderCons.constructed().get(0)).build();
            mockedFactory.verify(() -> JdkDioGpioPinFactory.createAndOpen(eq(mockedPinConfig)));
            assertEquals(mockedPin, Whitebox.getInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME));
        }
    }

    @Test
    public void testOpenPullModeInitializationNeeded() throws Exception {
        JdkDioGpioPin pinWithInitializationNeeded =
                new JdkDioGpioPin(TEST_INDEX, TEST_NAME, GpioDirection.INPUT, GpioMode.PULL_UP, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);
        String pinPullFilePath = GPIO_BASE_PATH + "/gpio" + TEST_INDEX + "/pull";

        GPIOPinConfig mockedPinConfig = mock(GPIOPinConfig.class);
        GPIOPin mockedPin = mock(GPIOPin.class);

        try (MockedConstruction<GPIOPinConfig.Builder> builderCons = mockConstruction(GPIOPinConfig.Builder.class,
                (mock, mctx) -> {
                    when(mock.setPinNumber(anyInt())).thenReturn(mock);
                    when(mock.setDirection(anyInt())).thenReturn(mock);
                    when(mock.setDriveMode(anyInt())).thenReturn(mock);
                    when(mock.setTrigger(anyInt())).thenReturn(mock);
                    when(mock.setInitValue(anyBoolean())).thenReturn(mock);
                    when(mock.build()).thenReturn(mockedPinConfig);
                });
             MockedStatic<JdkDioGpioPinFactory> mockedFactory = mockStatic(JdkDioGpioPinFactory.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFactory.when(() -> JdkDioGpioPinFactory.createAndOpen(any(GPIOPinConfig.class)))
                    .thenReturn(mockedPin);

            pinWithInitializationNeeded.open();

            assertEquals(1, builderCons.constructed().size());
            verify(builderCons.constructed().get(0)).build();
            mockedFactory.verify(() -> JdkDioGpioPinFactory.createAndOpen(eq(mockedPinConfig)));
            mockedFiles.verify(() -> Files.write(eq(Paths.get(pinPullFilePath)), eq("up".getBytes())));
            assertEquals(mockedPin, Whitebox.getInternalState(pinWithInitializationNeeded, JDK_DIO_PIN_FIELD_NAME));
        }
    }

    @Test(expected = GpioDeviceException.class)
    public void testOpenIOException() throws Exception {
        GPIOPinConfig mockedPinConfig = mock(GPIOPinConfig.class);

        try (MockedConstruction<GPIOPinConfig.Builder> builderCons = mockConstruction(GPIOPinConfig.Builder.class,
                (mock, mctx) -> {
                    when(mock.setPinNumber(anyInt())).thenReturn(mock);
                    when(mock.setDirection(anyInt())).thenReturn(mock);
                    when(mock.setDriveMode(anyInt())).thenReturn(mock);
                    when(mock.setTrigger(anyInt())).thenReturn(mock);
                    when(mock.setInitValue(anyBoolean())).thenReturn(mock);
                    when(mock.build()).thenReturn(mockedPinConfig);
                });
             MockedStatic<JdkDioGpioPinFactory> mockedFactory = mockStatic(JdkDioGpioPinFactory.class)) {
            mockedFactory.when(() -> JdkDioGpioPinFactory.createAndOpen(any(GPIOPinConfig.class)))
                    .thenThrow(new IOException());

            testJdkDioGpioPin.open();

            fail("GPIO device exception must be thrown");
        }
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

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(true);

        List<List<?>> bridgeListenerArgs = new ArrayList<>();
        try (MockedConstruction<JdkDioGpioPinListenerBridge> bridgeListenerCons =
                     mockConstruction(JdkDioGpioPinListenerBridge.class,
                             (mock, mctx) -> bridgeListenerArgs.add(new ArrayList<>(mctx.arguments())))) {

            testJdkDioGpioPin.addGpioPinListener(mockedListener);

            assertEquals(1, bridgeListenerCons.constructed().size());
            assertEquals(mockedListener, bridgeListenerArgs.get(0).get(0));
            assertEquals(TEST_ACTIVE_LOW, bridgeListenerArgs.get(0).get(1));
            verify(mockedPin).setInputListener(eq(bridgeListenerCons.constructed().get(0)));
        }
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

        Whitebox.setInternalState(testJdkDioGpioPin, JDK_DIO_PIN_FIELD_NAME, mockedPin);

        when(mockedPin.isOpen()).thenReturn(true);
        doThrow(IOException.class).when(mockedPin).setInputListener(any(PinListener.class));

        try (MockedConstruction<JdkDioGpioPinListenerBridge> bridgeListenerCons =
                     mockConstruction(JdkDioGpioPinListenerBridge.class)) {
            testJdkDioGpioPin.addGpioPinListener(mockedListener);
        }

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
        doThrow(IOException.class).when(mockedPin).setInputListener(nullable(PinListener.class));

        testJdkDioGpioPin.removeGpioPinListener();

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

}
