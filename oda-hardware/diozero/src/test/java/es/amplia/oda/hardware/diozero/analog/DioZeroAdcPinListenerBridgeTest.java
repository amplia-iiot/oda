package es.amplia.oda.hardware.diozero.analog;

import com.diozero.api.AnalogInputEvent;
import es.amplia.oda.core.commons.adc.AdcChannelListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DioZeroAdcPinListenerBridge.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class DioZeroAdcPinListenerBridgeTest {

    private static final AnalogInputEvent TEST_EVENT =
            new AnalogInputEvent(1, System.currentTimeMillis(), System.nanoTime(), 5.0f);

    @Mock
    private AdcChannelListener mockedListener;
    @InjectMocks
    private DioZeroAdcPinListenerBridge testBridge;

    @Mock
    private DioZeroAdcEvent mockedEvent;

    @Test
    public void testValueChanged() throws Exception {
        PowerMockito.whenNew(DioZeroAdcEvent.class).withAnyArguments().thenReturn(mockedEvent);

        testBridge.valueChanged(TEST_EVENT);

        PowerMockito.verifyNew(DioZeroAdcEvent.class).withArguments(eq(TEST_EVENT));
        verify(mockedListener).channelValueChanged(eq(mockedEvent));
    }
}