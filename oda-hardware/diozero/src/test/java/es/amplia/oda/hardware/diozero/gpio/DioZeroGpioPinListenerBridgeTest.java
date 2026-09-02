package es.amplia.oda.hardware.diozero.gpio;

import es.amplia.oda.core.commons.gpio.GpioPinListener;

import com.diozero.api.DigitalInputEvent;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class DioZeroGpioPinListenerBridgeTest {

    @Test
    public void valueChanged() {
        boolean testValue = true;
        GpioPinListener mockedListener = mock(GpioPinListener.class);
        DigitalInputEvent mockedEvent = mock(DigitalInputEvent.class);
        DioZeroGpioPinListenerBridge testListener = new DioZeroGpioPinListenerBridge(mockedListener, false);

        when(mockedEvent.getValue()).thenReturn(testValue);

        testListener.valueChanged(mockedEvent);

        verify(mockedEvent).getValue();
        verify(mockedListener).pinValueChanged(eq(testValue));
    }

    @Test
    public void valueChangedActiveLow() {
        boolean testValue = true;
        GpioPinListener mockedListener = mock(GpioPinListener.class);
        DigitalInputEvent mockedEvent = mock(DigitalInputEvent.class);
        DioZeroGpioPinListenerBridge testListener = new DioZeroGpioPinListenerBridge(mockedListener, true);

        when(mockedEvent.getValue()).thenReturn(testValue);

        testListener.valueChanged(mockedEvent);

        verify(mockedEvent).getValue();
        verify(mockedListener).pinValueChanged(eq(!testValue));
    }
}
