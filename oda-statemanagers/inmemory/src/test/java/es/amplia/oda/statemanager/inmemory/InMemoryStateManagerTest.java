package es.amplia.oda.statemanager.inmemory;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import es.amplia.oda.event.api.EventDispatcher;
import es.amplia.oda.ruleengine.api.RuleEngine;
import es.amplia.oda.statemanager.inmemory.configuration.StateManagerInMemoryConfiguration;
import es.amplia.oda.statemanager.inmemory.database.DatabaseHandler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.util.Collections;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InMemoryStateManager.class, DatabaseHandler.class})
public class InMemoryStateManagerTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DEVICE_ID_2 = "testDevice2";
    private static final String TEST_DEVICE_ID_3 = "testDevice3";

    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final String TEST_DATASTREAM_ID_3 = "testDatastream3";

    private static final String TEST_FEED = "testFeed";

    private static final Object TEST_VALUE_NEW = "test";
    private static final Object TEST_VALUE_OLD = "oldValueTest";
    private static final Long TEST_AT_NEW = 2L;
    private static final Long TEST_AT_OLD = 1L;
    private static final Object TEST_VALUE_2_NEW = 99.99;
    private static final Object TEST_VALUE_2_OLD = 42.;
    private static final String NOT_FOUND_DATASTREAM_ID = "notFound";
    private static final DevicePattern TEST_DEVICE_PATTERN = new DevicePattern("*");


    private final State testState = new State();

    @Mock
    private DatastreamsGettersFinder mockedGettersFinder;
    @Mock
    private DatastreamsGetter mockedGetter1;
    @Mock
    private DatastreamsSettersFinder mockedSettersFinder;
    @Mock
    private EventDispatcher mockedEventDispatcher;
    @Mock
    private DatabaseHandler mockedDatabase;
    private InMemoryStateManager testStateManager;

    @Mock
    private DatastreamsSetter mockedSetter;
    @Mock
    private RuleEngine mockedEngine;
    @Mock
    private Serializer mockedSerializer;
    @Mock
    private BundleContext mockedContext;

    private static final int NUM_THREADS = 2;
    private static final int MAX_SIZE_THREADS_QUEUE = 10;
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(NUM_THREADS, NUM_THREADS,
            0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(MAX_SIZE_THREADS_QUEUE));

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private static final Scheduler scheduler = new SchedulerImpl(executorService);

    @Before
    public void setUp() {
        testStateManager = new InMemoryStateManager(mockedGettersFinder, mockedSettersFinder, mockedEventDispatcher,
                mockedEngine, mockedSerializer, executor, scheduler, mockedContext);

        testState.put(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID),
                new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED, TEST_AT_OLD, TEST_VALUE_OLD, Status.OK, null, true, true));
        testState.put(new DatastreamInfo(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2),
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, null, TEST_AT_OLD, TEST_VALUE_2_OLD, Status.OK, null, true, true));
        testState.put(new DatastreamInfo(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID),
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID, null, TEST_AT_OLD, TEST_VALUE_OLD, Status.OK, null, true, true));

        testState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID,
                new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED, TEST_AT_NEW, TEST_VALUE_NEW, Status.OK, null, false, false));
        testState.refreshValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2,
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, null, TEST_AT_NEW, TEST_VALUE_2_NEW, Status.OK, null, false, false));
        testState.refreshValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID,
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID, null, TEST_AT_NEW, TEST_VALUE_NEW, Status.OK, null, false, false));

        Whitebox.setInternalState(testStateManager, "state", testState);
        Whitebox.setInternalState(testStateManager, "database", mockedDatabase);
        Whitebox.setInternalState(testStateManager, "maxHistoricalData", 10);
        Whitebox.setInternalState(testStateManager, "forgetTime", 100);
        Whitebox.setInternalState(testStateManager, "scheduler", scheduler);

    }


    @Test
    public void testGetDatastreamInformation() throws ExecutionException, InterruptedException {
        CompletableFuture<DatastreamValue> future =
                testStateManager.getDatastreamInformation(TEST_DEVICE_ID, TEST_DATASTREAM_ID);
        DatastreamValue value = future.get();

        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertEquals(TEST_FEED, value.getFeed());
        assertEquals(TEST_AT_NEW, value.getAt());
        assertEquals(TEST_VALUE_NEW, value.getValue());
        assertEquals(Status.OK, value.getStatus());
        assertNull(value.getError());
    }


    @Test
    public void testGetDatastreamInformationNotFound() throws ExecutionException, InterruptedException {
        CompletableFuture<DatastreamValue> future =
                testStateManager.getDatastreamInformation(TEST_DEVICE_ID, NOT_FOUND_DATASTREAM_ID);
        DatastreamValue value = future.get();

        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(NOT_FOUND_DATASTREAM_ID, value.getDatastreamId());
        assertNull(value.getValue());
        assertEquals(Status.NOT_FOUND, value.getStatus());
        assertNotNull(value.getError());
    }

    @Test
    public void testGetDatastreamInformationNoMatchingDeviceIdAndDatastreamId() throws ExecutionException,
            InterruptedException {
        CompletableFuture<DatastreamValue> future =
                testStateManager.getDatastreamInformation(TEST_DEVICE_ID, TEST_DATASTREAM_ID_2);
        DatastreamValue value = future.get();

        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID_2, value.getDatastreamId());
        assertNull(value.getValue());
        assertEquals(Status.NOT_FOUND, value.getStatus());
        assertNotNull(value.getError());
    }

    @Test
    public void testGetDatastreamsInformation() throws ExecutionException, InterruptedException {
        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.getDatastreamsInformation(TEST_DEVICE_ID, Collections.singleton(TEST_DATASTREAM_ID));
        Set<DatastreamValue> values = future.get();

        assertEquals(1, values.size());
        DatastreamValue value = values.toArray(new DatastreamValue[0])[0];
        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertEquals(TEST_AT_NEW, value.getAt());
        assertEquals(TEST_VALUE_NEW, value.getValue());
        assertEquals(Status.OK, value.getStatus());
        assertNull(value.getError());
    }

    @Test
    public void testGetDatastreamsInformationNotFound() throws ExecutionException, InterruptedException {
        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.getDatastreamsInformation(TEST_DEVICE_ID, Collections.singleton(NOT_FOUND_DATASTREAM_ID));
        Set<DatastreamValue> values = future.get();

        assertEquals(1, values.size());
        DatastreamValue value = values.toArray(new DatastreamValue[0])[0];
        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(NOT_FOUND_DATASTREAM_ID, value.getDatastreamId());
        assertNull(value.getValue());
        assertEquals(Status.NOT_FOUND, value.getStatus());
        assertNotNull(value.getError());
    }

    @Test
    public void testGetDatastreamInformationWithDevicePattern() throws ExecutionException, InterruptedException {
        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.getDatastreamsInformation(TEST_DEVICE_PATTERN, TEST_DATASTREAM_ID);
        Set<DatastreamValue> values = future.get();

        assertEquals(2, values.size());
        checkDatastreamValueOK(TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_AT_NEW, TEST_VALUE_NEW, values);
        checkDatastreamValueOK(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID, TEST_AT_NEW, TEST_VALUE_NEW, values);
    }

    @SuppressWarnings("SameParameterValue")
    private void checkDatastreamValueOK(String deviceId, String datastreamId, long at, Object value, Set<DatastreamValue> values) {
        if (values.stream().noneMatch(datastreamValue ->
                deviceId.equals(datastreamValue.getDeviceId()) && datastreamId.equals(datastreamValue.getDatastreamId()) &&
                        at <= datastreamValue.getAt() && value.equals(datastreamValue.getValue()) &&
                        Status.OK.equals(datastreamValue.getStatus()) && datastreamValue.getError() == null)) {
            fail("Datastream value with deviceId " + deviceId + ", datastreamId " + datastreamId + " and value " +
                    value + " with OK status not found");
        }
    }

    @Test
    public void testGetDatastreamsInformationWithDevicePattern() throws ExecutionException, InterruptedException {
        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.getDatastreamsInformation(TEST_DEVICE_PATTERN, Collections.singleton(TEST_DATASTREAM_ID));
        Set<DatastreamValue> values = future.get();

        assertEquals(2, values.size());
        checkDatastreamValueOK(TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_AT_NEW, TEST_VALUE_NEW, values);
        checkDatastreamValueOK(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID, TEST_AT_NEW, TEST_VALUE_NEW, values);
    }

    @Test
    public void testGetDeviceInformation() throws ExecutionException, InterruptedException {
        when(mockedGettersFinder.getGettersOfDevice(any())).thenReturn(Collections.singletonList(mockedGetter1));
        when(mockedGetter1.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID);
        when(mockedGetter1.get(anyString())).thenReturn(CompletableFuture.completedFuture(
                new DatastreamsGetter.CollectedValue(TEST_AT_NEW, TEST_VALUE_NEW)));

        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.getDeviceInformation(TEST_DEVICE_ID);
        Set<DatastreamValue> values = future.get();

        assertEquals(1, values.size());
        DatastreamValue value = values.toArray(new DatastreamValue[0])[0];
        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertEquals(TEST_AT_NEW, value.getAt());
        assertEquals(TEST_VALUE_NEW, value.getValue());
        assertEquals(Status.OK, value.getStatus());
        assertNull(value.getError());
    }

    @Test
    public void testSetDatastreamValue() throws ExecutionException, InterruptedException {
        long beforeTest = System.currentTimeMillis();
        Object newValue = "newTest";

        when(mockedSettersFinder.getSettersSatisfying(anyString(), any()))
                .thenReturn(new DatastreamsSettersFinder.Return(Collections.singletonMap(TEST_DATASTREAM_ID, mockedSetter), Collections.emptySet()));
        when(mockedSetter.set(anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<DatastreamValue> future =
                testStateManager.setDatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, newValue);
        DatastreamValue value = future.get();

        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertTrue(value.getAt() >= beforeTest);
        assertEquals(newValue, value.getValue());
        assertEquals(Status.OK, value.getStatus());
        assertNull(value.getError());
        verify(mockedSettersFinder).getSettersSatisfying(TEST_DEVICE_ID, Collections.singleton(TEST_DATASTREAM_ID));
        verify(mockedSetter).set(TEST_DEVICE_ID, newValue);
        DatastreamValue lastValue = testState.getLastValue(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID));
        assertEquals(value.getDeviceId(), lastValue.getDeviceId());
        assertEquals(value.getDatastreamId(), lastValue.getDatastreamId());
        assertEquals(value.getValue(), lastValue.getValue());
    }


    @Test
    public void testSetDatastreamValueRuntimeException() throws ExecutionException, InterruptedException {
        long beforeTest = System.currentTimeMillis();
        Object newValue = "newTest";
        String errorDescription = "Error description";

        when(mockedSettersFinder.getSettersSatisfying(anyString(), any()))
                .thenReturn(new DatastreamsSettersFinder.Return(Collections.singletonMap(TEST_DATASTREAM_ID, mockedSetter), Collections.emptySet()));
        when(mockedSetter.set(anyString(), any())).thenThrow(new RuntimeException(errorDescription));

        CompletableFuture<DatastreamValue> future =
                testStateManager.setDatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, newValue);
        DatastreamValue value = future.get();

        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertTrue(value.getAt() >= beforeTest);
        assertNull(value.getValue());
        assertEquals(Status.PROCESSING_ERROR, value.getStatus());
        assertNotNull(value.getError());
        verify(mockedSettersFinder).getSettersSatisfying(TEST_DEVICE_ID, Collections.singleton(TEST_DATASTREAM_ID));
        verify(mockedSetter).set(TEST_DEVICE_ID, newValue);
        assertNotEquals(value, testState.getLastValue(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID)));
    }

    @Test
    public void testSetDatastreamValueFutureWithException() throws ExecutionException, InterruptedException {
        long beforeTest = System.currentTimeMillis();
        Object newValue = "newTest";
        String errorDescription = "Error description";
        CompletableFuture<Void> futureWithException = new CompletableFuture<>();
        futureWithException.completeExceptionally(new RuntimeException(errorDescription));

        when(mockedSettersFinder.getSettersSatisfying(anyString(), any()))
                .thenReturn(new DatastreamsSettersFinder.Return(Collections.singletonMap(TEST_DATASTREAM_ID, mockedSetter), Collections.emptySet()));
        when(mockedSetter.set(anyString(), any())).thenReturn(futureWithException);

        CompletableFuture<DatastreamValue> future =
                testStateManager.setDatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, newValue);
        DatastreamValue value = future.get();

        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertTrue(value.getAt() >= beforeTest);
        assertNull(value.getValue());
        assertEquals(Status.PROCESSING_ERROR, value.getStatus());
        assertNotNull(value.getError());
        verify(mockedSettersFinder).getSettersSatisfying(TEST_DEVICE_ID, Collections.singleton(TEST_DATASTREAM_ID));
        verify(mockedSetter).set(TEST_DEVICE_ID, newValue);
        assertNotEquals(value, testState.getLastValue(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID)));
    }

    @Test
    public void testSetDatastreamValues() throws ExecutionException, InterruptedException {
        long beforeTest = System.currentTimeMillis();
        Object newValue = "newTest";
        String newDatastream = "newDatastream";
        DatastreamsSetter newDatastreamSetter = mock(DatastreamsSetter.class);
        Map<String, Object> valuesToSet = new HashMap<>();
        valuesToSet.put(TEST_DATASTREAM_ID, newValue);
        valuesToSet.put(newDatastream, null);
        valuesToSet.put(NOT_FOUND_DATASTREAM_ID, 5);
        Map<String, DatastreamsSetter> foundSetters = new HashMap<>();
        foundSetters.put(TEST_DATASTREAM_ID, mockedSetter);
        foundSetters.put(newDatastream, newDatastreamSetter);

        when(mockedSettersFinder.getSettersSatisfying(anyString(), any())).thenReturn(new DatastreamsSettersFinder.Return(
                foundSetters, Collections.singleton(NOT_FOUND_DATASTREAM_ID)));
        when(mockedSetter.set(anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.setDatastreamValues(TEST_DEVICE_ID, valuesToSet);
        Set<DatastreamValue> values = future.get();
        checkDatastreamValueOK(TEST_DEVICE_ID, TEST_DATASTREAM_ID, beforeTest, newValue, values);
        checkDatastreamValueErrorProcessing(TEST_DEVICE_ID, newDatastream, beforeTest, values);
        checkDatastreamValueNotFound(TEST_DEVICE_ID, NOT_FOUND_DATASTREAM_ID, values);
    }

    @SuppressWarnings("SameParameterValue")
    private void checkDatastreamValueErrorProcessing(String deviceId, String datastreamId, long at, Set<DatastreamValue> values) {
        if (values.stream().noneMatch(datastreamValue ->
                deviceId.equals(datastreamValue.getDeviceId()) && datastreamId.equals(datastreamValue.getDatastreamId()) &&
                        at <= datastreamValue.getAt() && datastreamValue.getValue() == null &&
                        Status.PROCESSING_ERROR.equals(datastreamValue.getStatus()) &&
                        datastreamValue.getError() != null)) {
            fail("Datastream value with deviceId " + deviceId + ", datastreamId " + datastreamId +
                    " and ERROR PROCESSING status not found");
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void checkDatastreamValueNotFound(String deviceId, String datastreamId, Set<DatastreamValue> values) {
        if (values.stream().noneMatch(datastreamValue ->
                deviceId.equals(datastreamValue.getDeviceId()) && datastreamId.equals(datastreamValue.getDatastreamId()) &&
                        datastreamValue.getValue() == null && Status.NOT_FOUND.equals(datastreamValue.getStatus())
                        && datastreamValue.getError() != null)) {
            fail("Datastream value with deviceId " + deviceId + ", datastreamId " + datastreamId +
                    " with NOT FOUND status not found");
        }
    }

    @Test
    public void testOnReceivedEvent() throws IOException, InterruptedException, ExecutionException {
        when(mockedDatabase.exists()).thenReturn(true);
        when(mockedDatabase.insertNewRow(any())).thenReturn(true);
        Long newAt = System.currentTimeMillis();
        Object newValue = "newTest";
        Event testEvent = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, TEST_FEED, newAt, newValue);

        State newState = new State();
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, new DatastreamValue(testEvent.getDeviceId(), testEvent.getDatastreamId(),
                testEvent.getFeed(), testEvent.getAt(), testEvent.getValue(), Status.OK, null, false, false));

        Whitebox.setInternalState(testStateManager, "state", newState);

        when(mockedEngine.engine(any(), any(), any())).thenReturn(newState);

        testStateManager.onReceivedEvents(Collections.singletonList(testEvent));

        // sleep to let threads execute the code
        Thread.sleep(500);

        DatastreamValue value = testStateManager.getDatastreamInformation(TEST_DEVICE_ID, TEST_DATASTREAM_ID).get();
        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertEquals(TEST_FEED, value.getFeed());
        assertEquals(newAt, value.getAt());
        assertEquals(newValue, value.getValue());
        assertEquals(Status.OK, value.getStatus());
        assertNull(value.getError());

        verify(mockedDatabase, times(1)).insertNewRow(any());
    }

    @Test
    public void testMultipleEventsCreatedInRules() throws IOException, InterruptedException {
        when(mockedDatabase.exists()).thenReturn(true);
        when(mockedDatabase.insertNewRow(any())).thenReturn(true);
        Long newAt = System.currentTimeMillis();

        // event that will trigger the process
        Event testEvent = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, TEST_FEED, newAt, "value1");

        State newState = new State();
        // create two new values for the same datastreamId and deviceId
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, new DatastreamValue(testEvent.getDeviceId(), testEvent.getDatastreamId(),
                testEvent.getFeed(), testEvent.getAt(),"value1", Status.OK, null, false, false));
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, new DatastreamValue(testEvent.getDeviceId(), testEvent.getDatastreamId(),
                testEvent.getFeed(), testEvent.getAt(), "value2", Status.OK, null, false, false));

        // get number of notProcessedValues before process
        List<DatastreamValue> notProcessedValues = newState.getNotProcessedValues(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID));
        assertEquals(2, notProcessedValues.size());
        assertEquals(2, notProcessedValues.size());
        assertEquals(TEST_DEVICE_ID, notProcessedValues.get(0).getDeviceId());
        assertEquals(TEST_DEVICE_ID, notProcessedValues.get(1).getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, notProcessedValues.get(0).getDatastreamId());
        assertEquals(TEST_DATASTREAM_ID, notProcessedValues.get(1).getDatastreamId());
        assertEquals(newAt, notProcessedValues.get(0).getAt());
        assertEquals(newAt, notProcessedValues.get(1).getAt());
        assertEquals("value1", notProcessedValues.get(0).getValue());
        assertEquals("value2", notProcessedValues.get(1).getValue());

        // check that processed mark is true
        List<DatastreamValue> storedValues = newState.getAllValues(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID));
        assertFalse(storedValues.get(0).getProcessed());
        assertFalse(storedValues.get(1).getProcessed());

        // check that sent mark is false
        assertFalse(storedValues.get(0).getSent());
        assertFalse(storedValues.get(1).getSent());

        Whitebox.setInternalState(testStateManager, "state", newState);

        when(mockedEngine.engine(any(), any(), any())).thenReturn(newState);

        // process events
        testStateManager.onReceivedEvents(Collections.singletonList(testEvent));

        // sleep to let threads execute the code
        Thread.sleep(500);

        // there must have been two inserts in the database
        verify(mockedDatabase, times(2)).insertNewRow(any());
        verify(mockedDatabase).insertNewRow(notProcessedValues.get(0));
        verify(mockedDatabase).insertNewRow(notProcessedValues.get(1));

        // check that processed mark is true
        storedValues = newState.getAllValues(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID));
        assertTrue(storedValues.get(0).getProcessed());
        assertTrue(storedValues.get(1).getProcessed());

        // check that sent mark is false
        assertFalse(storedValues.get(0).getSent());
        assertFalse(storedValues.get(1).getSent());
    }


    @Test
    public void testOnReceivedEventToSendImmediately() throws IOException, InterruptedException, ExecutionException {
        when(mockedDatabase.exists()).thenReturn(true);
        when(mockedDatabase.insertNewRow(any())).thenReturn(true);
        Long newAt = System.currentTimeMillis();
        Object newValue = "newTest";
        Event testEvent = new Event(TEST_DATASTREAM_ID_3, TEST_DEVICE_ID_3, null, TEST_FEED, newAt, newValue);

        State newState = new State();
        newState.refreshValue(TEST_DEVICE_ID_3, TEST_DATASTREAM_ID_3, new DatastreamValue(testEvent.getDeviceId(),
                testEvent.getDatastreamId(), testEvent.getFeed(), testEvent.getAt(), testEvent.getValue(), Status.OK,
                null, false, false));
        newState.sendImmediately(TEST_DEVICE_ID_3, TEST_DATASTREAM_ID_3);
        Whitebox.setInternalState(testStateManager, "state", newState);

        when(mockedEngine.engine(any(), any(), any())).thenReturn(newState);

        testStateManager.onReceivedEvents(Collections.singletonList(testEvent));

        // sleep to let threads execute the code
        Thread.sleep(500);

        DatastreamValue value = testStateManager.getDatastreamInformation(TEST_DEVICE_ID_3, TEST_DATASTREAM_ID_3).get();
        assertEquals(TEST_DEVICE_ID_3, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID_3, value.getDatastreamId());
        assertEquals(TEST_FEED, value.getFeed());
        assertEquals(newAt, value.getAt());
        assertEquals(newValue, value.getValue());
        assertEquals(Status.OK, value.getStatus());
        assertNull(value.getError());
        assertTrue(value.getSent());
        Event event = new Event(TEST_DATASTREAM_ID_3, TEST_DEVICE_ID_3, null, TEST_FEED, newAt, newValue);
        verify(mockedEventDispatcher).publishImmediately(Collections.singletonList(event));
        verify(mockedDatabase, times(1)).insertNewRow(any());
    }

    @Test
    public void testOnReceivedEventButCantBeStored() throws IOException, InterruptedException, ExecutionException {
        when(mockedDatabase.exists()).thenReturn(true);
        when(mockedDatabase.insertNewRow(any())).thenReturn(false);
        Long newAt = System.currentTimeMillis();
        Object newValue = "newTest";
        Event testEvent = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, null, newAt, newValue);

        State newState = new State();
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, new DatastreamValue(testEvent.getDeviceId(),
                testEvent.getDatastreamId(), testEvent.getFeed(), testEvent.getAt(), testEvent.getValue(), Status.OK,
                null, false, false));
        Whitebox.setInternalState(testStateManager, "state", newState);

        testStateManager.onReceivedEvents(Collections.singletonList(testEvent));

        // sleep to let threads execute the code
        Thread.sleep(500);

        DatastreamValue value = testStateManager.getDatastreamInformation(TEST_DEVICE_ID, TEST_DATASTREAM_ID).get();
        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertEquals(newAt, value.getAt());
        assertEquals(newValue, value.getValue());
        assertEquals(Status.OK, value.getStatus());
        assertNull(value.getError());
        verify(mockedDatabase, times(1)).insertNewRow(any());
    }

    @Test
    public void testOnReceivedEventException() throws IOException, InterruptedException, ExecutionException {
        when(mockedDatabase.exists()).thenReturn(true);
        when(mockedDatabase.insertNewRow(any())).thenThrow( new IOException());
        Long newAt = System.currentTimeMillis();
        Object newValue = "newTest";
        Event testEvent = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, null, newAt, newValue);

        State newState = new State();
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, new DatastreamValue(testEvent.getDeviceId(),
                testEvent.getDatastreamId(), testEvent.getFeed(), testEvent.getAt(), testEvent.getValue(), Status.OK,
                null, false, false));
        Whitebox.setInternalState(testStateManager, "state", newState);

        testStateManager.onReceivedEvents(Collections.singletonList(testEvent));

        // sleep to let threads execute the code
        Thread.sleep(500);

        DatastreamValue value = testStateManager.getDatastreamInformation(TEST_DEVICE_ID, TEST_DATASTREAM_ID).get();
        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertEquals(newAt, value.getAt());
        assertEquals(newValue, value.getValue());
        assertEquals(Status.OK, value.getStatus());
        assertNull(value.getError());
        verify(mockedDatabase, times(1)).insertNewRow(any());
    }

    @Test
    public void testLoadConfiguration() throws Exception {
        whenNew(DatabaseHandler.class).withAnyArguments().thenReturn(mockedDatabase);
        Map<DatastreamInfo, List<DatastreamValue>> collectData = new HashMap<>();
        DatastreamInfo dsInfo = new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID);
        DatastreamValue dsValue = new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, null,
                System.currentTimeMillis(), true, Status.OK, null, false, false);
        List<DatastreamValue> dsValues = new ArrayList<>();
        dsValues.add(dsValue);
        collectData.put(dsInfo, dsValues);
        when(mockedDatabase.collectDataFromDatabase()).thenReturn(collectData);

        this.testStateManager.loadConfiguration(StateManagerInMemoryConfiguration.builder().
                databasePath("this/is/a/path").maxData(100).forgetTime(3600).forgetPeriod(10).build());

        verifyNew(DatabaseHandler.class).withArguments(eq("this/is/a/path"), eq(mockedSerializer),
              eq(scheduler), eq(100), eq((long) 3600), eq((long) 10));
    }

    @Test
    public void testDatastreamsInMemoryLimitByTime() throws InterruptedException {
        // there are two limits
        // by maximum number of datastream values per datastreamId and deviceId
        Whitebox.setInternalState(testStateManager, "maxHistoricalData", 10);
        // by maximum date (in seconds)
        Whitebox.setInternalState(testStateManager, "forgetTime", 1);

        // get current time
        long eventAt = System.currentTimeMillis() + 200;

        State newState = new State();

        // insert three new events for TEST_DEVICE_ID and TEST_DATASTREAM_ID
        DatastreamValue ds = new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, null, eventAt, 100, Status.OK, null, false, false);
        ds.setDate(eventAt);
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, ds);
        ds = new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, null, eventAt - 200, 200, Status.OK, null, false, false);
        ds.setDate(eventAt - 2000000);
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, ds);
        ds = new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, null, eventAt - 300, 300, Status.OK, null, false, false);
        ds.setDate(eventAt - 3000000);
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, ds);

        // insert three new events for TEST_DEVICE_ID_2 and TEST_DATASTREAM_ID_2
        ds = new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, null, eventAt, 400, Status.OK, null, false, false);
        ds.setDate(eventAt);
        newState.refreshValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, ds);
        ds = new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, null, eventAt - 200, 500, Status.OK, null, false, false);
        ds.setDate(eventAt - 2000000);
        newState.refreshValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, ds);
        ds = new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, null, eventAt - 300, 600, Status.OK, null, false, false);
        ds.setDate(eventAt - 3000000);
        newState.refreshValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, ds);

        // check there are three events in state for each combination of datastreamId and deviceId
        assertEquals(3, newState.getAllValues(TEST_DEVICE_ID, TEST_DATASTREAM_ID).size());
        assertEquals(3, newState.getAllValues(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2).size());

        // set as state the one we have created in test
        Whitebox.setInternalState(testStateManager, "state", newState);

        // call method where events are processed
        // the event as param of this method is ignored
        // all datastreamValues in state are processed, not just the one who triggered it
        testStateManager.onReceivedEvents(Collections.singletonList(new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, null, eventAt, 1)));

        // sleep to let threads execute the code
        Thread.sleep(500);

        // check there is only one event in state and that it is the one whose value is 100 for TEST_DEVICE_ID and TEST_DATASTREAM_ID
        assertEquals(1, newState.getAllValues(TEST_DEVICE_ID, TEST_DATASTREAM_ID).size());
        assertEquals(100, newState.getAllValues(TEST_DEVICE_ID, TEST_DATASTREAM_ID).get(0).getValue());

        // check there is only one event in state and that it is the one whose value is 100 for TEST_DEVICE_ID_2 and TEST_DATASTREAM_ID_2
        assertEquals(1, newState.getAllValues(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2).size());
        assertEquals(400, newState.getAllValues(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2).get(0).getValue());
    }

    @Test
    public void testDatastreamsInMemoryLimitByMaxData() throws InterruptedException {
        // there are two limits
        // by maximum number of datastream values per datastreamId and deviceId
        Whitebox.setInternalState(testStateManager, "maxHistoricalData", 2);
        // by maximum date (in seconds)
        Whitebox.setInternalState(testStateManager, "forgetTime", 100);

        // get current time
        long eventAt = System.currentTimeMillis();

        State newState = new State();

        // insert three new events
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID,
                new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, null, eventAt, 100, Status.OK, null, false, false));
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID,
                new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, null, eventAt, 200, Status.OK, null, false, false));
        newState.refreshValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID,
                new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, null, eventAt, 300, Status.OK, null, false, false));

        // insert three new events for TEST_DEVICE_ID_2 and TEST_DATASTREAM_ID_2
        newState.refreshValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2,
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, null, eventAt, 400, Status.OK, null, false, false));
        newState.refreshValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2,
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, null, eventAt, 500, Status.OK, null, false, false));
        newState.refreshValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2,
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, null, eventAt, 600, Status.OK, null, false, false));

        // check there are three events in state for each combination of datastreamId and deviceId
        assertEquals(3, newState.getAllValues(TEST_DEVICE_ID, TEST_DATASTREAM_ID).size());
        assertEquals(3, newState.getAllValues(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2).size());

        // set as state the one we have created in test
        Whitebox.setInternalState(testStateManager, "state", newState);

        // call method where events are processed
        // the event as param of this method is ignored
        // all datastreamValues in state are processed, not just the one who triggered it
        testStateManager.onReceivedEvents(Collections.singletonList(new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID,
                null, null, eventAt, 400)));

        // sleep to let threads execute the code
        Thread.sleep(500);

        // check there is only two event in state and there are the last two introduced (independent of its time value)
        assertEquals(2, newState.getAllValues(TEST_DEVICE_ID, TEST_DATASTREAM_ID).size());
        assertEquals(200, newState.getAllValues(TEST_DEVICE_ID, TEST_DATASTREAM_ID).get(0).getValue());
        assertEquals(300, newState.getAllValues(TEST_DEVICE_ID, TEST_DATASTREAM_ID).get(1).getValue());

        // check there is only two event in state and there are the last two introduced (independent of its time value)
        assertEquals(2, newState.getAllValues(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2).size());
        assertEquals(500, newState.getAllValues(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2).get(0).getValue());
        assertEquals(600, newState.getAllValues(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2).get(1).getValue());

    }
}