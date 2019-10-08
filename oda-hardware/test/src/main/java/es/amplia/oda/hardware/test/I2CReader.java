package es.amplia.oda.hardware.test;

import com.diozero.api.I2CDevice;
import es.amplia.oda.core.commons.exceptions.ConfigurationException;

import java.io.*;

public class I2CReader {
	public static void doYourThing() {
		I2CDevice device = new I2CDevice(0,104 );
		if(!device.isOpen() && device.probe()) {
			throw new ConfigurationException("Error");
		}

		byte[] bytesRegister = new byte[] {(byte)0xA0};
		device.write(bytesRegister);

		byte[] data = new byte[]{0x00, 0x00, 0x00};

		for(int i = 0 ; i < 3; i++) {
			data[i] = device.readByte(160);
		}

		for (byte b : data) {
			System.out.println(b);
		}
	}

	public static void doWhateverThatWorks() throws IOException {
		File i2cFile = new File("/dev/i2c-0");
		OutputStream writer = new FileOutputStream(i2cFile);
		InputStream reader = new FileInputStream(i2cFile);

		if(i2cFile.setReadable(true) && i2cFile.setWritable(true)) {
			/*writer.write(0xA0);*/
			byte[] bytes = new byte[3];
			int readed = reader.read(bytes,0,3);
		}
	}
}
