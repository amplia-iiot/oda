package es.amplia.oda.datastreams.simulator.configuration;

import lombok.Value;

@Value
public class SetDatastreamSetterConfiguration implements SimulatedDatastreamsGetterConfiguration {
    String datastreamId;
    String deviceId;
}
