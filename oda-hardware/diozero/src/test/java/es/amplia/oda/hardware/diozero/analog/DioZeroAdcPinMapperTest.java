package es.amplia.oda.hardware.diozero.analog;

import es.amplia.oda.core.commons.adc.BadAdcChannelException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class DioZeroAdcPinMapperTest {

	@Test
	public void testMapChannelIndexToDevicePinZero() {
		assertEquals(1, DioZeroAdcPinMapper.mapChannelIndexToDevicePin(0));
	}

	@Test
	public void testMapChannelIndexToDevicePinOne() {
		assertEquals(5, DioZeroAdcPinMapper.mapChannelIndexToDevicePin(1));
	}

	@Test(expected = BadAdcChannelException.class)
	public void testMapChannelIndexToDevicePinBadAdcChannelException() {
		DioZeroAdcPinMapper.mapChannelIndexToDevicePin(99);

		fail("Bad ADC Channel Exception should be thrown");
	}
}
