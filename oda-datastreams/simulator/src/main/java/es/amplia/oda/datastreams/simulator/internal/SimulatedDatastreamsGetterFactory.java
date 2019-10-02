package es.amplia.oda.datastreams.simulator.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

public class SimulatedDatastreamsGetterFactory {

    public DatastreamsGetter createSimulatedDatastreamsGetter(String datastreamId, String deviceId, double minValue,
                                                              double maxValue, double maxDifferenceBetweenMeasurements) {
        return new SimulatedDatastreamsGetter(datastreamId, deviceId, minValue, maxValue,
                maxDifferenceBetweenMeasurements);
    }
}
