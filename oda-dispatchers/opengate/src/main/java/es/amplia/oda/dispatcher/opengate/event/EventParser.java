package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;

import java.util.List;

interface EventParser {
    OutputDatastream parse(List<Event> event);
}
