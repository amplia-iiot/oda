package es.amplia.oda.core.commons.osgi.proxies;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.core.commons.utils.Event;
import org.osgi.framework.BundleContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class StateManagerProxy implements StateManager {

    private final OsgiServiceProxy<StateManager> proxy;

    public StateManagerProxy(BundleContext bundleContext) {
        this.proxy = new OsgiServiceProxy<>(StateManager.class, bundleContext);
    }

    @Override
    public CompletableFuture<DatastreamValue> getDatastreamInformation(String deviceId, String datastreamId) {
        return proxy.callFirst(stateManager -> stateManager.getDatastreamInformation(deviceId, datastreamId));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getAllDatastreamsInformation(String deviceId, String datastreamId) {
        return proxy.callFirst(stateManager -> stateManager.getAllDatastreamsInformation(deviceId, datastreamId));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getAllDatastreamsInformationByAt(String deviceId, String datastreamId) {
        return proxy.callFirst(stateManager -> stateManager.getAllDatastreamsInformationByAt(deviceId, datastreamId));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(String deviceId, Set<String> datastreamIds) {
        return proxy.callFirst(stateManager -> stateManager.getDatastreamsInformation(deviceId, datastreamIds));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(DevicePattern devicePattern, String datastreamId) {
        return proxy.callFirst(stateManager -> stateManager.getDatastreamsInformation(devicePattern, datastreamId));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(DevicePattern devicePattern, Set<String> datastreamIds) {
        return proxy.callFirst(stateManager -> stateManager.getDatastreamsInformation(devicePattern, datastreamIds));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDeviceInformation(String deviceId) {
        return proxy.callFirst(stateManager -> stateManager.getDeviceInformation(deviceId));
    }

    @Override
    public CompletableFuture<DatastreamValue> setDatastreamValue(String deviceId, String datastreamId, Object value) {
        return proxy.callFirst(stateManager -> stateManager.setDatastreamValue(deviceId, datastreamId, value));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> setDatastreamValues(String deviceId, Map<String, Object> datastreamValues) {
        return proxy.callFirst(stateManager -> stateManager.setDatastreamValues(deviceId, datastreamValues));
    }
    @Override
    public void onReceivedEvents(List<Event> event) {
        proxy.consumeFirst(stateManager -> stateManager.onReceivedEvents(event));
    }

    @Override
    public void publishValues(List<Event> event) {
        proxy.consumeFirst(stateManager -> stateManager.publishValues(event));
    }

    @Override
    public void close() {
        proxy.close();
    }
}
