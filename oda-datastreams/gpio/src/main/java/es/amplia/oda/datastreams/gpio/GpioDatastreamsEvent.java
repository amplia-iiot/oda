package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioDeviceException;
import es.amplia.oda.core.commons.gpio.GpioPin;
import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.AbstractDatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.EventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

class GpioDatastreamsEvent extends AbstractDatastreamsEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(GpioDatastreamsEvent.class);


    private final String datastreamId;
    private final int pinIndex;
    private final GpioService gpioService;

    private GpioPin pin;


    GpioDatastreamsEvent(EventPublisher eventPublisher, String datastreamId, int pinIndex, GpioService gpioService) {
        super(eventPublisher);
        this.datastreamId = datastreamId;
        this.pinIndex = pinIndex;
        this.gpioService = gpioService;
        registerToEventSource();
    }

    @Override
    public void registerToEventSource() {
        try {
            pin = gpioService.getPinByIndex(pinIndex);

            if (!pin.isOpen()) {
                pin.open();
            }

            pin.addGpioPinListener(this::publishValue);
        } catch (GpioDeviceException gpioDeviceException) {
            LOGGER.warn("Error initializing datastream event {}: Exception adding listener to GPIO pin {}. {}", datastreamId,
                    pinIndex, gpioDeviceException.getMessage());
        }
    }

    void publishValue(boolean value) {
        Map<String, Map<String, Map<Long, Object>>> events = new HashMap<>();
        Map<String, Map<Long, Object>> event = new HashMap<>();
        Map<Long, Object> data = new HashMap<>();
        data.put(System.currentTimeMillis(), value);
        event.put(null, data);
        events.put(datastreamId, event);
        publish("", null, events);
    }

    @Override
    public void unregisterFromEventSource() {
        try {
            pin.removeGpioPinListener();
        } catch (GpioDeviceException gpioDeviceException) {
            LOGGER.warn("Error releasing datastream event {}: Exception removing listener from GPIO pin {}. {}",
                    datastreamId, pinIndex, gpioDeviceException.getMessage());
        } catch (Exception exception) {
            LOGGER.warn("Error releasing datastream event {}: GPIO pin is not available", datastreamId);
        }
    }
}
