package es.amplia.oda.hardware.diozero.analog;

import es.amplia.oda.core.commons.adc.BadAdcChannelException;

class DioZeroAdcPinMapper {

	private DioZeroAdcPinMapper() {}

	static int mapChannelIndexToDevicePin(int channelIndex) {
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
