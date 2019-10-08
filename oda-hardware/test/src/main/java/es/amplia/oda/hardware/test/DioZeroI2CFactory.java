package es.amplia.oda.hardware.test;

import com.diozero.api.PinInfo;
import com.diozero.internal.provider.DeviceInterface;
import com.diozero.internal.provider.I2CDeviceFactoryInterface;
import com.diozero.internal.provider.I2CDeviceInterface;
import com.diozero.util.BoardPinInfo;
import com.diozero.util.RuntimeIOException;

public class DioZeroI2CFactory implements I2CDeviceFactoryInterface {
	private String name;

	@Override
	public I2CDeviceInterface createI2CDevice(String key, int controller, int address, int addressSize, int clockFrequency) throws RuntimeIOException {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isDeviceOpened(String key) {
		return false;
	}

	@Override
	public void deviceOpened(DeviceInterface device) {

	}

	@Override
	public void deviceClosed(DeviceInterface device) {

	}

	@Override
	public BoardPinInfo getBoardPinInfo() {
		return null;
	}

	@Override
	public String createPinKey(PinInfo pinInfo) {
		return null;
	}

	@Override
	public String createI2CKey(int controller, int address) {
		return null;
	}

	@Override
	public String createSpiKey(int controller, int chipSelect) {
		return null;
	}

	@Override
	public void close() throws RuntimeIOException {

	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public DeviceInterface getDevice(String key) {
		return null;
	}

	@Override
	public <T extends DeviceInterface> T getDevice(String key, Class<T> deviceClass) {
		return null;
	}
}
