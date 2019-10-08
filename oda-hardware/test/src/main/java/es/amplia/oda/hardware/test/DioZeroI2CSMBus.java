package es.amplia.oda.hardware.test;

import com.diozero.api.I2CDevice;
import com.diozero.internal.provider.sysfs.I2CSMBusInterface;
import com.diozero.util.FileUtil;
import com.diozero.util.RuntimeIOException;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DioZeroI2CSMBus implements I2CSMBusInterface {

	private RandomAccessFile deviceFile;
	private int controller;
	private int deviceAddress;

	public DioZeroI2CSMBus(int controller, int deviceAddress) {
		this.controller = controller;
		this.deviceAddress = deviceAddress;
		String device_file = "/dev/i2c-" + controller;

		try {
			deviceFile = new RandomAccessFile(device_file, "rwd");
			int fd = FileUtil.getNativeFileDescriptor(deviceFile.getFD());
		} catch (IOException e) {
			close();
			throw new RuntimeIOException("Error opening I2C device");
		}
	}

	@Override
	public void close() {

	}

	@Override
	public boolean probe(I2CDevice.ProbeMode mode) {
		return false;
	}

	@Override
	public void writeQuick(byte bit) {

	}

	@Override
	public byte readByte() {
		return 0;
	}

	@Override
	public void writeByte(byte data) {

	}

	@Override
	public byte[] readBytes(int length) {
		return new byte[0];
	}

	@Override
	public void writeBytes(byte[] data) {

	}

	@Override
	public byte readByteData(int register) {
		return 0;
	}

	@Override
	public void writeByteData(int register, byte data) {

	}

	@Override
	public short readWordData(int register) {
		return 0;
	}

	@Override
	public void writeWordData(int register, short data) {

	}

	@Override
	public short processCall(int register, short data) {
		return 0;
	}

	@Override
	public byte[] readBlockData(int register) {
		return new byte[0];
	}

	@Override
	public void writeBlockData(int register, byte[] data) {

	}

	@Override
	public byte[] blockProcessCall(int register, byte[] data, int length) {
		return new byte[0];
	}

	@Override
	public byte[] readI2CBlockData(int register, int length) {
		return new byte[0];
	}

	@Override
	public void writeI2CBlockData(int register, byte[] data) {

	}
}
