package es.amplia.oda.hardware.diozero.analog;

import com.diozero.api.AnalogInputEvent;
import com.diozero.api.InputEventListener;
import es.amplia.oda.core.commons.diozero.AdcChannelListener;

class DioZeroAdcPinListenerBridge implements InputEventListener<AnalogInputEvent> {

	private AdcChannelListener listener;

	DioZeroAdcPinListenerBridge(AdcChannelListener listener) {
		this.listener = listener;
	}

	@Override
	public void valueChanged(AnalogInputEvent event) {
		listener.channelValueChanged(new DioZeroAdcEvent(event));
	}
}
