package es.amplia.oda.hardware.jdkdio.gpio;

import es.amplia.oda.core.commons.gpio.GpioDirection;
import es.amplia.oda.core.commons.gpio.GpioMode;
import es.amplia.oda.core.commons.gpio.GpioTrigger;

import jdk.dio.InvalidDeviceConfigException;
import jdk.dio.gpio.GPIOPinConfig;

final class JdkGpioMapper {

    // Hide public default constructor.
    private JdkGpioMapper() {}

    static int mapGpioDirectionToJdkGpioDirection(GpioDirection direction) throws InvalidDeviceConfigException {
        switch (direction) {
            case INPUT:
                return GPIOPinConfig.DIR_INPUT_ONLY;
            case OUTPUT:
                return GPIOPinConfig.DIR_OUTPUT_ONLY;
            default:
                throw new InvalidDeviceConfigException("Invalid GPIO direction");
        }
    }

    static int mapGpioModeToJdkGpioMode(GpioMode mode) throws InvalidDeviceConfigException {
        switch (mode) {
            case PULL_DOWN:
                return GPIOPinConfig.MODE_INPUT_PULL_DOWN;
            case PULL_UP:
                return GPIOPinConfig.MODE_INPUT_PULL_UP;
            case OPEN_DRAIN:
                return GPIOPinConfig.MODE_OUTPUT_OPEN_DRAIN;
            case PUSH_PULL:
                return GPIOPinConfig.MODE_OUTPUT_PUSH_PULL;
            default:
                throw new InvalidDeviceConfigException("Invalid GPIO mode");
        }
    }

    static int mapGpioTriggerToJdkGpioTrigger(GpioTrigger trigger, boolean activeLow)
            throws InvalidDeviceConfigException {
        switch (trigger) {
            case NONE:
                return GPIOPinConfig.TRIGGER_NONE;
            case FALLING_EDGE:
                return !activeLow ? GPIOPinConfig.TRIGGER_FALLING_EDGE : GPIOPinConfig.TRIGGER_RISING_EDGE;
            case RISING_EDGE:
                return !activeLow ? GPIOPinConfig.TRIGGER_RISING_EDGE : GPIOPinConfig.TRIGGER_FALLING_EDGE;
            case BOTH_EDGES:
                return GPIOPinConfig.TRIGGER_BOTH_EDGES;
            case LOW_LEVEL:
                return !activeLow ? GPIOPinConfig.TRIGGER_LOW_LEVEL : GPIOPinConfig.TRIGGER_HIGH_LEVEL;
            case HIGH_LEVEL:
                return !activeLow ? GPIOPinConfig.TRIGGER_HIGH_LEVEL : GPIOPinConfig.TRIGGER_LOW_LEVEL;
            case BOTH_LEVELS:
                return GPIOPinConfig.TRIGGER_BOTH_LEVELS;
            default:
                throw new InvalidDeviceConfigException("Invalid GPIO trigger");
        }
    }
}
