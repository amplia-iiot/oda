package es.amplia.oda.core.commons.i2c;

import java.util.List;

public interface I2CService {
	/**
	 *	Search a device handled by the specified controller and located on the input address and returns it.
	 * @param controller Number of controller that contains the target device.
	 * @param address Address where the target device is located.
	 * @return If target device exists, return an I2CDevice with it, else throw an I2CDeviceException exception.
	 * @see I2CDevice
	 * @see I2CDeviceException
	 */
	I2CDevice getI2CFromAddress(int controller, int address);

	/**
	 *	Search a device with the specified name on every controllers and registered addresses.
	 * @param name Name of the target device.
	 * @return If target device exists, return an I2CDevice with it, else throw an I2CDeviceException exception.
	 * @see I2CDevice
	 * @see I2CDeviceException
	 */
	I2CDevice getI2CFromName(String name);

	/**
	 *	Search all devices handled by a specified controller.
	 * @param controller Number of the target controller where the method should search.
	 * @return If one or more devices exists, return a list of the I2CDevice's handled by controller, else return a void list.
	 * @see I2CDevice
	 */
	List<I2CDevice> getAllI2CFromController(int controller);

	/**
	 *	Search all devices registered.
	 * @return If one or more devices exists, return a list of the I2CDevice's handled by controller, else return a void list.
	 * @see I2CDevice
	 */
	List<I2CDevice> getAllI2C();
}
