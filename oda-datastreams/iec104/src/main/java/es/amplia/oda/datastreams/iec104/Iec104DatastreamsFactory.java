package es.amplia.oda.datastreams.iec104;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

public interface Iec104DatastreamsFactory {
    DatastreamsGetter createIec104DatastreamsGetter(String datastreamId);

    DatastreamsSetter createIec104DatastreamsSetter(String datastreamId);

    void updateGetterPolling (int initialPolling, int polling);
}
