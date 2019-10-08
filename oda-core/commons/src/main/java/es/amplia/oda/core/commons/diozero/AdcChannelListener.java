package es.amplia.oda.core.commons.diozero;

public interface AdcChannelListener {
	// TODO: Make documentation for this
	/**
	 * Invoke when the status of the attached ADC input channel changes.
	 * @param event New value of the ADC input channel.
	 */
	void channelValueChanged(AdcEvent event);
}
