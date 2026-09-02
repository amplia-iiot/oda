package es.amplia.oda.hardware.diozero.gpio;

import es.amplia.oda.core.commons.gpio.GpioPinListener;

import com.diozero.api.DigitalInputEvent;
import com.diozero.api.InputEventListener;

class DioZeroGpioPinListenerBridge implements InputEventListener<DigitalInputEvent> {

    private final GpioPinListener gpioPinListener;
    private final boolean activeLow;

    DioZeroGpioPinListenerBridge(GpioPinListener gpioPinListener, boolean activeLow) {
        this.gpioPinListener = gpioPinListener;
        this.activeLow = activeLow;
    }

    @Override
    public void valueChanged(DigitalInputEvent event) {
        gpioPinListener.pinValueChanged(event.getValue() ^ activeLow);
    }
}
