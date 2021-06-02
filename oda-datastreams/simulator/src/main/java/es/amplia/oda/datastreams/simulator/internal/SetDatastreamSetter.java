package es.amplia.oda.datastreams.simulator.internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SetDatastreamSetter implements DatastreamsSetter {

    private final String datastreamId;
    private final String deviceId;
    private Object value;

    SetDatastreamSetter(String datastreamId, String deviceId) {
        this.datastreamId = datastreamId;
        this.deviceId = deviceId;
    }

    @Override
    public String getDatastreamIdSatisfied() {
        return datastreamId;
    }

    @Override
    public Type getDatastreamType() {
        return null;
    }

    @Override
    public List<String> getDevicesIdManaged() {
        return Collections.singletonList(deviceId);
    }

    @Override
    public CompletableFuture<Void> set(String device, Object value) {
        return CompletableFuture.runAsync(() -> {
            this.value = value;
        });
    }
}
