package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.gpio.GpioService;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.event.api.EventDispatcher;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GpioDatastreamsRegistry implements AutoCloseable {

    private static final int NUM_THREADS = 10;

    private final Executor executor = Executors.newFixedThreadPool(NUM_THREADS);

    private final BundleContext bundleContext;

    private final GpioService gpioService;

    private final EventDispatcher eventDispatcher;

    private final Map<String, GpioDatastreamsEvent> datastreamsEvents = new HashMap<>();

    private final List<ServiceRegistration<?>> datastreamsServiceRegistrations = new ArrayList<>();


    GpioDatastreamsRegistry(BundleContext bundleContext, GpioService gpioService,
                                   EventDispatcher eventDispatcher) {
        this.bundleContext = bundleContext;
        this.gpioService = gpioService;
        this.eventDispatcher = eventDispatcher;
    }

    public void addDatastreamGetter(int pinIndex, String datastreamId) {
        GpioDatastreamsGetter datastreamsGetter =
                GpioDatastreamsFactory.createGpioDatastreamsGetter(datastreamId, pinIndex, gpioService, executor);
        ServiceRegistration<DatastreamsGetter> registration =
                bundleContext.registerService(DatastreamsGetter.class, datastreamsGetter, null);
        datastreamsServiceRegistrations.add(registration);
    }

    public void addDatastreamSetter(int pinIndex, String datastreamId) {
        GpioDatastreamsSetter datastreamsSetter =
                GpioDatastreamsFactory.createGpioDatastreamsSetter(datastreamId, pinIndex, gpioService, executor);
        ServiceRegistration<DatastreamsSetter> registration =
                bundleContext.registerService(DatastreamsSetter.class, datastreamsSetter, null);
        datastreamsServiceRegistrations.add(registration);
    }

    public void addDatastreamEvent(int pinIndex, String datastreamId) {
        GpioDatastreamsEvent datastreamsEventSender;

        if (datastreamsEvents.containsKey(datastreamId)) {
            datastreamsEventSender = datastreamsEvents.get(datastreamId);
            datastreamsEventSender.unregisterFromEventSource();
        }

        datastreamsEventSender =
                GpioDatastreamsFactory.createGpioDatastreamsEvent(datastreamId, pinIndex, gpioService, eventDispatcher);
        datastreamsEventSender.registerToEventSource();
        datastreamsEvents.put(datastreamId, datastreamsEventSender);
    }

    public void close() {
        datastreamsServiceRegistrations.forEach(ServiceRegistration::unregister);
        datastreamsServiceRegistrations.clear();
        datastreamsEvents.values().forEach(GpioDatastreamsEvent::unregisterFromEventSource);
        datastreamsEvents.clear();
    }
}
