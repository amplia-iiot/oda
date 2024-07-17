package es.amplia.oda.datastreams.opcua;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

public interface OpcUaDatastreamsFactory {
    DatastreamsGetter createOpcUaDatastreamsGetter(String datastreamId);

    DatastreamsSetter createOpcUaDatastreamsSetter(String datastreamId);
}
