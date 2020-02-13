package es.amplia.oda.statemanager.realtime;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinder;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.statemanager.api.EventHandler;
import es.amplia.oda.statemanager.api.StateManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class RealTimeStateManager implements StateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeStateManager.class);
    static final String VALUE_NOT_FOUND_ERROR = "Datastream has no value to set";


    private final DatastreamsGettersFinder datastreamsGettersFinder;
    private final DatastreamsSettersFinder datastreamsSettersFinder;
    private final EventHandler eventHandler;
    private final EventDispatcher eventDispatcher;


    RealTimeStateManager(DatastreamsGettersFinder datastreamsGettersFinder,
                         DatastreamsSettersFinder datastreamsSettersFinder,
                         EventHandler eventHandler, EventDispatcher eventDispatcher) {
        this.datastreamsGettersFinder = datastreamsGettersFinder;
        this.datastreamsSettersFinder = datastreamsSettersFinder;
        this.eventHandler = eventHandler;
        this.eventDispatcher = eventDispatcher;
        registerToEvents(eventHandler);
    }

    //TODO: Check the validity of this
    @Override
    public CompletableFuture<List<DatastreamValue>> getDatastreamInformation(String deviceId, String datastreamId) {
        return getDatastreamsInformation(deviceId, Collections.singleton(datastreamId))
                .thenApply(set -> set.iterator().next());
    }

    @Override
    public CompletableFuture<Set<List<DatastreamValue>>> getDatastreamsInformation(String deviceId, Set<String> datastreamIds) {
        LOGGER.debug("Getting values for device '{}': {}", deviceId, datastreamIds);

        DatastreamsGettersFinder.Return finderReturn =
                datastreamsGettersFinder.getGettersSatisfying(new DevicePattern(deviceId), datastreamIds);

        Set<CompletableFuture<List<DatastreamValue>>> values =
                getNotFoundIdsAsFutures(deviceId, finderReturn.getNotFoundIds());
        values.addAll(getValues(deviceId, finderReturn.getGetters()));

        return allOf(values);
    }

    private Set<CompletableFuture<List<DatastreamValue>>> getNotFoundIdsAsFutures(String deviceId,
                                                                            Set<String> notFoundDatastreamIds) {
        return notFoundDatastreamIds.stream()
                .map(datastreamId -> createDatastreamListNotFound(deviceId, datastreamId))
                .map(CompletableFuture::completedFuture)
                .collect(Collectors.toSet());
    }

    private Set<CompletableFuture<List<DatastreamValue>>> getValues(String deviceId, List<DatastreamsGetter> getters) {
        return getters.stream()
                .map(datastreamsGetter -> getValueFromGetFutureHandlingExceptions(deviceId, datastreamsGetter))
                .collect(Collectors.toSet());
    }

    private CompletableFuture<List<DatastreamValue>> getValueFromGetFutureHandlingExceptions(String deviceId,
                                                                                       DatastreamsGetter datastreamsGetter) {
        String datastreamId = datastreamsGetter.getDatastreamIdSatisfied();
        try {
            CompletableFuture<DatastreamsGetter.CollectedValue> getFuture = datastreamsGetter.get(deviceId);
            return getFuture.handle((ok,error)-> {
                if (ok != null) {
                    return createDatastreamList(deviceId, datastreamId, ok);
                } else {
                    return createDatastreamProcessingErrorList(deviceId, datastreamId, error);
                }
            });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(createDatastreamProcessingErrorList(deviceId, datastreamId, e));
        }
    }

    private static <T> CompletableFuture<Set<T>> allOf(Set<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return allDoneFuture.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public CompletableFuture<Set<List<DatastreamValue>>> getDatastreamsInformation(DevicePattern devicePattern, String datastreamId) {
        return getDatastreamsInformation(devicePattern, Collections.singleton(datastreamId));
    }

    @Override
    public CompletableFuture<Set<List<DatastreamValue>>> getDatastreamsInformation(DevicePattern devicePattern, Set<String> datastreamIds) {
        DatastreamsGettersFinder.Return finderReturn =
                datastreamsGettersFinder.getGettersSatisfying(devicePattern, datastreamIds);
        Set<CompletableFuture<List<DatastreamValue>>> values = getValues(devicePattern, finderReturn.getGetters());
        return allOf(values);
    }

    private Set<CompletableFuture<List<DatastreamValue>>> getValues(DevicePattern devicePattern, List<DatastreamsGetter> getters) {
        return getters.stream()
                .flatMap(datastreamsGetter -> getAllValuesFromGetFutureHandlingExceptions(devicePattern, datastreamsGetter).stream())
                .collect(Collectors.toSet());
    }

    private Set<CompletableFuture<List<DatastreamValue>>> getAllValuesFromGetFutureHandlingExceptions(DevicePattern devicePattern, DatastreamsGetter datastreamsGetter) {
        return datastreamsGetter.getDevicesIdManaged().stream()
                .filter(devicePattern::match)
                .map(deviceId -> getValueFromGetFutureHandlingExceptions(deviceId, datastreamsGetter))
                .collect(Collectors.toSet());
    }

    @Override
    public CompletableFuture<Set<List<DatastreamValue>>> getDeviceInformation(String deviceId) {
        LOGGER.debug("Getting all values for device '{}'", deviceId);

        Set<CompletableFuture<List<DatastreamValue>>> values =
                datastreamsGettersFinder.getGettersOfDevice(deviceId).stream()
                        .map(datastreamsGetter -> getValueFromGetFutureHandlingExceptions(deviceId, datastreamsGetter))
                        .collect(Collectors.toSet());

        return allOf(values);
    }

    //TODO: Check the validity of this
    @Override
    public CompletableFuture<List<DatastreamValue>> refreshDatastreamValue(String deviceId, String datastreamId, Object value) {
        return refreshDatastreamValues(deviceId, Collections.singletonMap(datastreamId, value))
                .thenApply(set -> set.iterator().next());
    }

    @Override
    public CompletableFuture<Set<List<DatastreamValue>>> refreshDatastreamValues(String deviceId, Map<String, Object> datastreamValues) {
        LOGGER.info("Setting for the device '{}' the values: {}", deviceId, datastreamValues);

        DatastreamsSettersFinder.Return satisfyingSetters =
                datastreamsSettersFinder.getSettersSatisfying(deviceId, datastreamValues.keySet());

        HashMap<String, Object> valuesToSet = new HashMap<>(datastreamValues);
        Set<CompletableFuture<List<DatastreamValue>>> values =
                getNotFoundIdsAsFutures(deviceId, satisfyingSetters.getNotFoundIds());
        valuesToSet.entrySet().removeIf(entry -> satisfyingSetters.getNotFoundIds().contains(entry.getKey()));
        values.addAll(getNotFoundValues(deviceId, valuesToSet));
        values.addAll(setValues(deviceId, valuesToSet, satisfyingSetters.getSetters()));

        return allOf(values);
    }

    private Set<CompletableFuture<List<DatastreamValue>>> getNotFoundValues(String deviceId, Map<String, Object> values) {
        return values.entrySet().stream()
                .filter(entry -> Objects.isNull(entry.getValue()))
                .map(entry -> createValueNotFound(deviceId, entry.getKey()))
                .map(CompletableFuture::completedFuture)
                .collect(Collectors.toSet());
    }

    private Set<CompletableFuture<List<DatastreamValue>>> setValues(String deviceId, Map<String, Object> datastreamValues,
                                                              Map<String, DatastreamsSetter> setters) {
        return datastreamValues.entrySet().stream()
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .map(entry ->
                        getValueFromSetFutureHandlingExceptions(deviceId, entry.getKey(),
                                setters.get(entry.getKey()), entry.getValue()))
                .collect(Collectors.toSet());
    }

    private CompletableFuture<List<DatastreamValue>> getValueFromSetFutureHandlingExceptions(String deviceId,
                                                                                             String datastreamId,
                                                                                             DatastreamsSetter datastreamsSetter,
                                                                                             Object value) {
        try {
            CompletableFuture<Void> setFuture = datastreamsSetter.set(deviceId, value);
            return setFuture.handle((ok,error)-> {
                if (error != null) {
                    return createDatastreamProcessingErrorList(deviceId, datastreamId, error);
                } else {
                    return createDatastreamList(deviceId, datastreamId, value);
                }
            });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(createDatastreamProcessingErrorList(deviceId, datastreamId, e));
        }
    }

    private List<DatastreamValue> createDatastreamListNotFound(String deviceId, String datastreamId) {
        List<DatastreamValue> listOfValues = new ArrayList<>();
        listOfValues.add(new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), null,
                DatastreamValue.Status.NOT_FOUND, null));
        return listOfValues;
    }

    private List<DatastreamValue> createDatastreamList(String deviceId, String datastreamId, DatastreamsGetter.CollectedValue value) {
        List<DatastreamValue> listOfValues = new ArrayList<>();
        listOfValues.add(new DatastreamValue(deviceId, datastreamId, value.getAt(), value.getValue(),
                DatastreamValue.Status.OK, null));
        return listOfValues;
    }

    private List<DatastreamValue> createDatastreamList(String deviceId, String datastreamId, Object value) {
        List<DatastreamValue> listOfValues = new ArrayList<>();
        listOfValues.add(new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), value,
                DatastreamValue.Status.OK, null));
        return listOfValues;
    }

    private List<DatastreamValue> createDatastreamProcessingErrorList(String deviceId, String datastreamId, Throwable error) {
        List<DatastreamValue> listOfValues = new ArrayList<>();
        listOfValues.add(new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), null,
                DatastreamValue.Status.PROCESSING_ERROR, error.getMessage()));
        return listOfValues;
    }

    private List<DatastreamValue> createValueNotFound(String deviceId, String datastreamId) {
        List<DatastreamValue> listOfValues = new ArrayList<>();
        listOfValues.add(new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), null,
                DatastreamValue.Status.PROCESSING_ERROR, VALUE_NOT_FOUND_ERROR));
        return listOfValues;
    }

    @Override
    public void registerToEvents(EventHandler eventHandler) {
        eventHandler.registerStateManager(this);
    }

    @Override
    public void unregisterToEvents(EventHandler eventHandler) {
        eventHandler.unregisterStateManager();
    }

    @Override
    public void onReceivedEvent(Event event) {
        eventDispatcher.publish(event);
    }

    @Override
    public void close() {
        unregisterToEvents(eventHandler);
    }
}
