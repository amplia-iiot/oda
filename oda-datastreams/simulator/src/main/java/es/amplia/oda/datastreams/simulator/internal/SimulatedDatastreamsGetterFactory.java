package es.amplia.oda.datastreams.simulator.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

public class SimulatedDatastreamsGetterFactory {

    public DatastreamsGetter createConstantDatastreamsGetter(String datastreamId, String deviceId, Object value) {
        return new ConstantDatastreamsGetter(datastreamId, deviceId, value);
    }

    public DatastreamsGetter createRandomDatastreamsGetter(String datastreamId, String deviceId, double minValue,
                                                           double maxValue, double maxDifferenceBetweenMeasurements) {
        return new RandomDatastreamsGetter(datastreamId, deviceId, minValue, maxValue,
                maxDifferenceBetweenMeasurements);
    }
}
