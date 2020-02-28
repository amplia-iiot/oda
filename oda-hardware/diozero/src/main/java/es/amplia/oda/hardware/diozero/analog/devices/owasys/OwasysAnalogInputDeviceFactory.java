package es.amplia.oda.hardware.diozero.analog.devices.owasys;

import com.diozero.api.PinInfo;
import com.diozero.internal.provider.AbstractDeviceFactory;
import com.diozero.internal.provider.AnalogInputDeviceFactoryInterface;
import com.diozero.util.BoardInfo;
import com.diozero.util.BoardPinInfo;
import com.diozero.util.DeviceFactoryHelper;
import es.amplia.oda.core.commons.adc.DeviceType;

public class OwasysAnalogInputDeviceFactory extends AbstractDeviceFactory implements AnalogInputDeviceFactoryInterface {

	private final String name;
	private final boolean activeLow;
	private final String path;

	private BoardInfo boardInfo;

	public OwasysAnalogInputDeviceFactory(String name, String path, boolean activeLow, DeviceType deviceType) {
		super(deviceType.toString() + "-" + name);

		this.name = name;
		this.path = path;
		this.activeLow = activeLow;
	}

	@Override
	public OwasysAnalogInputDevice createAnalogInputDevice(String key, PinInfo pinInfo) {
		return new OwasysAnalogInputDevice(this, key, pinInfo.getDeviceNumber(), path);
	}

	@Override
	public float getVRef() {
		return activeLow ? 5f : 10f;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BoardPinInfo getBoardPinInfo() {
		if (boardInfo == null) {
			boardInfo = DeviceFactoryHelper.getNativeDeviceFactory().getBoardInfo();
		}
		return boardInfo;
	}
}
