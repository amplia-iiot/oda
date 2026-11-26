package es.amplia.oda.hardware.diozero.analog;

import es.amplia.oda.core.commons.adc.BadAdcChannelException;
import es.amplia.oda.hardware.diozero.analog.devices.fx30.DioZeroAdcPinMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class DioZeroAdcPinMapperTest {

	@Test
	public void testMapChannelIndexToDevicePinZero() {
		assertEquals(1, DioZeroAdcPinMapper.mapChannelIndexToDevicePin(0));
	}

	@Test
	public void testMapChannelIndexToDevicePinOne() {
		assertEquals(5, DioZeroAdcPinMapper.mapChannelIndexToDevicePin(1));
	}

	@Test
	public void testMapChannelIndexToDevicePinBadAdcChannelException() {
		assertThrows(BadAdcChannelException.class, () -> DioZeroAdcPinMapper.mapChannelIndexToDevicePin(99));
	}
}
