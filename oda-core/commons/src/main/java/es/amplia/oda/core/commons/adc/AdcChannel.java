package es.amplia.oda.core.commons.adc;

public interface AdcChannel {

	/**
	 * Get the index of the device
	 * @return integer of the channel index
	 */
	int getIndex();

	/**
	 * Get the pin of the device
	 * @return integer of the channel pin
	 */
	int getPin();

	/**
	 * Get the name of the device
	 * @return name of the device
	 */
	String getName();

	/**
	 * Get the range of the device.
	 * Scaled values are the raw data recollected from device multiply by scale
	 * @return range of the device
	 */
	float getRange();

	/**
	 * Get the actual scaled value of the device.
	 * Are the same that raw data multiplied by scale
	 * @return scaled value of the device in the moment of execution
	 */
	float getScaledValue();

	/**
	 * Get the actual raw value of the device
	 * @return raw value of the device in the moment of execution
	 */
	float getUnscaledValue();

	/**
	 * Add a listener to see the events of the device
	 * @param listener {@link AdcChannelListener} listener that will watch events in the device
	 * @see AdcChannelListener
	 */
	void addAdcPinListener(AdcChannelListener listener);

	/**
	 * Remove all registered {@link AdcChannelListener} in the device.
	 * They will stop to watch the events
	 */
	void removeAllAdcPinListener();

	/**
	 * Close the object and stop watching data of the device
	 */
	void close();
}
