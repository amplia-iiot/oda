package es.amplia.oda.core.commons.i2c;

import java.nio.ByteBuffer;

public interface I2CDevice {

	/**
	 * Getter of address.
	 * @return an int with the address where is located the device.
	 */
	int getAddress();

	/**
	 * Getter of controller.
	 * @return an int with the controller that handles the device.
	 */
	int getController();

	/**
	 * Getter of name..
	 * @return an String with the name specified through configuration to the device.
	 */
	String getName();

	/**
	 * Read an unsigned integer from the device (on the file of the controller, in the address and register specified).
	 * @return double representing the actual value read from the device.
	 * @throws InterruptedException if there is an error during the wait of bus are ready
	 */
	double readRawData() throws InterruptedException;

	/**
	 * Read an unsigned integer like readRawData() but converts the value into a decimal value 0-1 depending of the scale
	 * specified by configuration.
	 * @return double representing actual value between 0 and 1.
	 * @throws InterruptedException if there is an error while the bus is waiting to be ready
	 */
	double readScaledData() throws InterruptedException;

	/**
	 * Read the specified amount of bytes and return it in a byteBuffer.
	 * @param count Amount of bytes that the method will read from the device.
	 * @return bytes read from the device.
	 * @see ByteBuffer
	 */
	ByteBuffer read(int count);

	/**
	 * Read a byte from the device.
	 * @return byte readed from the device.
	 */
	byte readByte();

	/**
	 * Write the content of data into device.
	 * @param data data that will be write into the device.
	 */
	void write(float data);

	/**
	 * Write a byte into device
	 * @param b byte that will be written in the device
	 */
	void writeByte(byte b);

	/**
	 * Check if the device is open
	 * @return true if device is open, false in another case
	 */
	boolean isOpen();

	/**
	 * Close the device
	 */
	void close();
}
