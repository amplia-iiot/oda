package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

interface I2CDatastreamsFactory {
    DatastreamsGetter createDatastreamsGetter(String name, long min, long max);
    DatastreamsSetter createDatastreamsSetter(String name);
}
