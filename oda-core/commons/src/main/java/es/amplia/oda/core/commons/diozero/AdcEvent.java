package es.amplia.oda.core.commons.diozero;

public interface AdcEvent {
	// TODO: Make documentation for this
	/**
	 *
	 * @return
	 */
	int getGpio();

	/**
	 *
	 * @return
	 */
	long getEpochTime();

	/**
	 *
	 * @return
	 */
	float getRange();

	/**
	 *
	 * @param range
	 */
	void setRange(float range);

	/**
	 *
	 * @return
	 */
	float getScaledValue();

	/**
	 *
	 * @return
	 */
	float getUnscaledValue();
}
