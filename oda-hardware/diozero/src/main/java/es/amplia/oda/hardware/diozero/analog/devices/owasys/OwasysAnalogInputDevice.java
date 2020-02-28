package es.amplia.oda.hardware.diozero.analog.devices.owasys;

import com.diozero.api.AnalogInputEvent;
import com.diozero.internal.provider.AbstractInputDevice;
import com.diozero.internal.provider.AnalogInputDeviceInterface;
import com.diozero.util.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class OwasysAnalogInputDevice extends AbstractInputDevice<AnalogInputEvent> implements AnalogInputDeviceInterface {

	private static final Logger LOGGER = LoggerFactory.getLogger(OwasysAnalogInputDevice.class);


	private final int adcNumber;
	private final String name;
	private final RandomAccessFile value;


	public OwasysAnalogInputDevice(OwasysAnalogInputDeviceFactory owasysAnalogInputDeviceFactory, String key,
							int adcNumber, String path) {
		super(key, owasysAnalogInputDeviceFactory);

		this.adcNumber = adcNumber;
		this.name = key;

		try {
			this.value = new RandomAccessFile(new File(path), "r");
		} catch (IOException e) {
			throw new RuntimeIOException("Error opening file " + path + " for ADC " + adcNumber, e);
		}
	}

	@Override
	public float getValue() {
		try {
			this.value.seek(0);
			float val = Float.parseFloat(value.readLine());
			return val / 3880f;
		} catch (IOException e) {
			LOGGER.error("Error trying to get ADC value from {} (pin number {})", name, adcNumber, e);
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
			LOGGER.error("Error trying to close ADC file");
		}
	}
}
