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
	 * @return long representing the actual value read from the device.
	 */
	long readUInt();

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
	 * Write the content of bytebuffer in the device. If the bytebuffer is started (position is not on byte #0),
	 * method will read until it reach the end of bytebuffer.
	 * @param bytebuffer data that will be write into the device.
	 * @see ByteBuffer
	 */
	void write(ByteBuffer bytebuffer);

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
