package es.amplia.oda.dispatcher.scada;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.ScadaConnector;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.DatastreamInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ScadaEventDispatcher implements EventDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScadaEventDispatcher.class);

    private final ScadaTableTranslator translator;
    private final ScadaConnector connector;

    ScadaEventDispatcher(ScadaTableTranslator translator, ScadaConnector connector) {
        this.translator = translator;
        this.connector = connector;
    }

    @Override
    public void publish(Event event) {
        try {
            DatastreamInfo datastreamInfo =
                    new DatastreamInfo(event.getDeviceId(), event.getDatastreamId(), event.getValue());
        	ScadaInfo info = translator.translate(datastreamInfo);
            connector.uplink(info.getIndex(), info.getValue(), info.getType(), event.getAt());
        } catch (DataNotFoundException exception) {
            LOGGER.warn("Can not publish event {}: SCADA index not found", event);
        }
    }
}
