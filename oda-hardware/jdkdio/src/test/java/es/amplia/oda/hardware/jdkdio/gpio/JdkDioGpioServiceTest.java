package es.amplia.oda.hardware.jdkdio.gpio;

import es.amplia.oda.core.commons.gpio.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JdkDioGpioService.class)
public class JdkDioGpioServiceTest {

    private static final String TEST_NAME = "testPin";
    private static final int TEST_INDEX = 3;
    private static final GpioDirection TEST_DIRECTION = GpioDirection.OUTPUT;
    private static final GpioMode TEST_MODE = GpioMode.OPEN_DRAIN;
    private static final GpioTrigger TEST_TRIGGER = GpioTrigger.NONE;
    private static final boolean TEST_ACTIVE_LOW = true;
    private static final boolean TEST_INITIAL_VALUE = true;

    private static final String GPIO_DEVICE_EXCEPTION_MESSAGE = "Gpio Device Exception must be thrown";

    @Mock
    private JdkDioGpioPin mockedPin1;
    @Mock
    private JdkDioGpioPin mockedPin2;
    @Mock
    private JdkDioGpioPin mockedPin3;
    @Mock
    private JdkDioGpioPin mockedPin4;
    private Map<Integer, JdkDioGpioPin> spiedAvailablePins;

    private JdkDioGpioService testJdkDioGpioService;

    @Before
    public void setUp() {
        testJdkDioGpioService = new JdkDioGpioService();

        Map<Integer, JdkDioGpioPin> availablePins = new HashMap<>();
        availablePins.put(1, mockedPin1);
        availablePins.put(2, mockedPin2);
        availablePins.put(3, mockedPin3);
        availablePins.put(4, mockedPin4);

        spiedAvailablePins = spy(availablePins);
        Whitebox.setInternalState(testJdkDioGpioService, "pins", spiedAvailablePins);
    }

    @Test
    public void testConstructor() {
        assertNotNull(testJdkDioGpioService);
    }

    @Test
    public void testGetPinByName() {
        when(mockedPin3.getName()).thenReturn(TEST_NAME);

        GpioPin resultPin = testJdkDioGpioService.getPinByName(TEST_NAME);

        assertEquals(mockedPin3, resultPin);
    }

