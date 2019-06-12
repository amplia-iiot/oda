package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

class GpioDatastreamsEvent implements DatastreamsEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(GpioDatastreamsEvent.class);


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

    @Override
    public void registerToEventSource() {
        try {
            pin = gpioService.getPinByIndex(pinIndex);

            if (!pin.isOpen()) {
                pin.open();
            }

            pin.addGpioPinListener(this::publishEvent);
        } catch (GpioDeviceException gpioDeviceException) {
            LOGGER.warn("Error initializing datastreams {}: Exception adding listener to GPIO pin {}. {}", datastreamId,
                    pinIndex, gpioDeviceException);
        }
    }

    void publishEvent(boolean value) {
        publish("", datastreamId, Collections.emptyList(), System.currentTimeMillis(), value);
    }

    @Override
    public void publish(String deviceId, String datastreamId, List<String> path, Long at, Object value) {
        Event gpioEvent = new Event(datastreamId, deviceId, path.toArray(new String[0]), at, value);
        eventDispatcher.publish(gpioEvent);
    }

    @Override
    public void unregisterFromEventSource() {
        try {
            pin.removeGpioPinListener();
        } catch (GpioDeviceException gpioDeviceException) {
            LOGGER.warn("Error releasing datastreams {}: Exception removing listener from GPIO pin {}. {}",
                    datastreamId, pinIndex, gpioDeviceException.getMessage());
        } catch (Exception exception) {
            LOGGER.warn("Error releasing datastreams event {}: GPIO pin is not available", datastreamId);
        }
    }
}
