package es.amplia.oda.statemanager.inmemory;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.core.commons.statemanager.DatabaseException;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.ruleengine.api.RuleEngine;
import es.amplia.oda.statemanager.inmemory.configuration.StateManagerInMemoryConfiguration;
import es.amplia.oda.statemanager.inmemory.database.DatabaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryStateManager implements StateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryStateManager.class);

    private static final String VALUE_NOT_FOUND_ERROR = "Datastream has no value to set";

    private final DatastreamsSettersFinder datastreamsSettersFinder;
    private final EventDispatcher eventDispatcher;
    private final RuleEngine ruleEngine;
    private final Serializer serializer;
    private final State state = new State();
    private DatabaseHandler database;
    private final ExecutorService executor;

    private final Scheduler scheduler;
    private int maxHistoricalData;
    private long forgetTime;
    private long forgetPeriod;


    InMemoryStateManager(DatastreamsSettersFinder datastreamsSettersFinder, EventDispatcher eventDispatcher,
                         RuleEngine ruleEngine, Serializer serializer, ExecutorService executor, Scheduler scheduler) {
        this.datastreamsSettersFinder = datastreamsSettersFinder;
        this.eventDispatcher = eventDispatcher;
        this.ruleEngine = ruleEngine;
        this.serializer = serializer;
        this.executor = executor;
        this.scheduler = scheduler;
    }

    @Override
    public CompletableFuture<DatastreamValue> getDatastreamInformation(String deviceId, String datastreamId) {
        LOGGER.info("Get datastream info for device {} and datastream {}", deviceId, datastreamId);
        return CompletableFuture.completedFuture(state.getLastValue(new DatastreamInfo(deviceId, datastreamId)));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(String deviceId, Set<String> datastreamIds) {
        LOGGER.info("Get datastream info for device {} and datastreams {}", deviceId, datastreamIds);
        return CompletableFuture.completedFuture(datastreamIds.stream()
                .map(datastreamId -> new DatastreamInfo(deviceId, datastreamId))
                .flatMap(this::getStreamOfDatapointsToSend)
                .collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(DevicePattern devicePattern, String datastreamId) {
        LOGGER.info("Get datastream info for device pattern {} and datastream {}", devicePattern, datastreamId);
        return CompletableFuture.completedFuture(
                state.getStoredValues().stream()
                        .filter(entry -> datastreamId.equals(entry.getDatastreamId()))
                        .filter(entry -> devicePattern.match(entry.getDeviceId()))
                        .flatMap(this::getStreamOfDatapointsToSend)
                        .collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(DevicePattern devicePattern, Set<String> datastreamIds) {
        LOGGER.info("Get datastream info for device pattern {} and datastreams {}", devicePattern, datastreamIds);
        return CompletableFuture.completedFuture(
                state.getStoredValues().stream()
                        .filter(entry -> datastreamIds.contains(entry.getDatastreamId()))
                        .filter(entry -> devicePattern.match(entry.getDeviceId()))
                        .flatMap(this::getStreamOfDatapointsToSend)
                        .collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDeviceInformation(String deviceId) {
        LOGGER.info("Get device info for device {}", deviceId);
        List<DatastreamInfo> stored = state.getStoredValues();
        return CompletableFuture.completedFuture(stored.stream()
                .filter(datastreamInfo -> deviceId.equals(datastreamInfo.getDeviceId()))
                .flatMap(this::getStreamOfDatapointsToSend)
                .collect(Collectors.toSet()));
    }

    private synchronized Stream<DatastreamValue> getStreamOfDatapointsToSend(DatastreamInfo datastreamInfo) {
        if (!this.state.exists(datastreamInfo.getDeviceId(), datastreamInfo.getDatastreamId())) {
            ArrayList<DatastreamValue> values = new ArrayList<>();
            values.add(this.state.createNotFoundValue(datastreamInfo));
            return values.stream();
        }
        Map<Long, Boolean> datapoints = database.getDatapointsSentValue(datastreamInfo.getDeviceId(), datastreamInfo.getDatastreamId());
        state.setSent(datastreamInfo.getDeviceId(), datastreamInfo.getDatastreamId(), datapoints);
        // get values to publish
        Supplier<Stream<DatastreamValue>> supplier = state.getNotSentValuesToSend(datastreamInfo);
        Stream<DatastreamValue> returnStream = supplier.get();
        supplier.get()
                .forEach(datastreamValue -> database.updateDataAsSent(
                        datastreamValue.getDeviceId(),
                        datastreamValue.getDatastreamId(),
                        datastreamValue.getAt()));
        return returnStream;
    }

    @Override
    public CompletableFuture<DatastreamValue> setDatastreamValue(String deviceId, String datastreamId, Object value) {
        return setDatastreamValues(deviceId, Collections.singletonMap(datastreamId, value))
                .thenApply(set -> set.iterator().next());
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
        return new DatastreamValue(deviceId, datastreamId, null, System.currentTimeMillis(), null,
                Status.PROCESSING_ERROR, VALUE_NOT_FOUND_ERROR, false, false);
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
                    return new DatastreamValue(deviceId, datastreamId, null, System.currentTimeMillis(), null,
                            Status.PROCESSING_ERROR, error.getMessage(), false, false);
                } else {
                    state.put(new DatastreamInfo(deviceId, datastreamId),
                            new DatastreamValue(deviceId, datastreamId, null, System.currentTimeMillis(), value,
                                    Status.OK, null, false, false));
                    return new DatastreamValue(deviceId, datastreamId, null, System.currentTimeMillis(), value,
                            Status.OK, null, false, false);
                }
            });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new DatastreamValue(deviceId, datastreamId, null,
                    System.currentTimeMillis(), null, Status.PROCESSING_ERROR, e.getMessage(), false, false));
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

    private synchronized void processEvents(List<Event> events) {
        List<Event> eventsToSendImmediately = new ArrayList<>();

        // for every event received
        for (Event event : events) {
            DatastreamValue dsValue = createDatastreamValueFromEvent(event);
            LOGGER.info("Processing new event {}", dsValue);

            // apply rules
            this.ruleEngine.engine(this.state, dsValue);

            // get all values to process (those marked as sendImmediately and/or refreshed)
            // we need to do this because rules engine can alter everything, not just the event received
            List<DatastreamInfo> valuesToProcess = this.state.getStoredValuesToProcess();
            for (DatastreamInfo dsInfo : valuesToProcess) {
                // get not processed values
                List<DatastreamValue> notProcessedValues = state.getNotProcessedValues(dsInfo);

                if (state.isToSendImmediately(dsInfo)) {
                    processEventsToSendImmediately(dsInfo, event, notProcessedValues, eventsToSendImmediately);
                }
                if (state.isRefreshed(dsInfo.getDeviceId(), dsInfo.getDatastreamId())) {
                    processEventsRefreshed(dsInfo, notProcessedValues);
                }
                // remove old values stored in memory
                state.removeHistoricValuesInMemory(dsInfo.getDatastreamId(), dsInfo.getDeviceId(),
                        this.forgetTime, this.maxHistoricalData);

                // mark values as processed
                for(DatastreamValue notProcessedValue :notProcessedValues) {
                    notProcessedValue.setProcessed(true);
                }
            }
        }
        // publish values marked as sendImmediately
        publishValues(eventsToSendImmediately);
    }


    private void processEventsToSendImmediately(DatastreamInfo dsInfo, Event event, List<DatastreamValue> notProcessedValues,
                                                List<Event> eventsToSendImmediately) {
        Event eventToSendImmediately;

        if (!notProcessedValues.isEmpty()) {
            for (DatastreamValue notProcessedValue : notProcessedValues) {

                eventToSendImmediately = new Event(notProcessedValue.getDatastreamId(), notProcessedValue.getDeviceId(),
                        event.getPath(), event.getFeed(), notProcessedValue.getAt(), notProcessedValue.getValue());

                // add event to list to publish
                eventsToSendImmediately.add(eventToSendImmediately);

                // mark value in memory as sent
                notProcessedValue.setSent(true);
            }
        }

        // disable sendImmediately mark
        state.clearSendImmediately(dsInfo.getDatastreamId(), dsInfo.getDeviceId());
    }

    private void processEventsRefreshed(DatastreamInfo dsInfo, List<DatastreamValue> notProcessedValues) {
        if (this.database != null && this.database.exists()) {

            if(!notProcessedValues.isEmpty()) {
                for (DatastreamValue notProcessedValue : notProcessedValues) {
                    try {
                        // if event was marked as send immediately, value will be inserted in database with sent = true
                        // if it was not marked as send immediately, value will be inserted in database with sent = false
                        if (!this.database.insertNewRow(notProcessedValue)) {
                            LOGGER.error("The value {} couldn't be stored into the database.", notProcessedValue);
                        }
                    } catch (DatabaseException | IOException e) {
                        LOGGER.error("Error trying to insert the new value for {} in device {}",
                                dsInfo.getDatastreamId(), dsInfo.getDeviceId());
                    }
                }

                // remove old values stored in database
                removeHistoricMaxDataInDatabase(dsInfo.getDeviceId(), dsInfo.getDatastreamId(), this.forgetPeriod);
            }

            // disable refreshed mark
            state.clearRefreshed(dsInfo.getDatastreamId(), dsInfo.getDeviceId());
        }
    }

    private void removeHistoricMaxDataInDatabase(String deviceId, String datastreamId, long forgetPeriod) {
        if (state.isTimeToCheckHistoricMaxDataInDatabase(new DatastreamInfo(deviceId, datastreamId), forgetPeriod)) {
            // remove historicMaxData from database
            this.database.deleteExcessiveHistoricMaxData(deviceId, datastreamId);

            // update last time check maxData date
            state.refreshLastTimeMaxDataCheck(deviceId, datastreamId);
        }
    }

    @Override
    public void publishValues(List<Event> events) {
        if (events.isEmpty()) {
            return;
        }

        eventDispatcher.publishImmediately(events);
    }

    private DatastreamValue createDatastreamValueFromEvent(Event event) {
        return new DatastreamValue(event.getDeviceId(), event.getDatastreamId(), event.getFeed(), event.getAt(),
                event.getValue(), Status.OK, null, false, false);
    }

    @Override
    public void close() {
        database.close();
    }

    public void loadConfiguration(StateManagerInMemoryConfiguration config) {
        this.forgetTime = config.getForgetTime();
        this.forgetPeriod = config.getForgetPeriod();
        this.maxHistoricalData = config.getMaxData();
        this.database = new DatabaseHandler(config.getDatabasePath(), serializer, scheduler,
                config.getMaxData(), config.getForgetTime(), config.getForgetPeriod());
        // get from database the datastreams stored and load it into memory
        Map<DatastreamInfo, List<DatastreamValue>> collectData = this.database.collectDataFromDatabase();
        this.state.loadData(collectData);
    }

    @Override
    public void onReceivedEvents(List<Event> events) {
        try {
            executor.execute(() -> processEvents(events));
            LOGGER.debug("Thread pool queue - pending tasks = {}, remaining capacity = {}",
                    ((ThreadPoolExecutor) executor).getQueue().size(),
                    ((ThreadPoolExecutor) executor).getQueue().remainingCapacity());
        } catch (RejectedExecutionException e) {
            LOGGER.error("Can't add task to thread pool, reached max size", e);
        }
    }
}
