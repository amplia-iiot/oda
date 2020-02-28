package es.amplia.oda.hardware.diozero.analog.devices.fx30;

import es.amplia.oda.core.commons.adc.BadAdcChannelException;

public class DioZeroAdcPinMapper {

	private DioZeroAdcPinMapper() {}

	public static int mapChannelIndexToDevicePin(int channelIndex) {
		switch (channelIndex) {
			case 0:
				return 1;
			case 1:
				return 5;
			default:
				throw new BadAdcChannelException("ADC Channel number " + channelIndex + " doesn't exists");
		}
	}
}
