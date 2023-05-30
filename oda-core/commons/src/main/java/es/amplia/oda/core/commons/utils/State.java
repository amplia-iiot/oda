package es.amplia.oda.core.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class State {

    private static final Logger LOGGER = LoggerFactory.getLogger(State.class);
    private Map<DatastreamInfo, DatastreamState> datastreams;

    private static class DatastreamState {
        private List<DatastreamValue> storedValues;
        private boolean refreshed;
        private boolean sendImmediately;
        private long lastTimeHistoricMaxDataCheck;

        public DatastreamState() {
            this.storedValues = new ArrayList<>();
            this.refreshed = false;
            this.sendImmediately = false;
            this.lastTimeHistoricMaxDataCheck = System.currentTimeMillis();
        }

        public DatastreamState(List<DatastreamValue> storedValues) {
            this.storedValues = storedValues;
            this.refreshed = false;
            this.sendImmediately = false;
            this.lastTimeHistoricMaxDataCheck = System.currentTimeMillis();
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

        public long getLastHistoricMaxDataCheck() {
            return this.lastTimeHistoricMaxDataCheck;
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

        public void clearRefreshed() {
            this.refreshed = false;
        }

        public void setLastTimeHistoricMaxDataCheck() {
            this.lastTimeHistoricMaxDataCheck = System.currentTimeMillis();
        }

        public void clearImmediately() {
            this.sendImmediately = false;
        }

        public void removeHistoricValuesInMemory(String datastreamId, String deviceId, long forgetTime, int maxHistoricalData) {
            // remove by max number
            if (this.storedValues.size() > maxHistoricalData) {
                removeHistoricalValuesInMemoryByMaxNumber(datastreamId, deviceId, maxHistoricalData);
            }

            // remove by date
            removeHistoricalValuesInMemoryByDate(datastreamId, deviceId, forgetTime);
        }

        private void removeHistoricalValuesInMemoryByMaxNumber(String datastreamId, String deviceId, int maxHistoricalData) {
            List<DatastreamValue> oldValuesByMaxNumber = this.storedValues.subList(0, this.storedValues.size() - maxHistoricalData);

            if (!oldValuesByMaxNumber.isEmpty()) {
                LOGGER.debug("Erasing historic data in memory for deviceId {} and datastreamId {}, by maxData parameter", deviceId, datastreamId);
                this.storedValues.removeAll(oldValuesByMaxNumber);
            }
        }

        private void removeHistoricalValuesInMemoryByDate(String datastreamId, String deviceId, long forgetTime) {
            long time = System.currentTimeMillis() - (forgetTime * 1000);

            List<DatastreamValue> oldValuesByDate = this.storedValues.stream()
                    .filter(value -> value.getAt() <= time)
                    .collect(Collectors.toList());

            if (!oldValuesByDate.isEmpty()) {
                LOGGER.debug("Erasing historic data in memory for deviceId {} and datastreamId {}, by forgetTime parameter", deviceId, datastreamId);
                this.storedValues.removeAll(oldValuesByDate);
            }
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

    public void logInfo(String msg, Object...objects) {
        LOGGER.info(msg, objects);
    }

    public void logDebug(String msg, Object...objects) {
        LOGGER.debug(msg, objects);
    }

    public void logError(String msg, Object...objects) {
        LOGGER.error(msg, objects);
    }

    public void logTrace(String msg, Object...objects) {
        LOGGER.trace(msg, objects);
    }

    public void logWarn(String msg, Object...objects) {
        LOGGER.warn(msg, objects);
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

    public void clearRefreshed(String datastreamId, String deviceId) {
        this.datastreams.get(new DatastreamInfo(deviceId, datastreamId)).clearRefreshed();
    }

    public void clearSendImmediately(String datastreamId, String deviceId) {
        this.datastreams.get(new DatastreamInfo(deviceId, datastreamId)).clearImmediately();
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

    public void setSent(String deviceId, String datastreamId, Map<Long, Boolean> values) {
        values.forEach((at, sent) -> this.datastreams.get(new DatastreamInfo(deviceId, datastreamId)).setSent(at, sent));
    }

    public void removeHistoricValuesInMemory(String datastreamId, String deviceId, long forgetTime, int maxHistoricData) {
        DatastreamState state = this.datastreams.get(new DatastreamInfo(deviceId, datastreamId));

        if (state != null) {
            state.removeHistoricValuesInMemory(datastreamId, deviceId, forgetTime, maxHistoricData);
        }
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

    public List<DatastreamInfo> getStoredValuesToProcess() {
        List<DatastreamInfo> list = new ArrayList<>();
        for (Map.Entry<DatastreamInfo, DatastreamState> datastreamEntry : this.datastreams.entrySet()) {
            if (datastreamEntry.getValue().sendImmediately || datastreamEntry.getValue().refreshed) {
                list.add(datastreamEntry.getKey());
            }
        }
        return list;
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
     * Method that check if historic data in database needs to be checked
     * Checks if there are more vales than configuration parameter maxData
     *
     * @param datastreamInfo Object with the data of the datastream that we want to check.
     * @param forgetPeriod seconds that indicate when we have to check the need for historic data erasure
     * @return true if is time to check historic data in dataabse
     */
    public boolean isTimeToCheckHistoricMaxDataInDatabase(DatastreamInfo datastreamInfo, long forgetPeriod) {

        // get lastTime historic data has been checked
        long lastTimeChecked = this.datastreams.get(datastreamInfo).getLastHistoricMaxDataCheck();

        // if more time than forgetPeriod have passed since last time historic data was checked, check again
        return System.currentTimeMillis() >= lastTimeChecked + (forgetPeriod * 1000);
    }

    /**
     * Method that sets the date of the last time historic data in database was checked
     * to see if there were more vales than configuration parameter maxData
     *
     * @param deviceId String with the identifier of the device to which the datastream we want to mark.
     * @param datastreamId String with the identifier of the datastream we want to mark.
     */
    public void refreshLastTimeMaxDataCheck(String deviceId, String datastreamId) {
        this.datastreams.get(getKey(deviceId, datastreamId)).setLastTimeHistoricMaxDataCheck();
    }


    /**
     * Method that reset both refreshed and sendImmediately maps
     */
    public void clearAllRefreshedAndImmediately() {
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
