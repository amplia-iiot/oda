package es.amplia.oda.dispatcher.scada;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.event.api.EventDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

class ScadaEventDispatcher implements EventDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScadaEventDispatcher.class);

    private final ScadaTableTranslator translator;
    private final ScadaConnector connector;

    ScadaEventDispatcher(ScadaTableTranslator translator, ScadaConnector connector) {
        this.translator = translator;
        this.connector = connector;
    }

    @Override
    public void publish(List<Event> events) {
        for (Event event : events) {
            try {
                LOGGER.info("Publishing events {}", event);
                DatastreamInfo datastreamInfo = new DatastreamInfo(event.getDeviceId(), event.getDatastreamId());
                // for the moment only check values that are not catalogued as events in scada tables
                boolean isEvent = false;
                ScadaInfo info = translator.translate(datastreamInfo, isEvent);
                connector.uplink(info.getIndex(), event.getValue(), info.getType(), event.getAt());
            } catch (DataNotFoundException exception) {
                LOGGER.warn("Can not publish event {}: SCADA index not found", event);
            }
        }
    }

    @Override
    public void publishImmediately(List<Event> events) {
        publish(events);
    }

    @Override
    public void publishSameThread(List<Event> events, boolean useQos) {
        publish(events);
    }
}
