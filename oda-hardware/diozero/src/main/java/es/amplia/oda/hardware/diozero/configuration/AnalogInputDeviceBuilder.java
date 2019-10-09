package es.amplia.oda.hardware.diozero.configuration;

import es.amplia.oda.core.commons.adc.AdcDeviceException;
import es.amplia.oda.core.commons.adc.DeviceType;
import es.amplia.oda.hardware.diozero.analog.Fx30AnalogInputDeviceFactory;

import com.diozero.api.AnalogInputDevice;
import com.diozero.internal.provider.AnalogInputDeviceFactoryInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AnalogInputDeviceBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnalogInputDeviceBuilder.class);

	static final boolean DEFAULT_LOW_MODE = false;
	private static final DeviceType DEFAULT_DEVICE = DeviceType.DEFAULT;


	private int channelIndex = -1;
	private String name = null;
	private String path;
	private boolean lowMode = DEFAULT_LOW_MODE;
	private DeviceType deviceType = DEFAULT_DEVICE;


	static AnalogInputDeviceBuilder newBuilder() {
		return new AnalogInputDeviceBuilder();
	}

	private AnalogInputDeviceBuilder() {}

	void setChannelIndex(int channelIndex) {
		this.channelIndex = channelIndex;
	}

	void setName(String name) {
		this.name = name;
	}

	void setPath(String path) {
		this.path = path;
	}

	void setLowMode(boolean lowMode) {
		this.lowMode = lowMode;
	}

	void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	AnalogInputDevice build() {
		checkRequiredParameters();
		checkCompatibleParameters();

		LOGGER.info("Build new ADC Channel({},{},{},{},{})", channelIndex, name, path, lowMode, deviceType);
		switch (deviceType) {
			case FX30:
				AnalogInputDeviceFactoryInterface factory = new Fx30AnalogInputDeviceFactory(name, path, lowMode, deviceType);
				return new AnalogInputDevice(factory, channelIndex);
			case DEFAULT:
				return new AnalogInputDevice(channelIndex);
			default:
				return null;
		}
	}

	private void checkRequiredParameters() {
		if (channelIndex == -1 || path == null) {
			throw new AdcDeviceException("Invalid parameters to build ADC Channel: index and path are required properties");
		}
	}

	private void checkCompatibleParameters() {
		if (deviceType.equals(DeviceType.FX30) && channelIndex != 0 && channelIndex != 1) {
				throw new AdcDeviceException("Invalid parameters to build ADC Channel: Only Channel 0 and Channel 1 of FX30" +
						"are ADC inputs. Change the channel index");
		}
	}
}
