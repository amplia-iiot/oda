package es.amplia.oda.core.commons.gpio;

import java.util.Map;

/**
 * API to use the GPIO service.
 */
public interface GpioService {
    /**
     * Get GPIO pin associated with the given name.
     * @param pinName GPIO pin name.
     * @return GPIO pin associated with the given name.
     * @throws GpioDeviceException Exception getting the GPIO pin with the given associated name.
     */
    GpioPin getPinByName(String pinName) throws GpioDeviceException;

    /**
     * Get GPIO pin associated with the given name and with the given configuration.
     * @param pinName GPIO pin name.
     * @param direction GPIO pin direction.
     * @param mode GPIO pin mode.
     * @param trigger GPIO pin trigger mode.
     * @param activeLow GPIO pin active low.
     * @param initialValue GPIO pin initial value.
     * @return GPIO pin associated with the given name and configuration
     * @throws GpioDeviceException Exception getting the GPIO pin with the given name and configuration.
     */
    GpioPin getPinByName(String pinName, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
                         boolean activeLow, boolean initialValue)
            throws GpioDeviceException;

    /**
     * Get the GPIO pin associated with the given pin's terminal index.
     * @param index Index of the GPIO pin's terminal.
     * @return GPIO pin associated with the given pin's terminal index.
     * @throws GpioDeviceException Exception getting the GPIO pin with the given pin's terminal index.
     */
    GpioPin getPinByIndex(int index) throws GpioDeviceException;

    /**
     * Get the GPIO pin associated with the given pin's terminal index and with the given configuration.
     * @param index Index of the GPIO pin's terminal.
     * @param direction GPIO pin direction.
     * @param mode GPIO pin mode.
     * @param trigger GPIO pin trigger mode.
     * @param activeLow GPIO pin active low.
     * @param initialValue GPIO pin initial value.
     * @return GPIO pin associated with the given pin's terminal index and configuration.
     */
    GpioPin getPinByIndex(int index, GpioDirection direction, GpioMode mode, GpioTrigger trigger,
                          boolean activeLow, boolean initialValue);

    /**
     * Get the available GPIO pins with the associated pin's terminal index.
     * @return The available GPIO pins with the associated pin's terminal index.
     */
    Map<Integer, GpioPin> getAvailablePins();
}
