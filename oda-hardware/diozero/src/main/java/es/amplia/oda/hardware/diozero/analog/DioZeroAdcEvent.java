package es.amplia.oda.hardware.diozero.analog;

import com.diozero.api.AnalogInputEvent;
import es.amplia.oda.core.commons.diozero.AdcEvent;

public class DioZeroAdcEvent implements AdcEvent {

	private AnalogInputEvent event;

	DioZeroAdcEvent(AnalogInputEvent event) {
		this.event = event;
	}

	@Override
	public int getGpio() {
		return event.getGpio();
	}

	@Override
	public long getEpochTime() {
		return event.getEpochTime();
	}

	@Override
	public float getRange() {
		return event.getRange();
	}

	@Override
	public void setRange(float range) {
		event.setRange(range);
	}

	@Override
	public float getScaledValue() {
		return event.getScaledValue();
	}

	@Override
	public float getUnscaledValue() {
		return event.getUnscaledValue();
	}
}
