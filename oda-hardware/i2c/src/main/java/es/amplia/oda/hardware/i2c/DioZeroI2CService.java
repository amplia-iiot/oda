package es.amplia.oda.hardware.i2c;

import es.amplia.oda.core.commons.i2c.I2CDevice;
import es.amplia.oda.core.commons.i2c.I2CDeviceException;
import es.amplia.oda.core.commons.i2c.I2CService;

import java.util.*;

public class DioZeroI2CService implements I2CService {

	private Map<Integer, Map<Integer, I2CDevice>> devices = new HashMap<>();

	@Override
	public I2CDevice getI2CFromAddress(int controller, int address) {
		return Optional.ofNullable(devices.get(controller).get(address))
				.orElseThrow(() ->
						new I2CDeviceException("No I2C device on controller " + controller + " and address " +  address));
	}

	@Override
	public I2CDevice getI2CFromName(String name) {
		for (Map<Integer, I2CDevice> address: devices.values()) {
			for (I2CDevice device :	address.values()) {
				if (device.getName().equals(name)) {
					return device;
				}
			}
		}
		throw new I2CDeviceException("No I2C device with name " + name);
	}

	@Override
	public List<I2CDevice> getAllI2CFromController(int controller) {
		return new ArrayList<>(this.devices.get(controller).values());
	}

	@Override
	public List<I2CDevice> getAllI2C() {
		List<I2CDevice> result = new ArrayList<>();
		devices.values().forEach(addresses -> result.addAll(addresses.values()));
		return result;
	}

	void close() {
		this.devices.values().forEach(addresses -> addresses.values().forEach(I2CDevice::close));
		devices.clear();
	}

	void loadConfiguration(List<I2CDevice> configuredDevices) {
		close();
		for (I2CDevice device : configuredDevices) {
			if (this.devices.get(device.getController()) == null) {
				this.devices.put(device.getController(), Collections.singletonMap(device.getAddress(), device));
			} else {
				this.devices.get(device.getController()).put(device.getAddress(), device);
			}
		}
	}
}
