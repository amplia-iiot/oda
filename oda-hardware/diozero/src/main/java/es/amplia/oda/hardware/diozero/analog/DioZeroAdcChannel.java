package es.amplia.oda.hardware.diozero.analog;

import com.diozero.api.AnalogInputDevice;
import es.amplia.oda.core.commons.adc.AdcChannel;
import es.amplia.oda.core.commons.adc.AdcChannelListener;
import es.amplia.oda.core.commons.adc.AdcDeviceException;

public class DioZeroAdcChannel implements AdcChannel {

	private final int pinNumber;
	private final AnalogInputDevice device;


	public DioZeroAdcChannel(int pinNumber, AnalogInputDevice device) {
		this.pinNumber = pinNumber;
		this.device = device;
	}

	@Override
	public int getIndex() {
		return device.getGpio();
	}

	@Override
	public int getPin() {
		return pinNumber;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public float getRange() {
		return device.getRange();
	}

	@Override
	public float getScaledValue() {
		return device.getScaledValue();
	}

	@Override
	public float getUnscaledValue() {
		return device.getUnscaledValue();
	}

	@Override
	public void addAdcPinListener(AdcChannelListener listener) {
		checkDeviceOpened();
		device.addListener(new DioZeroAdcPinListenerBridge(listener));
	}

	@Override
	public void removeAllAdcPinListener() {
		checkDeviceOpened();
		device.removeAllListeners();
	}

	private void checkDeviceOpened() {
		if (this.device == null) {
			throw new AdcDeviceException("There is not a device registered to do operations");
		}
	}

	@Override
	public void close() {
		device.close();
	}
}
