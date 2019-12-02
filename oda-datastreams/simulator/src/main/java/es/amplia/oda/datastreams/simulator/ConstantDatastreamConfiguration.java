package es.amplia.oda.datastreams.simulator;

import lombok.Value;

@Value
class ConstantDatastreamConfiguration implements SimulatedDatastreamsConfiguration {
    private String datastreamId;
    private String deviceId;
    private Object value;
}
