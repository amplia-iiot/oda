package es.amplia.oda.core.commons.adc;

import es.amplia.oda.core.commons.gpio.*;

public interface AdcChannel {

	/**
	 * Get the numeric index of the ADC channel's terminal.
	 * @return Numeric index of the ADC channel's terminal.
	 */
	int getIndex();

	int getPin();

	String getName();

	float getRange();

	float getScaledValue();

	float getUnscaledValue();

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
	void addAdcPinListener(AdcChannelListener listener);

	/**
	 * Remove added ADC pin listener.
	 * @throws GpioDeviceException Exception removing the listener.
	 */
	void removeAllAdcPinListener();

	/**
	 * Close the ADC channel.
	 * If the ADC channel is already close silently fails.
	 *
	 * @throws GpioDeviceException Exception closing the ADC pin.
	 */
	void close();
}
