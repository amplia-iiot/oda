package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.EventPublisher;

class GpioDatastreamsFactoryImpl implements GpioDatastreamsFactory {

    private final GpioService gpioService;
    private final EventPublisher eventPublisher;

    GpioDatastreamsFactoryImpl(GpioService gpioService, EventPublisher eventPublisher) {
        this.gpioService = gpioService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public DatastreamsGetter createGpioDatastreamsGetter(String datastreamId, int pinIndex) {
        return new GpioDatastreamsGetter(datastreamId, pinIndex, gpioService);
    }

    @Override
    public DatastreamsSetter createGpioDatastreamsSetter(String datastreamId, int pinIndex) {
        return new GpioDatastreamsSetter(datastreamId, pinIndex, gpioService);
    }

    @Override
    public DatastreamsEvent createGpioDatastreamsEvent(String datastreamId, int pinIndex) {
        return new GpioDatastreamsEvent(eventPublisher, datastreamId, pinIndex, gpioService);
    }
}
