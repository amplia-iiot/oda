package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;
import es.amplia.oda.event.api.Event;

interface EventParser {
    OutputDatastream parse(Event event);
}
