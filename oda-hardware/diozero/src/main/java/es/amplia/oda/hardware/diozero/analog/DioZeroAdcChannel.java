package es.amplia.oda.hardware.diozero.analog;

import com.diozero.api.AnalogInputDevice;
import es.amplia.oda.core.commons.diozero.AdcChannel;
import es.amplia.oda.core.commons.diozero.AdcChannelListener;
import es.amplia.oda.core.commons.diozero.AdcDeviceException;

public class DioZeroAdcChannel implements AdcChannel {

	private int pinNumber;

	private AnalogInputDevice device;

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
	public void close() throws AdcDeviceException {
		device.close();
	}

	@Override
	public float getRange() throws AdcDeviceException {
		return device.getRange();
	}

	@Override
	public float getScaledValue() throws AdcDeviceException {
		return device.getScaledValue();
	}

	@Override
	public float getUnscaledValue() throws AdcDeviceException {
		return device.getUnscaledValue();
	}

	@Override
	public void addAdcPinListener(AdcChannelListener listener) throws AdcDeviceException {
		checkDeviceOpened();
		device.addListener(new DioZeroAdcPinListenerBridge(listener));
	}

	@Override
	public void removeAllAdcPinListener() throws AdcDeviceException {
		checkDeviceOpened();
		device.removeAllListeners();
	}

	private void checkDeviceOpened() throws AdcDeviceException {
		if (this.device == null) {
			throw new AdcDeviceException("There is not a device registered to do operations");
		}
	}
}
