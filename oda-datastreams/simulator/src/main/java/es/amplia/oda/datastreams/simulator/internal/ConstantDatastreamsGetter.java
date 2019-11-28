package es.amplia.oda.datastreams.simulator.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class ConstantDatastreamsGetter implements DatastreamsGetter {

    private final String datastreamId;
    private final String deviceId;
    private final Object value;

    ConstantDatastreamsGetter(String datastreamId, String deviceId, Object value) {
        this.datastreamId = datastreamId;
        this.deviceId = deviceId;
        this.value = value;
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
        return CompletableFuture.completedFuture(new CollectedValue(System.currentTimeMillis(), value));
    }
}
