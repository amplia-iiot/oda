package es.amplia.oda.core.commons.diozero;

import es.amplia.oda.core.commons.gpio.*;

public interface AdcChannel {
	// TODO: Make documentation for this

	/**
	 * Get the numeric index of the ADC channel's terminal.
	 * @return Numeric index of the ADC channel's terminal.
	 */
	int getIndex();

	int getPin();

	String getName();

	/**
	 * Close the ADC channel.
	 * If the ADC channel is already close silently fails.
	 *
	 * @throws GpioDeviceException Exception closing the ADC pin.
	 */
	void close() throws AdcDeviceException;

	/**
	 *
	 * @return
	 * @throws AdcDeviceException
	 */
	float getRange() throws AdcDeviceException;

	/**
	 * Get the ADC channel value.
	 * @return ADC channel value.
	 * @throws GpioDeviceException Exception getting the ADC pin value.
	 */
	float getScaledValue() throws AdcDeviceException;

	/**
	 * Get the ADC channel value.
	 * @return ADC channel value.
	 * @throws GpioDeviceException Exception getting the ADC pin value.
	 */
	float getUnscaledValue() throws AdcDeviceException;

	/**
	 * Add a listener to the ADC pin to be notified when the input changes.
	 * Only one listener can be registered to the ADC pin. Subsequent calls
	 * to this method will unregister the previous listener.
	 *
	 * Attaching a listener to an output pin will raise an exception.
	 *
	 * @param listener ADC pin listener represented by {@link AdcChannelListener}
	 * @throws GpioDeviceException Exception adding the listener.
	 */
	void addAdcPinListener(AdcChannelListener listener) throws AdcDeviceException;

	/**
	 * Remove added ADC pin listener.
	 * @throws GpioDeviceException Exception removing the listener.
	 */
	void removeAllAdcPinListener() throws AdcDeviceException;
}
