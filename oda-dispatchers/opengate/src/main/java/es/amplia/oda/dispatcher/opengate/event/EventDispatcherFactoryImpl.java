package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.OpenGateConnector;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EventDispatcherFactoryImpl implements EventDispatcherFactory {

    private static final Map<Boolean, Function<DeviceInfoProvider, EventParser>> eventParserCreators = new HashMap<>();

    static {
        eventParserCreators.put(false, EventDispatcherFactoryImpl::createCompleteEventParser);
        eventParserCreators.put(true, EventDispatcherFactoryImpl::createReducedOutputEventParser);
    }

    private static EventParser createCompleteEventParser(DeviceInfoProvider deviceInfoProvider) {
        return new EventParserImpl(deviceInfoProvider);
    }

    private static EventParser createReducedOutputEventParser(DeviceInfoProvider deviceInfoProvider) {
        return new EventParserReducedOutputImpl(deviceInfoProvider);
    }


    private final DeviceInfoProvider deviceInfoProvider;
    private final Serializer serializer;
    private final OpenGateConnector connector;


    public EventDispatcherFactoryImpl(DeviceInfoProvider deviceInfoProvider, Serializer serializer,
                                      OpenGateConnector connector) {
        this.deviceInfoProvider = deviceInfoProvider;
        this.serializer = serializer;
        this.connector = connector;
    }

    @Override
    public EventCollector createEventCollector(boolean reducedOutput) {
        EventParser eventParser = eventParserCreators.get(reducedOutput).apply(deviceInfoProvider);
        EventDispatcherImpl internalEventDispatcher = new EventDispatcherImpl(eventParser, serializer, connector);
        return new EventCollectorImpl(internalEventDispatcher);
    }
}
