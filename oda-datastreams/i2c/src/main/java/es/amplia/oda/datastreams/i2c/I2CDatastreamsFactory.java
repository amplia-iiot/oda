package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

public interface I2CDatastreamsFactory {
    DatastreamsGetter createDatastreamsGetter(String name, String device, long min, long max);
    DatastreamsSetter createDatastreamsSetter(String name);
}
