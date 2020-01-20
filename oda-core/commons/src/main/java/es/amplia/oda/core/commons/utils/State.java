package es.amplia.oda.core.commons.utils;

import java.util.HashMap;
import java.util.Map;

public class State {
    private Map<DatastreamInfo, DatastreamValue> storedValues;
    private Map<DatastreamInfo, Boolean> refreshedValues;
    private Map<DatastreamInfo, Boolean> sendImmediately;

    public State() {
        storedValues = new HashMap<>();
        refreshedValues = new HashMap<>();
        sendImmediately = new HashMap<>();
    }

    public State(Map<DatastreamInfo, DatastreamValue> storedValues) {
        this.storedValues = storedValues;
        this.refreshedValues = new HashMap<>();
        for (DatastreamInfo dsInfo: storedValues.keySet()) {
            this.refreshedValues.put(dsInfo, false);
            this.sendImmediately.put(dsInfo, false);
        }
    }

    public State(Map<DatastreamInfo, DatastreamValue> storedValues, Map<DatastreamInfo, Boolean> refreshedValues, Map<DatastreamInfo, Boolean> sendImmediately) {
        this.storedValues = storedValues;
        this.refreshedValues = refreshedValues;
        this.sendImmediately = sendImmediately;
    }

    public void put(DatastreamInfo dsInfo, DatastreamValue dsValue) {
        this.storedValues.put(dsInfo, dsValue);
        this.refreshedValues.put(dsInfo, false);
        this.sendImmediately.put(dsInfo, false);
    }

    // To add everything needed
    public void refreshValue(String datastreamId, DatastreamValue value) {
        storedValues.put(getKey(datastreamId), value);
        refreshedValues.put(getKey(datastreamId), true);
        sendImmediately.putIfAbsent(getKey(datastreamId), false);
    }

    public void sendImmediately(String datastreamId) {
        sendImmediately.put(getKey(datastreamId), true);
    }

    private DatastreamInfo getKey(String datastreamId) {
        for (DatastreamInfo dsInfo : storedValues.keySet()) {
            if (dsInfo.getDatastreamId().equals(datastreamId)) {
                return dsInfo;
            }
        }
        return new DatastreamInfo("", datastreamId);
    }

    public DatastreamValue getValue(String datastreamId) {
        for (DatastreamValue dsValues : storedValues.values()) {
            if (dsValues.getDatastreamId().equals(datastreamId)) {
                return dsValues;
            }
        }
        return null;
    }

    public DatastreamValue getValue(DatastreamInfo datastream) {
        return this.storedValues.getOrDefault(datastream, createNotFoundValue(datastream));
    }

    public boolean exists(String datastreamId) {
        for (DatastreamInfo dsInfo : storedValues.keySet()) {
            if (dsInfo.getDatastreamId().equals(datastreamId)) {
                return true;
            }
        }
        return false;
    }

    public Map<DatastreamInfo, DatastreamValue> getStoredValues() {
        return this.storedValues;
    }

    public boolean isRefreshed(String datastreamId) {
        for (DatastreamInfo dsInfo : storedValues.keySet()) {
            if (dsInfo.getDatastreamId().equals(datastreamId)) {
                return refreshedValues.get(dsInfo);
            }
        }
        return false;
    }

    public DatastreamValue createValue(DatastreamInfo dsInfo, Object val) {
        return createValue(dsInfo.getDeviceId(), dsInfo.getDatastreamId(), val);
    }

    public DatastreamValue createValue(String deviceId, String datastreamId, Object val) {
        return new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), val,
                DatastreamValue.Status.OK, "");
    }

    public DatastreamValue createNotFoundValue(DatastreamInfo dsInfo) {
        return createNotFoundValue(dsInfo.getDeviceId(), dsInfo.getDatastreamId());
    }

    public DatastreamValue createNotFoundValue(String deviceId, String datastreamId) {
        return new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), null,
                DatastreamValue.Status.NOT_FOUND, "Datastream not found");
    }

    public void clearRefreshedAndImmediately() {
        refreshedValues.keySet().forEach(value -> refreshedValues.put(value, false));
        sendImmediately.keySet().forEach(value -> sendImmediately.put(value, false));
    }

    public boolean isToSendImmediately(DatastreamInfo datastreamInfo) {
        return this.sendImmediately.get(datastreamInfo);
    }

    @Override
    public String toString() {
        return "State{" +
                "storedValues=" + storedValues +
                ", refreshedValues=" + refreshedValues +
                ", sendImmediately=" + sendImmediately +
                '}';
    }
}
