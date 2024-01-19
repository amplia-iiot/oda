package es.amplia.oda.dispatcher.opengate.event;

import es.amplia.oda.core.commons.utils.Event;
import es.amplia.oda.dispatcher.opengate.datastreamdomain.OutputDatastream;

import java.util.List;

interface EventParser {
    List<OutputDatastream> parse(List<Event> event);
}
