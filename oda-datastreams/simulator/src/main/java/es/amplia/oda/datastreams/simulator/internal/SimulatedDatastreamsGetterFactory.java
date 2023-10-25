package es.amplia.oda.datastreams.simulator.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

public class SimulatedDatastreamsGetterFactory {

    public DatastreamsGetter createConstantDatastreamsGetter(String datastreamId, String deviceId, String feed, Object value) {
        return new ConstantDatastreamsGetter(datastreamId, deviceId, feed, value);
    }

    public DatastreamsGetter createRandomDatastreamsGetter(String datastreamId, String deviceId, String feed,
                                                           double minValue, double maxValue,
                                                           double maxDifferenceBetweenMeasurements) {
        return new RandomDatastreamsGetter(datastreamId, deviceId, feed, minValue, maxValue,
                maxDifferenceBetweenMeasurements);
    }
}
