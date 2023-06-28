package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.interfaces.SerializerProvider;
import es.amplia.oda.core.commons.utils.Scheduler;
import es.amplia.oda.dispatcher.opengate.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EventDispatcherFactoryImpl implements EventDispatcherFactory {

    private final DeviceInfoProvider deviceInfoProvider;
    private final SerializerProvider serializerProvider;
    private final OpenGateConnector connector;
    private final Scheduler scheduler;

    private final Map<Boolean, Function<DeviceInfoProvider, EventParser>> eventParserCreators = new HashMap<>();


    public EventDispatcherFactoryImpl(DeviceInfoProvider deviceInfoProvider, SerializerProvider serializerProvider,
                                      OpenGateConnector connector, Scheduler scheduler) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.serializerProvider = serializerProvider;
        this.connector = connector;
        this.scheduler = scheduler;
        populateEventParserCreators();
    }

    private void populateEventParserCreators() {
        eventParserCreators.put(false, EventDispatcherFactoryImpl::createCompleteEventParser);
        eventParserCreators.put(true, EventDispatcherFactoryImpl::createReducedOutputEventParser);
    }

    private static EventParser createCompleteEventParser(DeviceInfoProvider deviceInfoProvider) {
        return new EventParserImpl(deviceInfoProvider);
    }

    private static EventParser createReducedOutputEventParser(DeviceInfoProvider deviceInfoProvider) {
        return new EventParserReducedOutputImpl(deviceInfoProvider);
    }

    @Override
    public EventCollector createEventCollector(boolean reducedOutput, ContentType contentType) {
        EventParser eventParser = eventParserCreators.get(reducedOutput).apply(deviceInfoProvider);
        Serializer serializer = serializerProvider.getSerializer(contentType);
        EventDispatcherImpl internalEventDispatcher =
                new EventDispatcherImpl(eventParser, serializer, contentType, connector, scheduler);
        return new EventCollectorImpl(internalEventDispatcher);
    }
}
