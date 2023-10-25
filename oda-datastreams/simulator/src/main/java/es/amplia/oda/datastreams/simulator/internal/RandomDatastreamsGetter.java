package es.amplia.oda.datastreams.simulator.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

class RandomDatastreamsGetter implements DatastreamsGetter {

    private final String datastreamId;
    private final String deviceId;
    private final String feed;
    private final double minValue;
    private final double maxValue;
    private final double maxDifference;

    private double lastValue;

    RandomDatastreamsGetter(String datastreamId, String deviceId, String feed, double minValue, double maxValue,
                            double maxDifferenceBetweenMeasurements) {
        this.datastreamId = datastreamId;
        this.deviceId = deviceId;
        this.feed = feed;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.maxDifference = maxDifferenceBetweenMeasurements / 100.0 * (maxValue - minValue);
        this.lastValue = randomBetween(minValue, maxValue);
    }

    private double randomBetween(double origin, double bound) {
        return ThreadLocalRandom.current().nextDouble(origin, bound);
    }

    @Override
    public String getDatastreamIdSatisfied() {
        return datastreamId;
    }

    @Override
    public List<String> getDevicesIdManaged() {
        return Collections.singletonList(deviceId);
    }

    @Override
    public CompletableFuture<CollectedValue> get(String device) {
        lastValue = getNextValue();
        double lastValueWithTwoDecimals = Double.parseDouble(String.format(Locale.ENGLISH, "%.2f", lastValue));
        return CompletableFuture.completedFuture(new CollectedValue(System.currentTimeMillis(), lastValueWithTwoDecimals, null, feed));
    }

    private double getNextValue() {
        double origin = lastValue - minValue < maxDifference ? minValue : lastValue - maxDifference;
        double bound = maxValue - lastValue < maxDifference ? maxValue : lastValue + maxDifference;
        return randomBetween(origin, bound);
    }
}
