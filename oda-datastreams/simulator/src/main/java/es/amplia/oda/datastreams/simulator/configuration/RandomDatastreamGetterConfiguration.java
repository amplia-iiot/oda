package es.amplia.oda.datastreams.simulator.configuration;

import lombok.Value;

@Value
public class RandomDatastreamGetterConfiguration implements SimulatedDatastreamsGetterConfiguration {
    private String datastreamId;
    private String deviceId;
    private String feed;
    private double minValue;
    private double maxValue;
    private double maxDifferenceBetweenMeasurements;
}
