package es.amplia.oda.hardware.jdkdio.gpio;

import es.amplia.oda.core.commons.gpio.GpioPinListener;

import jdk.dio.gpio.PinEvent;
import jdk.dio.gpio.PinListener;

class JdkDioGpioPinListenerBridge implements PinListener {

    private final GpioPinListener gpioPinListener;
    private final boolean activeLow;

    JdkDioGpioPinListenerBridge(GpioPinListener gpioPinListener, boolean activeLow) {
        this.gpioPinListener = gpioPinListener;
        this.activeLow = activeLow;
    }

    @Override
    public void valueChanged(PinEvent pinEvent) {
        gpioPinListener.pinValueChanged(pinEvent.getValue() ^ activeLow);
    }
}
