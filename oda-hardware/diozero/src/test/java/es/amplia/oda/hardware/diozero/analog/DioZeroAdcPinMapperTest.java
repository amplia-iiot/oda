package es.amplia.oda.hardware.diozero.analog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DioZeroAdcPinMapperTest {

	@Test
	public void testMapChannelIndexToDevicePinZero() {
		assertEquals(1, DioZeroAdcPinMapper.mapChannelIndexToDevicePin(0));
	}

	@Test
	public void testMapChannelIndexToDevicePinOne() {
		assertEquals(5, DioZeroAdcPinMapper.mapChannelIndexToDevicePin(1));
	}
}
