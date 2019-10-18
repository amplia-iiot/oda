package es.amplia.oda.core.commons.adc;

public interface AdcChannel {

	int getIndex();

	int getPin();

	String getName();

	float getRange();

	float getScaledValue();

	float getUnscaledValue();

	void addAdcPinListener(AdcChannelListener listener);

	void removeAllAdcPinListener();

	void close();
}
