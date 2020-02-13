package es.amplia.oda.core.commons.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State {
    //TODO: Change this to use only one Map (less maps -> less paths to do -> better time and use of resources) and combine this info on a class
    private Map<DatastreamInfo, List<DatastreamValue>> storedValues;
    private Map<DatastreamInfo, Boolean> refreshedValues;
    private Map<DatastreamInfo, Boolean> sendImmediately;

    /**
     * Default constructor.
     *
     * Initialize all Maps of this class to a new empty HashMap.
     * With this constructor, State is initialized without datastreams
     */
    public State() {
        storedValues = new HashMap<>();
        refreshedValues = new HashMap<>();
        sendImmediately = new HashMap<>();
    }

    /**
     * Constructor with datastreams and their values.
     *
     * Initialize all Maps of this class, inserting all datastreams with their values and putting to false their
     * refreshed and sendImmediately attribute.
     *
     * @param storedValues Map with datastreams to add and their values.
     */
    public State(Map<DatastreamInfo, List<DatastreamValue>> storedValues) {
        this.storedValues = storedValues;
        this.refreshedValues = new HashMap<>();
        this.sendImmediately = new HashMap<>();
        for (DatastreamInfo dsInfo: storedValues.keySet()) {
            this.refreshedValues.put(dsInfo, false);
            this.sendImmediately.put(dsInfo, false);
        }
    }

    /**
     * Constructor with all info of each datastreams.
     *
     * Initialize all Maps of this class, inserting all datastreams with their values and setting the refreshed and
     * sendImmediately to their respective values on maps.
     *
     * @param storedValues Map with datastreams to add and their values.
     * @param refreshedValues Map that contains if a datastream was refreshed during the use of this State object.
     * @param sendImmediately Map that contains if a datastream has to be send immediately when this State object come
     *                        back to the StateManager.
     */
    public State(Map<DatastreamInfo, List<DatastreamValue>> storedValues, Map<DatastreamInfo, Boolean> refreshedValues, Map<DatastreamInfo, Boolean> sendImmediately) {
        this.storedValues = storedValues;
        this.refreshedValues = refreshedValues;
        this.sendImmediately = sendImmediately;
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

        this.storedValues.put(dsInfo, values);
        this.refreshedValues.put(dsInfo, false);
        this.sendImmediately.put(dsInfo, false);
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
        List<DatastreamValue> values = this.storedValues.get(getKey(deviceId, datastreamId));
        values.add(value);

        this.storedValues.put(getKey(deviceId, datastreamId), values);
        this.refreshedValues.put(getKey(deviceId, datastreamId), true);
        this.sendImmediately.putIfAbsent(getKey(deviceId, datastreamId), false);
    }

    /**
     * Method that search the Object DatastreamInfo with the specified deviceId and datastreamId. If no DatastreamInfo is
     * found, it returns a new Object with the specified data.
     * @param deviceId String with the identifier of the device to which the datastream we want to search
     * @param datastreamId String with the identifier of the datastream we want to search.
     * @return Object with the information of wanted datastream.
     */
    private DatastreamInfo getKey(String deviceId, String datastreamId) {
        for (DatastreamInfo dsInfo : storedValues.keySet()) {
            if (dsInfo.getDeviceId().equals(deviceId) && dsInfo.getDatastreamId().equals(datastreamId)) {
                return dsInfo;
            }
        }
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
        for (DatastreamInfo dsInfo : storedValues.keySet()) {
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
        for (DatastreamInfo dsInfo : storedValues.keySet()) {
            if(dsInfo.getDeviceId().equals(deviceId) && dsInfo.getDatastreamId().equals(datastreamId)) {
                List<DatastreamValue> values = storedValues.get(dsInfo);
                if (!values.isEmpty()) {
                    return values.get(values.size() - 1);
                }
            }
        }
        return createNotFoundValue(deviceId, datastreamId);
    }

    /**
     * Method that get the last value (that is the actual value) of the datastream with the specified data.
     *
     * @param datastreamInfo Object with the information of the wanted datastream
     * @return Object with the last value and its metadata of the datastream.
     */
    public DatastreamValue getLastValue(DatastreamInfo datastreamInfo) {
        List<DatastreamValue> values = this.storedValues.getOrDefault(datastreamInfo, createNotFoundValueArray(datastreamInfo));
        return values.get(values.size() - 1);
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
        for (DatastreamInfo dsInfo : storedValues.keySet()) {
            if(dsInfo.getDeviceId().equals(deviceId) && dsInfo.getDatastreamId().equals(datastreamId)) {
                return storedValues.get(dsInfo);
            }
        }
        return createNotFoundValueArray(deviceId, datastreamId);
    }

    /**
     * Method that get all values of the datastream with the specified id's.
     *
     * @param datastreamInfo Object with the information of the wanted datastream
     * @return List with all values and its metadata of the datastream.
     */
    @SuppressWarnings("unused")
    public List<DatastreamValue> getAllValues(DatastreamInfo datastreamInfo) {
        return this.storedValues.getOrDefault(datastreamInfo, createNotFoundValueArray(datastreamInfo));
    }

    /**
     * Method to get all datastreams with their historic of values.
     *
     * @return Map with the identifiers of the datastreams with their values.
     */
    public Map<DatastreamInfo, List<DatastreamValue>> getStoredValues() {
        return this.storedValues;
    }

    /**
     * Method that check if a datastream was refreshed in this State object.
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to check.
     * @param datastreamId String with the identifier of the datastream we want to check.
     * @return true if datastream was refreshed, false otherwise
     */
    public boolean isRefreshed(String deviceId, String datastreamId) {
        for (DatastreamInfo dsInfo : storedValues.keySet()) {
            if (dsInfo.getDeviceId().equals(deviceId) && dsInfo.getDatastreamId().equals(datastreamId)) {
                return refreshedValues.get(dsInfo);
            }
        }
        return false;
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
        sendImmediately.put(getKey(deviceId, datastreamId), true);
    }


    /**
     * Method that check if a datastream have to be send immediately when it arrives to State Manager.
     *
     * @param datastreamInfo Object with the data of the datastream that we want to check.
     * @return true if datastream was marked to send immediately, false otherwise
     */
    public boolean isToSendImmediately(DatastreamInfo datastreamInfo) {
        return this.sendImmediately.get(datastreamInfo);
    }

    /**
     * Method that reset both refreshed and sendImmediately maps
     */
    public void clearRefreshedAndImmediately() {
        refreshedValues.keySet().forEach(value -> refreshedValues.put(value, false));
        sendImmediately.keySet().forEach(value -> sendImmediately.put(value, false));
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
                DatastreamValue.Status.OK, "");
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
                DatastreamValue.Status.NOT_FOUND, "Datastream not found");
    }
}
