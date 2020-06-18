package es.amplia.oda.hardware.jdkdio.configuration;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioDirection;
import es.amplia.oda.core.commons.gpio.GpioMode;
import es.amplia.oda.core.commons.gpio.GpioTrigger;

import es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioPin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JdkDioGpioPinBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdkDioGpioPinBuilder.class);

    static final GpioDirection DEFAULT_DIRECTION = GpioDirection.OUTPUT;
    static final GpioMode DEFAULT_MODE = GpioMode.OPEN_DRAIN;
    static final GpioTrigger DEFAULT_TRIGGER = GpioTrigger.NONE;
    static final boolean DEFAULT_ACTIVE_LOW = false;
    static final boolean DEFAULT_INITIAL_VALUE = false;

    // To check if index is configured
    private int index = -1;
    // Default name is the index
    private String name = null;
    private GpioDirection direction = DEFAULT_DIRECTION;
    private GpioMode mode = DEFAULT_MODE;
    private GpioTrigger trigger = DEFAULT_TRIGGER;
    private boolean activeLow = DEFAULT_ACTIVE_LOW;
    private boolean initialValue = DEFAULT_INITIAL_VALUE;

    static JdkDioGpioPinBuilder newBuilder() {
        return new JdkDioGpioPinBuilder();
    }

    // Private constructor to hide public default one
    private JdkDioGpioPinBuilder() {}

    void setIndex(int index) {
        this.index = index;
    }

    void setName(String name) {
        this.name = name;
    }

    void setDirection(GpioDirection direction) {
        this.direction = direction;
    }

    void setMode(GpioMode mode) {
        this.mode = mode;
    }

    void setTrigger(GpioTrigger trigger) {
        this.trigger = trigger;
    }

    void setActiveLow(boolean activeLow) {
        this.activeLow = activeLow;
    }

    void setInitialValue(boolean initialValue) {
        this.initialValue = initialValue;
    }

    JdkDioGpioPin build() {
        checkRequiredParameters();
        checkCompatibleParameters();

        LOGGER.info("Build new GPIO Pin(Idx: {}, Name: {}, Dir: {}, Md: {}, Tr: {}, low: {}, Init: {})",
                index, name, direction, mode, trigger, activeLow, initialValue);
        return new JdkDioGpioPin(index, name, direction, mode, trigger, activeLow, initialValue);
    }

    private void checkRequiredParameters() {
        if (index == -1) {
            throw new GpioDeviceException("Invalid parameters to build GPIO Pin: index is a required property");
        }
    }

    private void checkCompatibleParameters() {
        switch (direction) {
            case OUTPUT:
                if (mode == GpioMode.PULL_UP || mode == GpioMode.PULL_DOWN || trigger != GpioTrigger.NONE) {
                    throw new GpioDeviceException("Incompatible parameters");
                }
                break;
            case INPUT:
                if (mode == GpioMode.OPEN_DRAIN || mode == GpioMode.PUSH_PULL) {
                    throw new GpioDeviceException("Incompatible parameters");
                }
                break;
            default:
        }
    }
}