    @Test(expected = GpioDeviceException.class)
    public void testGetPinByNameEmptyPins() {
        Map<Integer, JdkDioGpioPin> noPins = new HashMap<>();

        Whitebox.setInternalState(testJdkDioGpioService, "pins", noPins);

        testJdkDioGpioService.getPinByName(TEST_NAME);

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test(expected = GpioDeviceException.class)
    public void testGetPinByNamePinNotFound() {
        when(mockedPin1.getName()).thenReturn("GPIO1");
        when(mockedPin2.getName()).thenReturn("GPIO2");
        when(mockedPin3.getName()).thenReturn("GPIO3");
        when(mockedPin4.getName()).thenReturn("GPIO4");

        testJdkDioGpioService.getPinByName(TEST_NAME);

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test
    public void testGetPinByNameWithConfigDifferentConfig() throws Exception {
        JdkDioGpioPin newMockedPin = mock(JdkDioGpioPin.class);

        when(mockedPin3.getIndex()).thenReturn(TEST_INDEX);
        when(mockedPin3.getName()).thenReturn(TEST_NAME);
        when(mockedPin3.getDirection()).thenReturn(TEST_DIRECTION);
        when(mockedPin3.getMode()).thenReturn(GpioMode.PUSH_PULL);
        when(mockedPin3.getTrigger()).thenReturn(TEST_TRIGGER);
        when(mockedPin3.isActiveLow()).thenReturn(false);
        when(mockedPin3.getInitialValue()).thenReturn(false);

        PowerMockito.whenNew(JdkDioGpioPin.class).withAnyArguments().thenReturn(newMockedPin);

        GpioPin resultPin =
                testJdkDioGpioService.getPinByName(TEST_NAME, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);

        assertEquals(newMockedPin, resultPin);
        PowerMockito.verifyNew(JdkDioGpioPin.class)
                .withArguments(eq(TEST_INDEX), eq(TEST_NAME), eq(TEST_DIRECTION), eq(TEST_MODE), eq(TEST_TRIGGER),
                        eq(TEST_ACTIVE_LOW), eq(TEST_INITIAL_VALUE));
        verify(spiedAvailablePins).put(eq(TEST_INDEX), eq(newMockedPin));
    }

    @Test
    public void testGetPinByNameWithConfigSameConfig() throws Exception {
        when(mockedPin3.getIndex()).thenReturn(TEST_INDEX);
        when(mockedPin3.getName()).thenReturn(TEST_NAME);
        when(mockedPin3.getDirection()).thenReturn(TEST_DIRECTION);
        when(mockedPin3.getMode()).thenReturn(TEST_MODE);
        when(mockedPin3.getTrigger()).thenReturn(TEST_TRIGGER);
        when(mockedPin3.isActiveLow()).thenReturn(TEST_ACTIVE_LOW);
        when(mockedPin3.getInitialValue()).thenReturn(TEST_INITIAL_VALUE);

        PowerMockito.whenNew(JdkDioGpioPin.class).withAnyArguments().thenReturn(mock(JdkDioGpioPin.class));

        GpioPin resultPin =
                testJdkDioGpioService.getPinByName(TEST_NAME, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);

        assertEquals(mockedPin3, resultPin);
        PowerMockito.verifyNew(JdkDioGpioPin.class, never())
                .withArguments(anyInt(), anyString(), any(GpioDirection.class), any(GpioMode.class),
                        any(GpioTrigger.class), anyBoolean(), anyBoolean());
        verify(spiedAvailablePins, never()).put(anyInt(), any(JdkDioGpioPin.class));
    }

    @Test
    public void testGetPinByNameWithConfigSameConfigOpenPin() {
        when(mockedPin3.getName()).thenReturn(TEST_NAME);
        when(mockedPin3.getDirection()).thenReturn(TEST_DIRECTION);
        when(mockedPin3.getMode()).thenReturn(TEST_MODE);
        when(mockedPin3.getTrigger()).thenReturn(TEST_TRIGGER);
        when(mockedPin3.isActiveLow()).thenReturn(TEST_ACTIVE_LOW);
        when(mockedPin3.getInitialValue()).thenReturn(TEST_INITIAL_VALUE);
        when(mockedPin3.isOpen()).thenReturn(true);

        GpioPin resultPin =
                testJdkDioGpioService.getPinByName(TEST_NAME, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);

        assertEquals(mockedPin3, resultPin);
        verify(mockedPin3).close();
    }

    @Test
    public void testGetPinByNameWithConfigDifferentConfigOpenPinCloseException() throws Exception {
        when(mockedPin3.getIndex()).thenReturn(TEST_INDEX);
        when(mockedPin3.getName()).thenReturn(TEST_NAME);
        when(mockedPin3.getDirection()).thenReturn(TEST_DIRECTION);
        when(mockedPin3.getMode()).thenReturn(GpioMode.PUSH_PULL);
        when(mockedPin3.getTrigger()).thenReturn(TEST_TRIGGER);
        when(mockedPin3.isActiveLow()).thenReturn(false);
        when(mockedPin3.getInitialValue()).thenReturn(false);
        when(mockedPin3.isOpen()).thenReturn(true);
        doThrow(GpioDeviceException.class).when(mockedPin3).close();

        PowerMockito.whenNew(JdkDioGpioPin.class).withAnyArguments().thenReturn(mock(JdkDioGpioPin.class));

        GpioPin resultPin =
                testJdkDioGpioService.getPinByName(TEST_NAME, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);

        assertEquals(mockedPin3, resultPin);
        verify(mockedPin3).close();
        PowerMockito.verifyNew(JdkDioGpioPin.class, never())
                .withArguments(anyInt(), anyString(), any(GpioDirection.class), any(GpioMode.class),
                        any(GpioTrigger.class), anyBoolean(), anyBoolean());
    }

    @Test(expected = GpioDeviceException.class)
    public void testGetPinByNameWithConfigEmptyPins() {
        Map<Integer, JdkDioGpioPin> noPins = new HashMap<>();

        Whitebox.setInternalState(testJdkDioGpioService, "pins", noPins);

        testJdkDioGpioService.getPinByName(TEST_NAME, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                TEST_INITIAL_VALUE);

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test(expected = GpioDeviceException.class)
    public void testGetPinByNameWithConfigNotFoundPin() {
        when(mockedPin1.getName()).thenReturn("GPIO1");
        when(mockedPin1.getName()).thenReturn("GPIO2");
        when(mockedPin1.getName()).thenReturn("GPIO3");
        when(mockedPin1.getName()).thenReturn("GPIO4");

        testJdkDioGpioService.getPinByName(TEST_NAME, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                TEST_INITIAL_VALUE);

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test
    public void testGetPinByIndex() {
        GpioPin resultPin = testJdkDioGpioService.getPinByIndex(TEST_INDEX);

        assertEquals(mockedPin3, resultPin);
    }

    @Test(expected = GpioDeviceException.class)
    public void testGetPinByIndexNoPins() {
        Map<Integer, JdkDioGpioPin> noPins = new HashMap<>();

        Whitebox.setInternalState(testJdkDioGpioService, "pins", noPins);

        testJdkDioGpioService.getPinByIndex(TEST_INDEX);

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test(expected = GpioDeviceException.class)
    public void testGetPinByIndexPinNotFound() {
        int nonExistentPin = 99;

        testJdkDioGpioService.getPinByIndex(nonExistentPin);

        fail(GPIO_DEVICE_EXCEPTION_MESSAGE);
    }

    @Test
    public void getPinByIndexWithConfigDifferentConfig() throws Exception {
        JdkDioGpioPin newMockedPin = mock(JdkDioGpioPin.class);

        when(mockedPin3.getIndex()).thenReturn(TEST_INDEX);
        when(mockedPin3.getName()).thenReturn(TEST_NAME);
        when(mockedPin3.getDirection()).thenReturn(TEST_DIRECTION);
        when(mockedPin3.getMode()).thenReturn(GpioMode.PUSH_PULL);
        when(mockedPin3.getTrigger()).thenReturn(TEST_TRIGGER);
        when(mockedPin3.isActiveLow()).thenReturn(false);
        when(mockedPin3.getInitialValue()).thenReturn(false);

        PowerMockito.whenNew(JdkDioGpioPin.class).withAnyArguments().thenReturn(newMockedPin);
        when(newMockedPin.getDirection()).thenReturn(TEST_DIRECTION);
        when(newMockedPin.getMode()).thenReturn(TEST_MODE);
        when(newMockedPin.getTrigger()).thenReturn(TEST_TRIGGER);
        when(newMockedPin.isActiveLow()).thenReturn(TEST_ACTIVE_LOW);
        when(newMockedPin.getInitialValue()).thenReturn(TEST_INITIAL_VALUE);

        GpioPin resultPin =
                testJdkDioGpioService.getPinByIndex(TEST_INDEX, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);

        assertEquals(mockedPin3, resultPin);
        PowerMockito.verifyNew(JdkDioGpioPin.class)
                .withArguments(eq(TEST_INDEX), eq(null), eq(TEST_DIRECTION), eq(TEST_MODE), eq(TEST_TRIGGER),
                        eq(TEST_ACTIVE_LOW), eq(TEST_INITIAL_VALUE));
        verify(spiedAvailablePins).put(eq(TEST_INDEX), eq(newMockedPin));
    }

    @Test
    public void getPinByIndexWithConfigSameConfig() throws Exception {
        JdkDioGpioPin newMockedPin = mock(JdkDioGpioPin.class);

        when(mockedPin3.getIndex()).thenReturn(TEST_INDEX);
        when(mockedPin3.getName()).thenReturn(TEST_NAME);
        when(mockedPin3.getDirection()).thenReturn(TEST_DIRECTION);
        when(mockedPin3.getMode()).thenReturn(TEST_MODE);
        when(mockedPin3.getTrigger()).thenReturn(TEST_TRIGGER);
        when(mockedPin3.isActiveLow()).thenReturn(TEST_ACTIVE_LOW);
        when(mockedPin3.getInitialValue()).thenReturn(TEST_INITIAL_VALUE);

        PowerMockito.whenNew(JdkDioGpioPin.class).withAnyArguments().thenReturn(newMockedPin);
        when(newMockedPin.getDirection()).thenReturn(TEST_DIRECTION);
        when(newMockedPin.getMode()).thenReturn(TEST_MODE);
        when(newMockedPin.getTrigger()).thenReturn(TEST_TRIGGER);
        when(newMockedPin.isActiveLow()).thenReturn(TEST_ACTIVE_LOW);
        when(newMockedPin.getInitialValue()).thenReturn(TEST_INITIAL_VALUE);

        GpioPin resultPin =
                testJdkDioGpioService.getPinByIndex(TEST_INDEX, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);

        assertEquals(mockedPin3, resultPin);
        PowerMockito.verifyNew(JdkDioGpioPin.class, never())
                .withArguments(anyInt(), anyString(), any(GpioDirection.class), any(GpioMode.class),
                        any(GpioTrigger.class), anyBoolean(), anyBoolean());
        verify(spiedAvailablePins, never()).put(anyInt(), any(JdkDioGpioPin.class));
    }

    @Test
    public void getPinByIndexWithConfigSameConfigOpenPin() throws Exception {
        JdkDioGpioPin newMockedPin = mock(JdkDioGpioPin.class);

        when(mockedPin3.getIndex()).thenReturn(TEST_INDEX);
        when(mockedPin3.getName()).thenReturn(TEST_NAME);
        when(mockedPin3.getDirection()).thenReturn(TEST_DIRECTION);
        when(mockedPin3.getMode()).thenReturn(TEST_MODE);
        when(mockedPin3.getTrigger()).thenReturn(TEST_TRIGGER);
        when(mockedPin3.isActiveLow()).thenReturn(TEST_ACTIVE_LOW);
        when(mockedPin3.getInitialValue()).thenReturn(TEST_INITIAL_VALUE);
        when(mockedPin3.isOpen()).thenReturn(true);

        PowerMockito.whenNew(JdkDioGpioPin.class).withAnyArguments().thenReturn(newMockedPin);
        when(newMockedPin.getDirection()).thenReturn(TEST_DIRECTION);
        when(newMockedPin.getMode()).thenReturn(TEST_MODE);
        when(newMockedPin.getTrigger()).thenReturn(TEST_TRIGGER);
        when(newMockedPin.isActiveLow()).thenReturn(TEST_ACTIVE_LOW);
        when(newMockedPin.getInitialValue()).thenReturn(TEST_INITIAL_VALUE);

        GpioPin resultPin =
                testJdkDioGpioService.getPinByIndex(TEST_INDEX, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);

        assertEquals(mockedPin3, resultPin);
        verify(mockedPin3).close();
        PowerMockito.verifyNew(JdkDioGpioPin.class, never())
                .withArguments(anyInt(), anyString(), any(GpioDirection.class), any(GpioMode.class),
                        any(GpioTrigger.class), anyBoolean(), anyBoolean());
        verify(spiedAvailablePins, never()).put(anyInt(), any(JdkDioGpioPin.class));
    }

    @Test
    public void getPinByIndexWithConfigSameConfigOpenPinCloseException() throws Exception {
        JdkDioGpioPin newMockedPin = mock(JdkDioGpioPin.class);

        when(mockedPin3.getIndex()).thenReturn(TEST_INDEX);
        when(mockedPin3.getName()).thenReturn(TEST_NAME);
        when(mockedPin3.getDirection()).thenReturn(TEST_DIRECTION);
        when(mockedPin3.getMode()).thenReturn(TEST_MODE);
        when(mockedPin3.getTrigger()).thenReturn(TEST_TRIGGER);
        when(mockedPin3.isActiveLow()).thenReturn(TEST_ACTIVE_LOW);
        when(mockedPin3.getInitialValue()).thenReturn(TEST_INITIAL_VALUE);
        when(mockedPin3.isOpen()).thenReturn(true);
        doThrow(GpioDeviceException.class).when(mockedPin3).close();

        PowerMockito.whenNew(JdkDioGpioPin.class).withAnyArguments().thenReturn(newMockedPin);

        GpioPin resultPin =
                testJdkDioGpioService.getPinByIndex(TEST_INDEX, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);

        assertEquals(mockedPin3, resultPin);
        verify(mockedPin3).close();
        PowerMockito.verifyNew(JdkDioGpioPin.class, never())
                .withArguments(anyInt(), anyString(), any(GpioDirection.class), any(GpioMode.class),
                        any(GpioTrigger.class), anyBoolean(), anyBoolean());
        verify(spiedAvailablePins, never()).put(anyInt(), any(JdkDioGpioPin.class));
    }

    @Test
    public void getPinByIndexWithConfigNewPin() throws Exception {
        JdkDioGpioPin newMockedPin = mock(JdkDioGpioPin.class);
        int newIndex = 99;

        PowerMockito.whenNew(JdkDioGpioPin.class).withAnyArguments().thenReturn(newMockedPin);

        GpioPin resultPin =
                testJdkDioGpioService.getPinByIndex(newIndex, TEST_DIRECTION, TEST_MODE, TEST_TRIGGER, TEST_ACTIVE_LOW,
                        TEST_INITIAL_VALUE);

        assertEquals(newMockedPin, resultPin);
        PowerMockito.verifyNew(JdkDioGpioPin.class)
                .withArguments(eq(newIndex), eq(null), eq(TEST_DIRECTION), eq(TEST_MODE), eq(TEST_TRIGGER),
                        eq(TEST_ACTIVE_LOW), eq(TEST_INITIAL_VALUE));
        verify(spiedAvailablePins).put(eq(newIndex), eq(newMockedPin));
    }

    @Test
    public void testGetAvailablePins() {
        Map<Integer, GpioPin> availablePins = testJdkDioGpioService.getAvailablePins();

        assertEquals(spiedAvailablePins.size(), availablePins.size());

        for (Map.Entry<Integer, JdkDioGpioPin> availablePin: spiedAvailablePins.entrySet()) {
            assertTrue(availablePins.containsKey(availablePin.getKey()));
            assertEquals(availablePin.getValue(), availablePins.get(availablePin.getKey()));
        }
    }

    @Test
    public void testAddConfiguredPin() {
        JdkDioGpioPin newMockedPin = mock(JdkDioGpioPin.class);

        when(newMockedPin.getIndex()).thenReturn(TEST_INDEX);
        when(newMockedPin.getDirection()).thenReturn(GpioDirection.INPUT);

        testJdkDioGpioService.addConfiguredPin(newMockedPin);

        verify(newMockedPin, never()).open();
        verify(spiedAvailablePins).put(eq(TEST_INDEX), eq(newMockedPin));
    }

    @Test
    public void testAddConfiguredPinOutputWithInitialValue() {
        JdkDioGpioPin newMockedPin = mock(JdkDioGpioPin.class);

        when(newMockedPin.getIndex()).thenReturn(TEST_INDEX);
        when(newMockedPin.getDirection()).thenReturn(TEST_DIRECTION);
        when(newMockedPin.getInitialValue()).thenReturn(TEST_INITIAL_VALUE);

        testJdkDioGpioService.addConfiguredPin(newMockedPin);

        verify(newMockedPin).open();
        verify(newMockedPin).setValue(eq(TEST_INITIAL_VALUE));
        verify(spiedAvailablePins).put(eq(TEST_INDEX), eq(newMockedPin));
    }

    @Test
    public void testRelease() {
        when(mockedPin2.isOpen()).thenReturn(true);
        when(mockedPin4.isOpen()).thenReturn(true);

        testJdkDioGpioService.release();

        spiedAvailablePins.values().forEach(pin -> verify(pin).isOpen());
        verify(mockedPin1, never()).close();
        verify(mockedPin2).close();
        verify(mockedPin3, never()).close();
        verify(mockedPin4).close();
    }

    @Test
    public void testReleaseCloseException() {
        when(mockedPin2.isOpen()).thenReturn(true);
        doThrow(GpioDeviceException.class).when(mockedPin2).close();
        when(mockedPin4.isOpen()).thenReturn(true);

        testJdkDioGpioService.release();

        spiedAvailablePins.values().forEach(pin -> verify(pin).isOpen());
        verify(mockedPin1, never()).close();
        verify(mockedPin2).close();
        verify(mockedPin3, never()).close();
        verify(mockedPin4).close();
    }
}