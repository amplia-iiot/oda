package es.amplia.oda.datastreams.gpio;

import es.amplia.oda.core.commons.interfaces.DatastreamsEvent;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

interface GpioDatastreamsFactory {
    DatastreamsGetter createGpioDatastreamsGetter(String datastreamId, int pinIndex);
    DatastreamsSetter createGpioDatastreamsSetter(String datastreamId, int pinIndex);
    DatastreamsEvent createGpioDatastreamsEvent(String datastreamId, int pinIndex);
}
