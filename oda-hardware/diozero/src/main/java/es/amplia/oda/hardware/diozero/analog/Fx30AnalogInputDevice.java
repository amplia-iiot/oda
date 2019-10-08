package es.amplia.oda.hardware.diozero.analog;

import com.diozero.api.AnalogInputEvent;
import com.diozero.internal.provider.AbstractInputDevice;
import com.diozero.internal.provider.AnalogInputDeviceInterface;
import com.diozero.util.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Fx30AnalogInputDevice extends AbstractInputDevice<AnalogInputEvent> implements AnalogInputDeviceInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(Fx30AnalogInputDevice.class);

	private int adcNumber;
	private float vRef;
	private int pinNumber;
	private String name;
	private boolean activeLow;
	private String path;
	private RandomAccessFile value;

	Fx30AnalogInputDevice(Fx30AnalogInputDeviceFactory fx30AnalogInputDeviceFactory, String key, int device, int adcNumber, String path, float vRef) {
		super(key, fx30AnalogInputDeviceFactory);

		this.adcNumber = adcNumber;
		this.vRef = vRef;
		this.pinNumber = device;
		this.name = key;
		this.activeLow = fx30AnalogInputDeviceFactory.getVRef() == 5f;
		this.path = path;

		Path devicePath = FileSystems.getDefault().getPath(path + device);
		try {
			value = new RandomAccessFile(devicePath.toFile(), "r");
		} catch (IOException e) {
			throw new RuntimeIOException("Error opening sysfs analog input files for ADC " + adcNumber, e);
		}
	}

	@Override
	protected void closeDevice() {
		try {
			value.close();
		} catch (IOException e) {
			LOGGER.error("Error trying to close ADC file");
		}
	}

	@Override
	public float getValue() throws RuntimeIOException {
		try {
			value.seek(7);
			float val = Float.parseFloat(value.readLine().split(" ")[0]);
			return val / vRef / 1000000f;
		} catch (IOException e) {
			LOGGER.error("Error trying to read ADC file {}", e.getMessage());
			throw new RuntimeIOException("Error reading analog input files: " + e, e);
		}
	}

	@Override
	public int getAdcNumber() {
		return adcNumber;
	}

	public float getvRef() {
		return vRef;
	}

	public int getPinNumber() {
		return pinNumber;
	}

	public String getName() {
		return name;
	}

	public boolean isActiveLow() {
		return activeLow;
	}

	public String getPath() {
		return path;
	}
}
