package es.amplia.oda.core.commons.adc;

public interface AdcEvent {
	/**
	 * Get the actual gpio pin associated to the AdcEvent
	 * @return integer of the pin associated to the AdcEvent
	 */
	int getGpio();

	/**
	 * Get the actual time in epoch format
	 * @return long of the epoch time
	 */
	long getEpochTime();

	/**
	 * Get the range of the event. Used to give the ScaledValue
	 * @return float of the scale that represents the range
	 * @see #getScaledValue
	 */
	float getRange();

	/**
	 * Set the range of the event. Used to give the ScaledValue
	 * @param range float of the scale that represents the range
	 * @see #getScaledValue
	 */
	void setRange(float range);

	/**
	 * Get the value from the device and put it on the specified range
	 * @return float of the scaled value collected from device
	 */
	float getScaledValue();

	/**
	 * Get the value from the device and returns it as is
	 * @return Value recollected from device
	 */
	float getUnscaledValue();
}
