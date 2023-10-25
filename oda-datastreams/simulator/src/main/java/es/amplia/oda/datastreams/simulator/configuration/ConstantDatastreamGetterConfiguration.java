package es.amplia.oda.datastreams.simulator.configuration;

import lombok.Value;

@Value
public class ConstantDatastreamGetterConfiguration implements SimulatedDatastreamsGetterConfiguration {
    private String datastreamId;
    private String deviceId;
    private String feed;
    private Object value;
}
