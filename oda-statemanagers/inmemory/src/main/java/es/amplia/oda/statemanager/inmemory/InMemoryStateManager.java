package es.amplia.oda.statemanager.inmemory;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
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
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemoryStateManager implements StateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryStateManager.class);

    private static final String VALUE_NOT_FOUND_ERROR = "Datastream has no value to set";

    private final DatastreamsSettersFinder datastreamsSettersFinder;
    private final DatastreamsGettersFinder datastreamsGettersFinder;
    private final EventDispatcher eventDispatcher;
    private final RuleEngine ruleEngine;
    private final Serializer serializer;
    private final State state = new State();
    private DatabaseHandler database;
    private ExecutorService executor;

    private final Scheduler scheduler;
    private int maxHistoricalData;
    private long forgetTime;
    private long forgetPeriod;


    InMemoryStateManager(DatastreamsGettersFinder datastreamsGettersFinder, DatastreamsSettersFinder datastreamsSettersFinder,
                         EventDispatcher eventDispatcher, RuleEngine ruleEngine, Serializer serializer,
                         ExecutorService executor, Scheduler scheduler) {
        this.datastreamsGettersFinder = datastreamsGettersFinder;
        this.datastreamsSettersFinder = datastreamsSettersFinder;
        this.eventDispatcher = eventDispatcher;
        this.ruleEngine = ruleEngine;
        this.serializer = serializer;
        this.executor = executor;
        this.scheduler = scheduler;
    }

    @Override
    public CompletableFuture<DatastreamValue> getDatastreamInformation(String deviceId, String datastreamId) {
        LOGGER.debug("Get datastream info for device {} and datastream {}", deviceId, datastreamId);
        return CompletableFuture.completedFuture(state.getLastValue(new DatastreamInfo(deviceId, datastreamId)));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getAllDatastreamsInformation(String deviceId, String datastreamId) {
        LOGGER.debug("Get all datastreams info for device {} and datastream {}", deviceId, datastreamId);
        return CompletableFuture.completedFuture(state.getAllValues(deviceId, datastreamId).stream().collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getAllDatastreamsInformationByAt(String deviceId, String datastreamId) {
        LOGGER.debug("Get all datastreams info for device {} and datastream {}", deviceId, datastreamId);
        return CompletableFuture.completedFuture(state.getAllValues(deviceId, datastreamId).stream()
                    .sorted(new Comparator<DatastreamValue>() {
                        @Override
                        public int compare(DatastreamValue o1, DatastreamValue o2) {
                            if (o1.getAt() == o2.getAt()) return 0;
                            else if (o1.getAt() < o2.getAt()) return -1;
                            else return 1;
                        }
                    })
                    .collect(Collectors.toSet()));
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(String deviceId, Set<String> datastreamIds) {
        LOGGER.debug("Get datastream info for device {} and datastreams {}", deviceId, datastreamIds);
        return CompletableFuture.completedFuture(datastreamIds.stream()
                .map(datastreamId -> new DatastreamInfo(deviceId, datastreamId))
                .flatMap(this::getStreamOfDatapointsToSend)
                .collect(Collectors.toSet()))
                .thenApply(this::setSent);
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(DevicePattern devicePattern, String datastreamId) {
        LOGGER.debug("Get datastream info for device pattern {} and datastream {}", devicePattern, datastreamId);
        return CompletableFuture.completedFuture(
                state.getStoredValues().stream()
                        .filter(entry -> datastreamId.equals(entry.getDatastreamId()))
                        .filter(entry -> devicePattern.match(entry.getDeviceId()))
                        .flatMap(this::getStreamOfDatapointsToSend)
                        .collect(Collectors.toSet()))
                        .thenApply(this::setSent);
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDatastreamsInformation(DevicePattern devicePattern, Set<String> datastreamIds) {
        LOGGER.debug("Get datastream info for device pattern {} and datastreams {}", devicePattern, datastreamIds);
        return CompletableFuture.completedFuture(
                state.getStoredValues().stream()
                        .filter(entry -> datastreamIds.contains(entry.getDatastreamId()))
                        .filter(entry -> devicePattern.match(entry.getDeviceId()))
                        .flatMap(this::getStreamOfDatapointsToSend)
                        .collect(Collectors.toSet()))
                        .thenApply(this::setSent);
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> getDeviceInformation(String deviceId) {
        LOGGER.debug("Get device info for device '{}'", deviceId);

        Set<CompletableFuture<DatastreamValue>> values =
                datastreamsGettersFinder.getGettersOfDevice(deviceId).stream()
                        .map(datastreamsGetter -> getValueFromGetFutureHandlingExceptions(deviceId, datastreamsGetter))
                        .collect(Collectors.toSet());

        return allOf(values);
    }

    private CompletableFuture<DatastreamValue> getValueFromGetFutureHandlingExceptions(String deviceId, DatastreamsGetter datastreamsGetter) {
        String datastreamId = datastreamsGetter.getDatastreamIdSatisfied();
        try {
            CompletableFuture<DatastreamsGetter.CollectedValue> getFuture = datastreamsGetter.get(deviceId);
            return getFuture.handle((ok,error)-> {
                if (ok != null) {
                    return new DatastreamValue(deviceId, datastreamId, ok.getFeed(), ok.getAt(), ok.getValue(),
                            DatastreamValue.Status.OK, null, false, false);
                } else {
                    return new DatastreamValue(deviceId, datastreamId, null, System.currentTimeMillis(), null,
                            DatastreamValue.Status.PROCESSING_ERROR, error.getMessage(), false, false);
                }
            });
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new DatastreamValue(deviceId, datastreamId, null,
                    System.currentTimeMillis(), null, DatastreamValue.Status.PROCESSING_ERROR,
                    e.getMessage(), false, false));
        }
    }



    private synchronized Stream<DatastreamValue> getStreamOfDatapointsToSend(DatastreamInfo datastreamInfo) {
        if (!this.state.exists(datastreamInfo.getDeviceId(), datastreamInfo.getDatastreamId())) {
            ArrayList<DatastreamValue> values = new ArrayList<>();
            values.add(this.state.createNotFoundValue(datastreamInfo));
            return values.stream();
        }
        // get values to publish
        Supplier<Stream<DatastreamValue>> supplier = state.getNotSentValuesToSend(datastreamInfo);
        Stream<DatastreamValue> returnStream = supplier.get();

        return returnStream;
    }

    private Set<DatastreamValue> setSent (Set<DatastreamValue> values) {
        values.forEach(datastreamValue -> {
            if (this.state.exists(datastreamValue.getDeviceId(), datastreamValue.getDatastreamId()) ) {
                setSentInState(datastreamValue);
                database.updateDataAsSent(
                    datastreamValue.getDeviceId(),
                    datastreamValue.getDatastreamId(),
                    datastreamValue.getAt());
            }});
        return values;
    }

    private void setSentInState (DatastreamValue value) {
        state.setSent(value.getDeviceId(), value.getDatastreamId(), value.getAt(), true);
    }

    @Override
    public CompletableFuture<DatastreamValue> setDatastreamValue(String deviceId, String datastreamId, Object value) {
        return setDatastreamValues(deviceId, Collections.singletonMap(datastreamId, value))
                .thenApply(set -> set.iterator().next());
    }

    @Override
    public CompletableFuture<Set<DatastreamValue>> setDatastreamValues(String deviceId, Map<String, Object> datastreamValues) {
        LOGGER.debug("Set datastream values for device {}, {}", deviceId, datastreamValues);
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
            LOGGER.debug("Processing new event {}", dsValue);

            // apply rules & and rules save events in state
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
            // get only values not already sent
            List<DatastreamValue> valuesToSent = notProcessedValues.stream().filter(value -> !value.getSent()).collect(Collectors.toList());
            for (DatastreamValue notProcessedValue : valuesToSent) {

                eventToSendImmediately = new Event(notProcessedValue.getDatastreamId(), notProcessedValue.getDeviceId(),
                        event.getPath(), notProcessedValue.getFeed(), notProcessedValue.getAt(), notProcessedValue.getValue());

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
                    LOGGER.debug("Processing refreshed event = {}", notProcessedValue);

                    try {
                        // if event was marked as send immediately, value will be inserted in database with sent = true
                        // if it was not marked as send immediately, value will be inserted in database with sent = false
                        if (!this.database.insertNewRow(notProcessedValue)) {
                            LOGGER.error("The value {} couldn't be stored into the database.", notProcessedValue);
                        }
                        // If datastream isSent mark as sent in State
                        if (notProcessedValue.getSent()) setSentInState(notProcessedValue);
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

        // update executor size
        updatePendingTasksProcessor(config.getNumThreads(), config.getTaskQueueSize());
    }

    @Override
    public void onReceivedEvents(List<Event> events) {
        LOGGER.debug("Processing event thread pool - num threads = {}, queue (occupied size = {}, remaining capacity = {})",
                ((ThreadPoolExecutor) executor).getPoolSize(),
                ((ThreadPoolExecutor) executor).getQueue().size(),
                ((ThreadPoolExecutor) executor).getQueue().remainingCapacity());

        try {
            executor.execute(() -> processEvents(events));
        } catch (RejectedExecutionException e) {
            LOGGER.error("Can't add task to processing events thread pool, reached pending tasks max capacity");
        }
    }

    private void updatePendingTasksProcessor(int numThreads, int sizeQueue) {
        LOGGER.debug("Updating processing events thread pool - new num threads = {}, queue new total size = {}", numThreads, sizeQueue);

        // create new executor with new queue
        ExecutorService newExecutor = new ThreadPoolExecutor(numThreads, numThreads, 0L,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(sizeQueue));

        // change executors so new tasks are added to the new executor
        ExecutorService oldExecutor = this.executor;
        this.executor = newExecutor;

        // stop old executor to not accept new tasks
        // this also executes all pending tasks in queue
        oldExecutor.shutdown();

        /*
        // get current pending tasks and pass it to new queue
        BlockingQueue<Runnable> oldQueue = ((ThreadPoolExecutor) oldExecutor).getQueue();

        for (Runnable pendingTask : oldQueue) {
            this.executor.submit(pendingTask);
        }*/
    }

}
