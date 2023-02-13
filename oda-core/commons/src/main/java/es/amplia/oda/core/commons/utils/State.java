package es.amplia.oda.core.commons.utils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class State {
    private Map<DatastreamInfo, DatastreamState> datastreams;

    private static class DatastreamState {
        private List<DatastreamValue> storedValues;
        private boolean refreshed;
        private boolean sendImmediately;

        public DatastreamState() {
            this.storedValues = new ArrayList<>();
            this.refreshed = false;
            this.sendImmediately = false;
        }

        public DatastreamState(List<DatastreamValue> storedValues) {
            this.storedValues = storedValues;
            this.refreshed = false;
            this.sendImmediately = false;
        }

        public List<DatastreamValue> getStoredValues() {
            return storedValues;
        }

        public void refreshValue(DatastreamValue value) {
            this.storedValues.add(value);
            this.refreshed = true;
        }

        public DatastreamValue getLastValue() {
            if (!this.storedValues.isEmpty()) {
                return this.storedValues.get(this.storedValues.size() - 1);
            }
            return null;
        }

        public void addDatastreamLoadedValues(List<DatastreamValue> list) {
            storedValues.addAll(storedValues.size(), list);
        }

        public boolean getRefreshed() {
            return this.refreshed;
        }

        public boolean getSendImmediately() {
            return this.sendImmediately;
        }

        public Stream<DatastreamValue> getNotSentValues() {
            return this.storedValues.stream()
                    .filter(stored -> !stored.isSent());
        }

        public void setSent(long at, boolean sent) {
            this.storedValues.forEach(datastreamValue -> {
                if (datastreamValue.getAt() == at) {
                    datastreamValue.setSent(sent);
                }
            });
        }

        public void sendImmediately() {
            this.sendImmediately = true;
        }

        public void clearRefreshedAndImmediately() {
            this.sendImmediately = false;
            this.refreshed = false;
        }
    }

    /**
     * Default constructor.
     *
     * Initialize all Maps of this class to a new empty HashMap.
     * With this constructor, State is initialized without datastreams
     */
    public State() {
        this.datastreams = new HashMap<>();
    }

    /**
     * Method to load the data of a map into the state. This will overwrite all previous data.
     *
     * Initialize all Maps of this class, inserting all datastreams with their values and putting to false their
     * refreshed and sendImmediately attribute.
     *
     * @param storedValues Map with datastreams to add and their values.
     */
    public void loadData(Map<DatastreamInfo, List<DatastreamValue>> storedValues) {
        storedValues.forEach((datastreamInfo, datastreamValues) -> {
            if (this.datastreams.containsKey(datastreamInfo)) {
                DatastreamState state = this.datastreams.get(datastreamInfo);
                state.addDatastreamLoadedValues(datastreamValues);
            } else {
                this.datastreams.put(datastreamInfo, new DatastreamState(datastreamValues));
            }
        });
        this.datastreams = new HashMap<>();
        storedValues.forEach((info, values) -> this.datastreams.put(info, new DatastreamState(values)));
    }

    /**
     * Method that put a new datastream on the state object, creating a new array that contains the specified value and
     * setting to false its others attributes.
     *
     * This method must be used to add the datastream to the State and not to change the value of a datastream. If the
     * datastreams already exists, this method will do nothing.
     *
     * @param dsInfo Object that contains the deviceId and datastreamId of the datastream, will be used like identifier.
     * @param dsValue Object with the value and its meta-information.
     */
    public void put(DatastreamInfo dsInfo, DatastreamValue dsValue) {
        List<DatastreamValue> values = new ArrayList<>();
        values.add(dsValue);

        DatastreamState state = new DatastreamState(values);
        state.refreshed = true;
        this.datastreams.put(dsInfo, state);
    }

    /**
     * Method that refresh the value of a datastream, adding this value to the storedValues array of that datastream, in
     * the last position.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to refresh belongs.
     * @param datastreamId String with the identifier of the datastream we want to refresh.
     * @param value Object with the new value and its metadata that we want to use to refresh datastream.
     */
    public void refreshValue(String deviceId, String datastreamId, DatastreamValue value) {
        DatastreamState state = this.datastreams.get(getKey(deviceId, datastreamId));

        if (state == null) {
            this.datastreams.put(new DatastreamInfo(deviceId, datastreamId), new DatastreamState());
        }

        this.datastreams.get(getKey(deviceId, datastreamId)).refreshValue(value);
    }

    /**
     * Method that search the Object DatastreamInfo with the specified deviceId and datastreamId. If no DatastreamInfo is
     * found, it returns a new Object with the specified data.
     * @param deviceId String with the identifier of the device to which the datastream we want to search
     * @param datastreamId String with the identifier of the datastream we want to search.
     * @return Object with the information of wanted datastream.
     */
    private DatastreamInfo getKey(String deviceId, String datastreamId) {
        return new DatastreamInfo(deviceId, datastreamId);
    }

    /**
     * Check if the specified datastream exists in the Maps of this State object.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to check.
     * @param datastreamId String with the identifier of the datastream we want to check.
     * @return true if datastream it exists, false otherwise
     */
    public boolean exists(String deviceId, String datastreamId) {
        for (DatastreamInfo dsInfo : datastreams.keySet()) {
            if (dsInfo.getDeviceId().equals(deviceId) && dsInfo.getDatastreamId().equals(datastreamId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method that get the last value (that is the actual value) of the datastream with the specified id's.
     *
     * Used in utils.js.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to get the last value.
     * @param datastreamId String with the identifier of the datastream we want to get the last value.
     * @return Object with the last value and its metadata of the datastream.
     */
    @SuppressWarnings("unused")
    public DatastreamValue getLastValue(String deviceId, String datastreamId) {
        return getLastValue(getKey(deviceId, datastreamId));
    }

    /**
     * Method that get the last value (that is the actual value) of the datastream with the specified data.
     *
     * @param datastreamInfo Object with the information of the wanted datastream
     * @return Object with the last value and its metadata of the datastream.
     */
    public DatastreamValue getLastValue(DatastreamInfo datastreamInfo) {
        DatastreamState dsState = this.datastreams.get(datastreamInfo);
        if (dsState != null) {
            DatastreamValue dsValue = dsState.getLastValue();
            if (dsValue != null) {
                return dsValue;
            }
        }
        return createNotFoundValue(datastreamInfo);
    }

    public Supplier<Stream<DatastreamValue>> getNotSentValuesToSend(DatastreamInfo datastreamInfo) {
        return () -> this.datastreams.keySet().stream()
            .filter(datastreamInfo::equals)
            .flatMap(dsInfo -> this.datastreams.get(dsInfo).getNotSentValues());
    }

    public List<DatastreamValue> getNotSentValues(DatastreamInfo datastreamInfo) {
        DatastreamState state = this.datastreams.get(datastreamInfo);
        if (state != null) {
            List<DatastreamValue> values = state.getStoredValues().stream().filter(value -> !value.isSent()).collect(Collectors.toList());
            if (!values.isEmpty()) {
                return values;
            }
        }
        return createNotFoundValueArray(datastreamInfo);
    }

    public void setSent(String deviceId, String datastreamId, Map<Long, Boolean> values) {
        values.forEach((at, sent) -> this.datastreams.get(new DatastreamInfo(deviceId, datastreamId)).setSent(at, sent));
    }

    /**
     * Method that get all values of the datastream with the specified id's.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to get values.
     * @param datastreamId String with the identifier of the datastream we want to get values.
     * @return List with all values and its metadata of the datastream.
     */
    @SuppressWarnings("unused")
    public List<DatastreamValue> getAllValues(String deviceId, String datastreamId) {
        return getAllValues(getKey(deviceId, datastreamId));
    }

    /**
     * Method that get all values of the datastream with the specified id's.
     *
     * @param datastreamInfo Object with the information of the wanted datastream
     * @return List with all values and its metadata of the datastream.
     */
    @SuppressWarnings("unused")
    public List<DatastreamValue> getAllValues(DatastreamInfo datastreamInfo) {
        DatastreamState dsState = this.datastreams.get(datastreamInfo);
        if (dsState != null) {
            List<DatastreamValue> values = dsState.getStoredValues();
            if (!values.isEmpty()) {
                return values;
            }
        }
        return createNotFoundValueArray(datastreamInfo);
    }

    /**
     * Method to get all datastreams identifiers.
     *
     * @return List with the identifiers of the datastreams.
     */
    public List<DatastreamInfo> getStoredValues() {
        List<DatastreamInfo> storedValues = new ArrayList<>();
        this.datastreams.forEach((info, state) -> storedValues.add(info));
        return storedValues;
    }

    /**
     * Method that check if a datastream was refreshed in this State object.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to check.
     * @param datastreamId String with the identifier of the datastream we want to check.
     * @return true if datastream was refreshed, false otherwise
     */
    public boolean isRefreshed(String deviceId, String datastreamId) {
        return this.datastreams.get(getKey(deviceId, datastreamId)).getRefreshed();
    }

    /**
     * Method that mark the datastream specified by parameters for the State Manager send the last value when it receives
     * the state.
     *
     * Used in utils.js.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to mark.
     * @param datastreamId String with the identifier of the datastream we want to mark.
     */
    @SuppressWarnings("unused")
    public void sendImmediately(String deviceId, String datastreamId) {
        this.datastreams.get(getKey(deviceId, datastreamId)).sendImmediately();
    }


    /**
     * Method that check if a datastream have to be send immediately when it arrives to State Manager.
     *
     * @param datastreamInfo Object with the data of the datastream that we want to check.
     * @return true if datastream was marked to send immediately, false otherwise
     */
    public boolean isToSendImmediately(DatastreamInfo datastreamInfo) {
        return this.datastreams.get(datastreamInfo).getSendImmediately();
    }

    /**
     * Method that reset both refreshed and sendImmediately maps
     */
    public void clearRefreshedAndImmediately() {
        this.datastreams.forEach((info, state) -> state.clearRefreshedAndImmediately());
    }

    /**
     * Create an Object DatastreamValue with the value val and the metadata specified by dsInfo.
     *
     * Used in utils.js.
     *
     * @param dsInfo Object that contains device and datastream that we are going to associate the value.
     * @param val Object that is being associated to the datastream.
     * @return Object DatastreamValue with the value and its metadata.
     */
    @SuppressWarnings("unused")
    public DatastreamValue createValue(DatastreamInfo dsInfo, Object val) {
        return createValue(dsInfo.getDeviceId(), dsInfo.getDatastreamId(), val);
    }

    /**
     * Create and object DatastreamValue with the value val and the metadata specified.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to assign to the value.
     * @param datastreamId String with the identifier of the datastream we want to assign to the value.
     * @param val Object that is being associated to the datastream.
     * @return Object DatastreamValue with the value and its metadata
     */
    public DatastreamValue createValue(String deviceId, String datastreamId, Object val) {
        return new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), val,
                DatastreamValue.Status.OK, "", false);
    }

    /**
     * Create and object DatastreamValue with the value val and the metadata specified.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to assign to the value.
     * @param datastreamId String with the identifier of the datastream we want to assign to the value.
     * @param at date of the datastream
     * @param val Object that is being associated to the datastream.
     * @return Object DatastreamValue with the value and its metadata
     */
    public DatastreamValue createValue(String deviceId, String datastreamId, long at, Object val) {
        return new DatastreamValue(deviceId, datastreamId, at, val, DatastreamValue.Status.OK, "", false);
    }

    /**
     * Auxiliary method used by other to create a value with a NOT_FOUND status. Used to warn that the datastream doesn't
     * exists in State's maps.
     *
     * @param dsInfo Object that contains device and datastream that we are going to associate the NOT_FOUND value.
     * @return List with one element, that will be and DatastreamValue with value null and Status NOT_FOUND.
     */
    public List<DatastreamValue> createNotFoundValueArray(DatastreamInfo dsInfo) {
        List<DatastreamValue> notFoundArray = new ArrayList<>();
        notFoundArray.add(createNotFoundValue(dsInfo));
        return notFoundArray;
    }

    /**
     * Auxiliary method used by other to create a value with a NOT_FOUND status. Used to warn that the datastream doesn't
     * exists in State's maps.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to assign to the NOT_FOUND value.
     * @param datastreamId String with the identifier of the datastream we want to assign to the NOT_FOUND value.
     * @return List with one element, that will be and DatastreamValue with value null and Status NOT_FOUND.
     */
    public List<DatastreamValue> createNotFoundValueArray(String deviceId, String datastreamId) {
        List<DatastreamValue> notFoundArray = new ArrayList<>();
        notFoundArray.add(createNotFoundValue(deviceId, datastreamId));
        return notFoundArray;
    }

    /**
     * Auxiliary method used by other to create a value with a NOT_FOUND status. Used to warn that the datastream doesn't
     * exists in State's maps.
     *
     * @param dsInfo Object that contains device and datastream that we are going to associate the NOT_FOUND value.
     * @return Object DatastreamValue with value null and Status NOT_FOUND.
     */
    public DatastreamValue createNotFoundValue(DatastreamInfo dsInfo) {
        return createNotFoundValue(dsInfo.getDeviceId(), dsInfo.getDatastreamId());
    }

    /**
     * Auxiliary method used by other to create a value with a NOT_FOUND status. Used to warn that the datastream doesn't
     * exists in State's maps.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to assign to the NOT_FOUND value.
     * @param datastreamId String with the identifier of the datastream we want to assign to the NOT_FOUND value.
     * @return Object DatastreamValue with value null and Status NOT_FOUND.
     */
    public DatastreamValue createNotFoundValue(String deviceId, String datastreamId) {
        return new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), null,
                DatastreamValue.Status.NOT_FOUND, "Datastream not found", false);
    }
}
