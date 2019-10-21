package es.amplia.oda.hardware.i2c;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;

import java.util.*;
import java.util.stream.Collectors;

public class DioZeroI2CService implements I2CService {

	private Map<String, I2CDevice> devices = new HashMap<>();

	@Override
	public I2CDevice getI2CFromAddress(int controller, int address) {
		for (I2CDevice device : devices.values()) {
			if (device.getController() == controller && device.getAddress() == address) {
				return device;
			}
		}
		throw new I2CDeviceException("No I2C device with controller " + controller + " and address " + address);
	}

	@Override
	public I2CDevice getI2CFromName(String name) {
		return Optional.ofNullable(devices.get(name))
				.orElseThrow(() -> new I2CDeviceException("No I2C device with name " + name));
	}

	@Override
	public List<I2CDevice> getAllI2CFromController(int controller) {
		return devices.values().stream().filter(device -> device.getController() == controller).collect(Collectors.toList());
	}

	@Override
	public List<I2CDevice> getAllI2C() {
		return new ArrayList<>(devices.values());
	}

	void close() {
		this.devices.values().forEach(I2CDevice::close);
		devices.clear();
	}

	public void loadConfiguration(List<I2CDevice> configuredDevices) {
		close();
		for (I2CDevice device : configuredDevices) {
			if (this.devices.get(device.getName()) == null) {

				this.devices.put(device.getName(), device);
			} else {
				throw new ConfigurationException("Two devices can't have the same name");
			}
		}
	}
}
