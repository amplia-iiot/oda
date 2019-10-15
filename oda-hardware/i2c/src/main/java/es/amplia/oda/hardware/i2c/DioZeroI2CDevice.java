package es.amplia.oda.hardware.i2c;

import es.amplia.oda.core.commons.i2c.I2CDevice;

import java.nio.ByteBuffer;

public class DioZeroI2CDevice implements I2CDevice {

	private String name;
	private int register;
	private com.diozero.api.I2CDevice i2cDevice;

	DioZeroI2CDevice(String name, int controller, int address, int register) {
		this.name = name;
		this.register = register;
		this.i2cDevice = new com.diozero.api.I2CDevice(controller, address);
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
	public long readUInt() {
		return this.i2cDevice.readUInt(this.register);
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
