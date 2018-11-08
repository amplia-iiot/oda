package es.amplia.oda.hardware.jdkdio.gpio;

import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

import java.io.IOException;

class JdkDioGpioPinFactory {

    static GPIOPin createAndOpen(GPIOPinConfig pinConfig) throws IOException {
        return DeviceManager.open(GPIOPin.class, pinConfig);
    }
}
