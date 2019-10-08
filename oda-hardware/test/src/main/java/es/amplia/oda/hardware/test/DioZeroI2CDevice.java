package es.amplia.oda.hardware.test;

import com.diozero.api.I2CDevice;
import com.diozero.internal.provider.DeviceFactoryInterface;
import com.diozero.internal.provider.I2CDeviceInterface;
import com.diozero.util.RuntimeIOException;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DioZeroI2CDevice implements I2CDeviceInterface {
	private static boolean USE_SYSFS = false;
	private static boolean I2C_SLAVE_FORCE = false;

	private I2CDevice device;

	public DioZeroI2CDevice(DeviceFactoryInterface deviceFactory, String key, int controller,
							int address, int addressSize, int frequency) {
	}

	@Override
	public boolean probe(com.diozero.api.I2CDevice.ProbeMode mode) throws RuntimeIOException {
		return true;
	}

	@Override
	public byte readByte() throws RuntimeIOException {
		return 0;
	}

	@Override
	public void writeByte(byte b) throws RuntimeIOException {

	}

	@Override
	public void read(ByteBuffer buffer) throws RuntimeIOException {

	}

	@Override
	public void write(ByteBuffer buffer) throws RuntimeIOException {

	}

	@Override
	public byte readByteData(int register) throws RuntimeIOException {
		return 0;
	}

	@Override
	public void writeByteData(int register, byte b) throws RuntimeIOException {

	}

	@Override
	public void readI2CBlockData(int register, int subAddressSize, ByteBuffer buffer) throws RuntimeIOException {

	}

	@Override
	public void writeI2CBlockData(int register, int subAddressSize, ByteBuffer buffer) throws RuntimeIOException {

	}

	@Override
	public String getKey() {
		return null;
	}

	@Override
	public boolean isOpen() {
		return this.device == null;
	}

	@Override
	public void close() {

	}
}
