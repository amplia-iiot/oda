package es.amplia.oda.hardware.diozero.analog;

import com.diozero.api.AnalogInputDevice;
import es.amplia.oda.core.commons.adc.AdcChannelListener;
import es.amplia.oda.core.commons.adc.AdcDeviceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DioZeroAdcChannelTest {

	private static final int index = 0;
	private static final float range = 10;
	private static final int pinNumber = 5;
	private static final float scaledValue = 0.3f;
	private static final float unscaledValue = 3f;

	private DioZeroAdcChannel adcChannel;

	@Mock
	AnalogInputDevice mockedAnalogDevice;
	@Mock
	AdcChannelListener mockedListener;

	@BeforeEach
	public void prepareForTest() {
		when(mockedAnalogDevice.getGpio()).thenReturn(index);
		when(mockedAnalogDevice.getRange()).thenReturn(range);
		when(mockedAnalogDevice.getScaledValue()).thenReturn(scaledValue);
		when(mockedAnalogDevice.getUnscaledValue()).thenReturn(unscaledValue);
		adcChannel = new DioZeroAdcChannel(pinNumber, mockedAnalogDevice);
	}

	@Test
	public void testGetIndex() {
		assertEquals(index, adcChannel.getIndex());
	}

	@Test
	public void testGetPin() {
		assertEquals(pinNumber, adcChannel.getPin());
	}

	@Test
	public void testGetName() {
		assertNull(adcChannel.getName());
	}

	@Test
	public void testGetRange() {
		assertEquals(Float.valueOf(range), Float.valueOf(adcChannel.getRange()));
	}

	@Test
	public void testGetScaledValue() {
		assertEquals(Float.valueOf(scaledValue), Float.valueOf(adcChannel.getScaledValue()));
	}

	@Test
	public void testGetUnscaledValue() {
		assertEquals(Float.valueOf(unscaledValue), Float.valueOf(adcChannel.getUnscaledValue()));
	}

	@Test
	public void testAddAdcPinListener() {
		adcChannel.addAdcPinListener(mockedListener);

		verify(mockedAnalogDevice).addListener(any());
	}

	@Test
	public void testAddAdcPinListenerWithException() {
		Whitebox.setInternalState(adcChannel, "device", (Object) null);

		assertThrows(AdcDeviceException.class, () -> adcChannel.addAdcPinListener(mockedListener));
	}

	@Test
	public void testRemoveAllAdcPinListener() {
		adcChannel.removeAllAdcPinListener();

		verify(mockedAnalogDevice).removeAllListeners();
	}

	@Test
	public void testRemoveAllAdcPinListenerWithException() {
		Whitebox.setInternalState(adcChannel, "device", (Object) null);

		assertThrows(AdcDeviceException.class, () -> adcChannel.removeAllAdcPinListener());
	}

	@Test
	public void testClose() {
		adcChannel.close();

		verify(mockedAnalogDevice).close();
	}
}
