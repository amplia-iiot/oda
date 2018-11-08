package es.amplia.oda.core.commons.gpio;

/**
 * Interface used to be notified when GPIO input pin status changes.
 */
public interface GpioPinListener {
    /**
     * Invoke when the status of the attached GPIO input pin changes.
     * @param value New value of the GPIO input pin.
     */
    void pinValueChanged(boolean value);
}
