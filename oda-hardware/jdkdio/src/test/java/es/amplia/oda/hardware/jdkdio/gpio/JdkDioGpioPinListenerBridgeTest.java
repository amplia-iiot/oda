package es.amplia.oda.hardware.jdkdio.gpio;

import es.amplia.oda.core.commons.gpio.GpioPinListener;

import jdk.dio.gpio.PinEvent;
import org.junit.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class JdkDioGpioPinListenerBridgeTest {
    @Test
    @SuppressWarnings("ConstantConditions")
    public void valueChanged() {
        boolean testValue = true;
        GpioPinListener mockedListener = mock(GpioPinListener.class);
        PinEvent mockedEvent = mock(PinEvent.class);
        JdkDioGpioPinListenerBridge testListener = new JdkDioGpioPinListenerBridge(mockedListener, false);

        when(mockedEvent.getValue()).thenReturn(testValue);

        testListener.valueChanged(mockedEvent);

        verify(mockedEvent).getValue();
        verify(mockedListener).pinValueChanged(eq(testValue));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void valueChangedActiveLow() {
        boolean testValue = true;
        GpioPinListener mockedListener = mock(GpioPinListener.class);
        PinEvent mockedEvent = mock(PinEvent.class);
        JdkDioGpioPinListenerBridge testListener = new JdkDioGpioPinListenerBridge(mockedListener, true);

        when(mockedEvent.getValue()).thenReturn(testValue);

        testListener.valueChanged(mockedEvent);

        verify(mockedEvent).getValue();
        verify(mockedListener).pinValueChanged(eq(!testValue));
    }
}