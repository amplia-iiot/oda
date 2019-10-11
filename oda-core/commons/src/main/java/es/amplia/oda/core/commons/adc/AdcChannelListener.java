package es.amplia.oda.core.commons.adc;

public interface AdcChannelListener {
	/**
	 * Invoke when the status of the attached ADC input channel changes.
	 * @param event New value of the ADC input channel.
	 */
	void channelValueChanged(AdcEvent event);
}
