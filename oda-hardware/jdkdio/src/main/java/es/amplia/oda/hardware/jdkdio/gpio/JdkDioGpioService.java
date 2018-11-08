package es.amplia.oda.hardware.jdkdio.gpio;

import es.amplia.oda.core.commons.gpio.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JdkDioGpioService implements GpioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdkDioGpioService.class);

    private final Map<Integer, GpioPin> pins = new HashMap<>();

    @Override
    public GpioPin getPinByName(String name) {
        return pins.values().stream()
                .filter(pin -> name.equals(pin.getName()))
                .findFirst()
                .orElseThrow(() -> new GpioDeviceException("No GPIO pin found with name " + name));
    }

    @Override
    public GpioPin getPinByName(String name, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
                                boolean activeLow, boolean initialValue) {
        GpioPin pin = getPinByName(name);
        pin = configurePin(pin, name, direction, mode, trigger, activeLow, initialValue);
        return pin;
    }

    private GpioPin configurePin(GpioPin pin, String name, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
                              boolean activeLow, boolean initialValue) {
        if (pin.isOpen()) {
            try {
                pin.close();
            } catch (GpioDeviceException e) {
                LOGGER.warn("Cannot close already configured pin with name " + name +
                        ". Returning pin with old configuration");
                return pin;
            }
        }

        if (!isPinConfigAs(pin, direction, mode, trigger, activeLow, initialValue)) {
            int index = pin.getIndex();
            GpioPin newPin = new JdkDioGpioPin(index, name, direction, mode, trigger, activeLow, initialValue);
            pins.put(index, newPin);
            return newPin;
        }

        return pin;
    }

    private boolean isPinConfigAs(GpioPin pin, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
                                  boolean activeLow, boolean initialValue) {
        return direction == pin.getDirection() && mode == pin.getMode() && trigger == pin.getTrigger()
                && activeLow == pin.isActiveLow() && initialValue == pin.getInitialValue();
    }

    @Override
    public GpioPin getPinByIndex(int index) {
        return tryGetPinByIndex(index)
                .orElseThrow(() -> new GpioDeviceException("No GPIO pin found with index " + index));
    }

    private Optional<GpioPin> tryGetPinByIndex(int index) {
        return Optional.ofNullable(pins.get(index));
    }

    @Override
    public GpioPin getPinByIndex(int index, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
                                 boolean activeLow, boolean initialValue) {
        GpioPin pin = tryGetPinByIndex(index)
                .orElseGet(() -> createNewPin(index, direction, mode, trigger, activeLow, initialValue));

        configurePin(pin, null, direction, mode, trigger, activeLow, initialValue);
        return pin;
    }

    private GpioPin createNewPin(int index, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
                                 boolean activeLow, boolean initialValue) {
        JdkDioGpioPin newPin = new JdkDioGpioPin(index, null, direction, mode, trigger, activeLow, initialValue);
        pins.put(index, newPin);
        return newPin;
    }

    @Override
    public Map<Integer, GpioPin> getAvailablePins() {
        return new HashMap<>(pins);
    }

    public void addConfiguredPin(JdkDioGpioPin pin) {
        pins.put(pin.getIndex(), pin);
        initValueIfNeeded(pin);
    }

    private void initValueIfNeeded(JdkDioGpioPin pin) {
        if (pin.getDirection() == GpioDirection.OUTPUT) {
            if (!pin.isOpen()) {
                pin.open();
            }
            pin.setValue(pin.getInitialValue());
        }
    }

    public void release() {
        for (GpioPin pin: pins.values()) {
            if (pin.isOpen()) {
                try {
                    pin.close();
                } catch (GpioDeviceException e) {
                    LOGGER.warn("Unable to release GPIO pin {}", pin.getName());
                }
            }
        }
        pins.clear();
    }
}
