package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class GpioDatastreamsEvent implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(GpioDatastreamsEvent.class);

    private final String datastreamId;
    private final int pinIndex;
    private final GpioService gpioService;
    private final EventDispatcher eventDispatcher;

    private GpioPin pin;


    GpioDatastreamsEvent(String datastreamId, int pinIndex, GpioService gpioService, EventDispatcher eventDispatcher) {
        this.datastreamId = datastreamId;
        this.pinIndex = pinIndex;
        this.gpioService = gpioService;
        this.eventDispatcher = eventDispatcher;
    }

    void init() {
        try {
            pin = gpioService.getPinByIndex(pinIndex);

            if (!pin.isOpen()) {
                pin.open();
            }

            pin.addGpioPinListener(this::publishEvent);
        } catch (GpioDeviceException gpioDeviceException) {
            logger.warn("Error initializing datastreams {}: Exception adding listener to GPIO pin {}. {}", datastreamId,
                    pinIndex, gpioDeviceException);
        }
    }

    void publishEvent(boolean value) {
        Event gpioEvent = new Event(datastreamId, "", null, System.currentTimeMillis(), value);
        eventDispatcher.publish(gpioEvent);
    }

    @Override
    public void close() {
        try {
            pin.removeGpioPinListener();
        } catch (GpioDeviceException gpioDeviceException) {
            logger.warn("Error releasing datastreams {}: Exception removing listener from GPIO pin {}. {}",
                    datastreamId, pinIndex, gpioDeviceException.getMessage());
        } catch (Exception exception) {
            logger.warn("Error releasing datastreams event {}: GPIO pin is not available", datastreamId);
        }
    }
}
