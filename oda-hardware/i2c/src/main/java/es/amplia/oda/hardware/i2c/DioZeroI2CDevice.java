package es.amplia.oda.hardware.i2c;

import es.amplia.oda.core.commons.i2c.I2CDevice;

import java.nio.ByteBuffer;

import static java.lang.Thread.sleep;

public class DioZeroI2CDevice implements I2CDevice {

	private final String name;
	private final int register;
	private final com.diozero.api.I2CDevice i2cDevice;
	private final double minimum;
	private final double maximum;

	public DioZeroI2CDevice(String name, int register, com.diozero.api.I2CDevice device, double min, double max) {
		this.name = name;
		this.register = register;
		this.i2cDevice = device;
		this.minimum = min;
		this.maximum = max;
	}

	@Override
	public int getAddress() {
		return this.i2cDevice.getAddress();
	}

	@Override
	public int getController() {
		return this.i2cDevice.getController();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public double readRawData() {
		synchronized (i2cDevice) {
			return (this.i2cDevice.readUInt(this.register));
		}
	}

	@Override
	public double readScaledData() {
		return (this.i2cDevice.readUInt(this.register) - minimum) / (maximum - minimum);
	}

	@Override
	public ByteBuffer read(int count) {
		return this.i2cDevice.read(this.register, count);
	}

	@Override
	public byte readByte() {
		return this.i2cDevice.readByte(this.register);
	}

	@Override
	public void write(ByteBuffer bytebuffer) {
		this.i2cDevice.write(bytebuffer, bytebuffer.capacity() - bytebuffer.position());
	}

	@Override
	public void writeByte(byte b) {
		this.i2cDevice.writeByte(b);
	}

	@Override
	public boolean isOpen() {
		return this.i2cDevice.isOpen();
	}

	@Override
	public void close() {
		this.i2cDevice.close();
	}
}
