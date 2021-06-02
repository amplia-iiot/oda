package es.amplia.oda.datastreams.simulator.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

public class SimulatedDatastreamsSetterFactory {
    public DatastreamsSetter createSetDatastreamsSetter(String datastreamId, String deviceId) {
        return new SetDatastreamSetter(datastreamId, deviceId);
    }
}
