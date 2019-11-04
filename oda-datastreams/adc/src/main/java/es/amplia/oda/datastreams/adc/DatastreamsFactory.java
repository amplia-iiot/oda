package es.amplia.oda.datastreams.adc;

import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

public interface DatastreamsFactory {
    DatastreamsGetter createAdcDatastreamsGetter(String datastreamId, int pinIndex, double min, double max);
    DatastreamsEvent createAdcDatastreamsEvent(String datastreamId, int pinIndex, double min, double max);
}
