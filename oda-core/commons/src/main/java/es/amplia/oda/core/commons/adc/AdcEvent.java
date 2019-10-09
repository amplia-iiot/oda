package es.amplia.oda.core.commons.adc;

public interface AdcEvent {
	int getGpio();
	long getEpochTime();
	float getRange();
	void setRange(float range);
	float getScaledValue();
	float getUnscaledValue();
}
