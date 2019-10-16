package es.amplia.oda.core.commons.gpio;

/**
 * API to interact with GPIO pins.
 */
public interface GpioPin {

    /**
     * Get the numeric index of the GPIO pin's terminal.
     * @return Numeric index of the GPIO pin's terminal.
     */
    int getIndex();

    /**
     * Get the name associated with the GPIO pin.
     * @return Name associated with the GPIO pin.
     */
    String getName();

    /**
     * Get the direction of the GPIO pin.
     * @return {@link GpioDirection} representing the direction of the GPIO pin.
     */
    GpioDirection getDirection();

    /**
     * Get the mode of the GPIO pin.
     * @return {@link GpioMode} representing the mode of the GPIO pin.
     */
    GpioMode getMode();

    /**
     * Get the trigger mode for the GPIO pin.
     * @return {@link GpioTrigger} representing the mode of the GPIO pin.
     */
    GpioTrigger getTrigger();

    /**
     * GPIO pin is active low.
     * @return GPIO pin is active low.
     */
    boolean isActiveLow();

    /**
     * Get the GPIO initial value.
     * @return GPIO initial value.
     */
    boolean getInitialValue();

    /**
     * Check if the GPIO pin is open.
     * @return True if the GPIO pin is open.
     */
    boolean isOpen();

    /**
     * Open the GPIO pin.
     * @throws GpioDeviceException Exception opening GPIO pin.
     */
    void open();

    /**
     * Close the GPIO pin.
     * If the GPIO pin is already close silently fails.
     *
     * @throws GpioDeviceException Exception closing the GPIO pin.
     */
    void close();

    /**
     * Get the GPIO pin value.
     * @return GPIO pin value.
     * @throws GpioDeviceException Exception getting the GPIO pin value.
     */
    boolean getValue();

    /**
     * Set the GPIO pin value.
     * @param value Value to set.
     * @throws GpioDeviceException Exception setting the GPIO pin value.
     */
    void setValue(boolean value);

    /**
     * Add a listener to the GPIO pin to be notified when the input changes.
     * Only one listener can be registered to the GPIO pin. Subsequent calls
     * to this method will unregister the previous listener.
     *
     * Attaching a listener to an output pin will raise an exception.
     *
     * @param listener GPIO pin listener represented by {@link GpioPinListener}
     * @throws GpioDeviceException Exception adding the listener.
     */
    void addGpioPinListener(GpioPinListener listener);

    /**
     * Remove the previously added GPIO pin listener.
     * @throws GpioDeviceException Exception removing the listener.
     */
    void removeGpioPinListener();
}
