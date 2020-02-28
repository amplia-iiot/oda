package es.amplia.oda.hardware.diozero.analog.devices.fx30;

import es.amplia.oda.core.commons.adc.DeviceType;

import com.diozero.api.PinInfo;
import com.diozero.internal.provider.AbstractDeviceFactory;
import com.diozero.internal.provider.AnalogInputDeviceFactoryInterface;
import com.diozero.util.BoardInfo;
import com.diozero.util.BoardPinInfo;
import com.diozero.util.DeviceFactoryHelper;

public class Fx30AnalogInputDeviceFactory extends AbstractDeviceFactory implements AnalogInputDeviceFactoryInterface {

	private final String name;
	private final float vRef;
	private final boolean activeLow;
	private final String path;

	private BoardInfo boardInfo;

	public Fx30AnalogInputDeviceFactory(String name, String path, boolean activeLow, DeviceType deviceType) {
		super(deviceType.toString() + "-" + name);

		this.vRef = 1.8f;
		this.name = name;
		this.path = path;
		this.activeLow = activeLow;
	}

	@Override
	public Fx30AnalogInputDevice createAnalogInputDevice(String key, PinInfo pinInfo) {
		int device = DioZeroAdcPinMapper.mapChannelIndexToDevicePin(pinInfo.getDeviceNumber());
		return new Fx30AnalogInputDevice(this, key, device, pinInfo.getDeviceNumber(), path, vRef);
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
