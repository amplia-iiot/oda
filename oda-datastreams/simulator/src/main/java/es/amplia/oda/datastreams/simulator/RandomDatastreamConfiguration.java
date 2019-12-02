package es.amplia.oda.datastreams.simulator;

import lombok.Value;

@Value
class RandomDatastreamConfiguration implements SimulatedDatastreamsConfiguration {
    private String datastreamId;
    private String deviceId;
    private double minValue;
    private double maxValue;
    private double maxDifferenceBetweenMeasurements;
}
