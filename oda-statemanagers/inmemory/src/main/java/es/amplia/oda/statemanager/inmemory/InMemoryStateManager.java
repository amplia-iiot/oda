package es.amplia.oda.statemanager.inmemory;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.DatastreamInfo;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;
import es.amplia.oda.core.commons.utils.DevicePattern;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.ruleengine.api.RuleEngine;
import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import es.amplia.oda.statemanager.api.EventHandler;
import es.amplia.oda.statemanager.api.StateManager;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class InMemoryStateManager implements StateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryStateManager.class);

    private static final String VALUE_NOT_FOUND_ERROR = "Datastream has no value to set";

    private final DatastreamsSettersFinder datastreamsSettersFinder;
    private final EventDispatcher eventDispatcher;
    private final EventHandler eventHandler;
    private final RuleEngine ruleEngine;
    private final State state = new State();

    InMemoryStateManager(DatastreamsSettersFinder datastreamsSettersFinder, EventDispatcher eventDispatcher,
                         EventHandler eventHandler, RuleEngine ruleEngine) {
        this.datastreamsSettersFinder = datastreamsSettersFinder;
        this.eventDispatcher = eventDispatcher;
        this.eventHandler = eventHandler;
        this.ruleEngine = ruleEngine;
        registerToEvents(eventHandler);
    }

    @Override
    public CompletableFuture<DatastreamValue> getDatastreamInformation(String deviceId, String datastreamId) {
        LOGGER.info("Get datastream info for device {} and datastream {}", deviceId, datastreamId);
        return CompletableFuture.completedFuture(getDatastreamValue(new DatastreamInfo(deviceId, datastreamId)));
    }

    private DatastreamValue getDatastreamValue(DatastreamInfo datastreamInfo) {
        return state.getValue(datastreamInfo);
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(String deviceId, Set<String> datastreamIds) {
        LOGGER.info("Get datastream info for device {} and datastreams {}", deviceId, datastreamIds);
        return CompletableFuture.completedFuture(datastreamIds.stream()
                .map(datastreamId -> new DatastreamInfo(deviceId, datastreamId))
                .map(this::getDatastreamValue)
                .collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(DevicePattern devicePattern, String datastreamId) {
        LOGGER.info("Get datastream info for device pattern {} and datastream {}", devicePattern, datastreamId);
        return CompletableFuture.completedFuture(
                state.getStoredValues().entrySet().stream()
                        .filter(entry -> datastreamId.equals(entry.getKey().getDatastreamId()))
                        .filter(entry -> devicePattern.match(entry.getKey().getDeviceId()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(DevicePattern devicePattern, Set<String> datastreamIds) {
        LOGGER.info("Get datastream info for device pattern {} and datastreams {}", devicePattern, datastreamIds);
        return CompletableFuture.completedFuture(
                state.getStoredValues().entrySet().stream()
                        .filter(entry -> datastreamIds.contains(entry.getKey().getDatastreamId()))
                        .filter(entry -> devicePattern.match(entry.getKey().getDeviceId()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDeviceInformation(String deviceId) {
        LOGGER.info("Get device info for device {}", deviceId);
        Map<DatastreamInfo, DatastreamValue> stored = state.getStoredValues();
        return CompletableFuture.completedFuture(stored.keySet().stream()
                .filter(datastreamInfo -> deviceId.equals(datastreamInfo.getDeviceId()))
                .map(stored::get)
                .collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<DatastreamValue> setDatastreamValue(String deviceId, String datastreamId, Object value) {
        return setDatastreamValues(deviceId, Collections.singletonMap(datastreamId, value))
                .thenApply(set -> set.toArray(new DatastreamValue[0])[0]);
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> setDatastreamValues(String deviceId, Map<String, Object> datastreamValues) {
        LOGGER.info("Set datastream values for device {}, {}", deviceId, datastreamValues);
        DatastreamsSettersFinder.Return satisfyingSetters =
                datastreamsSettersFinder.getSettersSatisfying(deviceId, datastreamValues.keySet());

        HashMap<String, Object> valuesToSet = new HashMap<>(datastreamValues);
        Set<CompletableFuture<DatastreamValue>> values =
                getNotFoundIdsAsFutures(deviceId, satisfyingSetters.getNotFoundIds());
        valuesToSet.entrySet().removeIf(entry -> satisfyingSetters.getNotFoundIds().contains(entry.getKey()));
        values.addAll(getNotFoundValues(deviceId, valuesToSet));
        values.addAll(setValues(deviceId, valuesToSet, satisfyingSetters.getSetters()));

        return allOf(values);
    }

    private Set<CompletableFuture<DatastreamValue>> getNotFoundIdsAsFutures(String deviceId,
                                                                            Set<String> notFoundDatastreamIds) {
        return notFoundDatastreamIds.stream()
                .map(datastreamId -> state.createNotFoundValue(deviceId, datastreamId))
                .map(CompletableFuture::completedFuture)
                .collect(Collectors.toSet());
    }

    private Set<CompletableFuture<DatastreamValue>> getNotFoundValues(String deviceId, Map<String, Object> values) {
        return values.entrySet().stream()
                .filter(entry -> Objects.isNull(entry.getValue()))
                .map(entry -> createValueNotFound(deviceId, entry.getKey()))
                .map(CompletableFuture::completedFuture)
                .collect(Collectors.toSet());
    }

    private DatastreamValue createValueNotFound(String deviceId, String datastreamId) {
        return new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), null,
                Status.PROCESSING_ERROR, VALUE_NOT_FOUND_ERROR);
    }

    private Set<CompletableFuture<DatastreamValue>> setValues(String deviceId, Map<String, Object> datastreamValues,
                                                              Map<String, DatastreamsSetter> setters) {
        return datastreamValues.entrySet().stream()
                .filter(entry -> Objects.nonNull(entry.getValue()))
                .map(entry ->
                        getValueFromSetFutureHandlingExceptions(deviceId, entry.getKey(),
                                setters.get(entry.getKey()), entry.getValue()))
                .collect(Collectors.toSet());
    }

    private CompletableFuture<DatastreamValue> getValueFromSetFutureHandlingExceptions(String deviceId,
                                                                                       String datastreamId,
                                                                                       DatastreamsSetter datastreamsSetter,
                                                                                       Object value) {
        try {
            CompletableFuture<Void> setFuture = datastreamsSetter.set(deviceId, value);
            return setFuture.handle((ok,error)-> {
                if (error != null) {
                    return new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), null,
                            Status.PROCESSING_ERROR, error.getMessage());
                } else {
                    state.put(new DatastreamInfo(deviceId, datastreamId),
                            new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), value, Status.OK, null));
                    return new DatastreamValue(deviceId, datastreamId, System.currentTimeMillis(), value,
                            Status.OK, null);
                }
            });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new DatastreamValue(deviceId, datastreamId,
                    System.currentTimeMillis(), null, Status.PROCESSING_ERROR, e.getMessage()));
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
    public void registerToEvents(EventHandler eventHandler) {
        eventHandler.registerStateManager(this);
    }

    @Override
    public void onReceivedEvent(Event event) {
        DatastreamValue dsValue = createDatastreamValueFromEvent(event);
        this.ruleEngine.engine(this.state, dsValue);
        // TODO: Add instant send event
        Set<DatastreamInfo> datastreams = this.state.getStoredValues().keySet();
        for (DatastreamInfo dsInfo : datastreams) {
            if(state.isToSendImmediately(dsInfo)) {
                event = new Event(dsInfo.getDatastreamId(), dsInfo.getDeviceId(), event.getPath(), event.getAt(), dsValue.getValue());
                eventDispatcher.publish(event);
            }
        }
        this.state.clearRefreshedAndImmediately();
        LOGGER.info("Registered event value {} to datastream {}", dsValue, event.getDatastreamId());
    }



    private DatastreamValue createDatastreamValueFromEvent(Event event) {
        return new DatastreamValue(event.getDeviceId(), event.getDatastreamId(), event.getAt(), event.getValue(),
                Status.OK, null);
    }

    @Override
    public void unregisterToEvents(EventHandler eventHandler) {
        eventHandler.unregisterStateManager();
    }

    @Override
    public void close() {
        unregisterToEvents(eventHandler);
    }
}
