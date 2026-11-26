package es.amplia.oda.hardware.diozero.gpio;

import es.amplia.oda.core.commons.gpio.*;

import com.diozero.api.DigitalInputDevice;
import com.diozero.api.DigitalOutputDevice;
import com.diozero.util.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DioZeroGpioPin implements GpioPin {

    private static final Logger LOGGER = LoggerFactory.getLogger(DioZeroGpioPin.class);

    private final int index;
    private final String name;
    private final GpioDirection direction;
    private final GpioMode mode;
    private final GpioTrigger trigger;
    private final boolean activeLow;
    private final boolean initialValue;

    // diozero devices open the pin on construction. Only one of them is set,
    // depending on the pin direction. The active low inversion is applied here,
    // at the ODA layer, so the devices are always driven/read in physical terms.
    private DigitalInputDevice inputDevice;
    private DigitalOutputDevice outputDevice;

    public DioZeroGpioPin(int index, String name, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
                          boolean activeLow, boolean initialValue) {
        this.index = index;
        this.name = name;
        this.direction = direction;
        this.mode = mode;
        this.trigger = trigger;
        this.activeLow = activeLow;
        this.initialValue = initialValue;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getName() {
        return name != null ? name : String.valueOf(index);
    }

    @Override
    public GpioDirection getDirection() {
        return direction;
    }

    @Override
    public GpioMode getMode() {
        return mode;
    }

    @Override
    public GpioTrigger getTrigger() {
        return trigger;
    }

    @Override
    public boolean isActiveLow() {
        return activeLow;
    }

    @Override
    public boolean getInitialValue() {
        return initialValue;
    }

    @Override
    public boolean isOpen() {
        return inputDevice != null || outputDevice != null;
    }

    @Override
    public void open() {
        LOGGER.info("Opening GPIO pin {}", index);
        try {
            if (direction == GpioDirection.OUTPUT) {
                // activeHigh = true so setOn/isOn map directly to the physical pin level;
                // the physical initial level is the logical initial value adjusted by active low.
                outputDevice = new DigitalOutputDevice(index, true, initialValue ^ activeLow);
            } else {
                inputDevice = new DigitalInputDevice(index,
                        DioZeroGpioMapper.mapGpioModeToDioZeroPullUpDown(mode),
                        DioZeroGpioMapper.mapGpioTriggerToDioZeroEventTrigger(trigger, activeLow));
            }
            LOGGER.info("GPIO pin {} opened", index);
        } catch (RuntimeIOException exception) {
            throw new GpioDeviceException("Unable to open GPIO pin " + getName() + ": " + exception, exception);
        }
    }

    @Override
    public void close() {
        LOGGER.info("Closing GPIO pin {}", index);
        try {
            if (inputDevice != null) {
                inputDevice.close();
                inputDevice = null;
            }
            if (outputDevice != null) {
                outputDevice.close();
                outputDevice = null;
            }
        } catch (RuntimeIOException exception) {
            throw new GpioDeviceException("Unable to close GPIO pin " + getName() + ": " + exception, exception);
        }
    }

    @Override
    public boolean getValue() {
        checkIsOpen("get GPIO pin value");
        try {
            boolean physicalValue = inputDevice != null ? inputDevice.getValue() : outputDevice.isOn();
            boolean result = physicalValue ^ activeLow;
            LOGGER.debug("Getting value {} from GPIO pin {}", result, index);
            return result;
        } catch (RuntimeIOException e) {
            throw new GpioDeviceException("Unable to get GPIO pin value: " + e, e);
        }
    }

    @Override
    public void setValue(boolean value) {
        checkIsOpen("set GPIO pin value");
        if (outputDevice == null) {
            throw new GpioDeviceException("Unable to set GPIO pin value: pin " + getName() + " is not an output pin");
        }
        try {
            outputDevice.setOn(value ^ activeLow);
            LOGGER.info("Set value {} to GPIO pin {}", value, index);
        } catch (RuntimeIOException e) {
            throw new GpioDeviceException("Unable to set GPIO pin value: " + e, e);
        }
    }

    @Override
    public void addGpioPinListener(GpioPinListener listener) {
        checkIsOpen("add GPIO pin listener");
        if (inputDevice == null) {
            throw new GpioDeviceException("Unable to add listener: GPIO pin " + getName() + " is not an input pin");
        }
        // The contract allows a single listener per pin, so drop any previous one first.
        inputDevice.removeAllListeners();
        inputDevice.addListener(new DioZeroGpioPinListenerBridge(listener, activeLow));
    }

    @Override
    public void removeGpioPinListener() {
        checkIsOpen("remove GPIO pin listener");
        if (inputDevice != null) {
            inputDevice.removeAllListeners();
        }
    }

    private void checkIsOpen(String operationDescription) {
        if (!isOpen()) {
            throw new GpioDeviceException("Unable to " + operationDescription + ": GPIO pin is closed");
        }
    }
}
