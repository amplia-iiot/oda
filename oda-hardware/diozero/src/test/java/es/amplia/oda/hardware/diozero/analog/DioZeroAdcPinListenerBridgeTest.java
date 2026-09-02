package es.amplia.oda.hardware.diozero.analog;

import com.diozero.api.AnalogInputEvent;
import es.amplia.oda.core.commons.adc.AdcChannelListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DioZeroAdcPinListenerBridgeTest {

    private static final AnalogInputEvent TEST_EVENT =
            new AnalogInputEvent(1, System.currentTimeMillis(), System.nanoTime(), 5.0f);

    @Mock
    private AdcChannelListener mockedListener;
    @InjectMocks
    private DioZeroAdcPinListenerBridge testBridge;

    @Test
    public void testValueChanged() throws Exception {
        List<List<?>> eventArgs = new ArrayList<>();

        try (MockedConstruction<DioZeroAdcEvent> eventCons = mockConstruction(DioZeroAdcEvent.class,
                (mock, mctx) -> eventArgs.add(new ArrayList<>(mctx.arguments())))) {

            testBridge.valueChanged(TEST_EVENT);

            assertEquals(1, eventCons.constructed().size());
            assertEquals(TEST_EVENT, eventArgs.get(0).get(0));
            verify(mockedListener).channelValueChanged(eq(eventCons.constructed().get(0)));
        }
    }
}
