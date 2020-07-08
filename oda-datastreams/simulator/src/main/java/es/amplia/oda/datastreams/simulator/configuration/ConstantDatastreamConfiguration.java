package es.amplia.oda.datastreams.simulator.configuration;

import lombok.Value;

@Value
public class ConstantDatastreamConfiguration implements SimulatedDatastreamsConfiguration {
    private String datastreamId;
    private String deviceId;
    private Object value;
}
