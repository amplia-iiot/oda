package es.amplia.oda.hardware.diozero.analog.devices.fx30;

import com.diozero.api.AnalogInputEvent;
import com.diozero.internal.provider.AbstractInputDevice;
import com.diozero.internal.provider.AnalogInputDeviceInterface;
import com.diozero.util.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Fx30AnalogInputDevice extends AbstractInputDevice<AnalogInputEvent> implements AnalogInputDeviceInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(Fx30AnalogInputDevice.class);


	private final int adcNumber;
	private final float vRef;
	private final int pinNumber;
	private final String name;
	private final RandomAccessFile value;


	Fx30AnalogInputDevice(Fx30AnalogInputDeviceFactory fx30AnalogInputDeviceFactory, String key, int device,
						  int adcNumber, String path, float vRef) {
		super(key, fx30AnalogInputDeviceFactory);

		this.adcNumber = adcNumber;
		this.vRef = vRef;
		this.pinNumber = device;
		this.name = key;

		Path devicePath = FileSystems.getDefault().getPath(path + device);
		try {
			this.value = new RandomAccessFile(devicePath.toFile(), "r");
		} catch (FileNotFoundException e) {
			throw new RuntimeIOException("Error opening file " + devicePath + " for ADC " + adcNumber, e);
		}
	}

	@Override
	public float getValue() {
		try {
			value.seek(7);
			float val = Float.parseFloat(value.readLine().split(" ")[0]);
			LOGGER.debug("Recollected {} as raw value from channel {}", val, adcNumber);
			return val / vRef / 1000000f;
		} catch (IOException e) {
			LOGGER.error("Error trying to get ADC value from {} (pin number {}), returning 0 as value", name, pinNumber, e);
			return 0;
		}
	}

	@Override
	public int getAdcNumber() {
		return adcNumber;
	}

	@Override
	protected void closeDevice() {
		try {
			value.close();
		} catch (IOException e) {
			LOGGER.warn("Couldn't trying to close ADC file");
		}
	}
}
