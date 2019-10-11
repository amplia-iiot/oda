package es.amplia.oda.datastreams.adc;

import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

public interface DatastreamsFactory {
    DatastreamsGetter createAdcDatastreamsGetter(String datastreamId, int pinIndex);
    DatastreamsEvent createAdcDatastreamsEvent(String datastreamId, int pinIndex);
}
