package es.amplia.oda.statemanager.inmemory;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.*;
import es.amplia.oda.core.commons.utils.DatastreamValue.Status;
import es.amplia.oda.event.api.Event;
import es.amplia.oda.ruleengine.api.RuleEngine;
import es.amplia.oda.statemanager.api.EventHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryStateManagerTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DATASTREAM_ID = "testDatastream";
    private static final long TEST_AT = 1L;
    private static final Object TEST_VALUE = "test";
    private static final String TEST_DEVICE_ID_2 = "testDevice2";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final long TEST_AT_2 = 2L;
    private static final Object TEST_VALUE_2 = 99.99;
    private static final String NOT_FOUND_DATASTREAM_ID = "notFound";
    private static final DevicePattern TEST_DEVICE_PATTERN = new DevicePattern("*");


    private final State testState = new State();

    @Mock
    private DatastreamsSettersFinder mockedSettersFinder;
    @Mock
    private EventHandler mockedEventHandler;
    private InMemoryStateManager testStateManager;

    @Mock
    private DatastreamsSetter mockedSetter;
    @Mock
    private RuleEngine mockedEngine;


    @Before
    public void setUp() {
        testStateManager = new InMemoryStateManager(mockedSettersFinder, mockedEventHandler, mockedEngine);

        testState.put(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID),
                new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_AT, TEST_VALUE, Status.OK, null));
        testState.put(new DatastreamInfo(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2),
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_2, TEST_AT_2, TEST_VALUE_2, Status.OK, null));
        testState.put(new DatastreamInfo(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID),
                new DatastreamValue(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID, TEST_AT_2, TEST_VALUE, Status.OK, null));
        Whitebox.setInternalState(testStateManager, "state", testState);
    }

    @Test
    public void testConstructor() {
        verify(mockedEventHandler).registerStateManager(eq(testStateManager));
    }

    @Test
    public void testGetDatastreamInformation() throws ExecutionException, InterruptedException {
        CompletableFuture<DatastreamValue> future =
                testStateManager.getDatastreamInformation(TEST_DEVICE_ID, TEST_DATASTREAM_ID);
        DatastreamValue value = future.get();

        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertEquals(TEST_AT, value.getAt());
        assertEquals(TEST_VALUE, value.getValue());
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
        assertEquals(TEST_AT, value.getAt());
        assertEquals(TEST_VALUE, value.getValue());
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
        checkDatastreamValueOK(TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_AT, TEST_VALUE, values);
        checkDatastreamValueOK(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID, TEST_AT_2, TEST_VALUE, values);
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
        checkDatastreamValueOK(TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_AT, TEST_VALUE, values);
        checkDatastreamValueOK(TEST_DEVICE_ID_2, TEST_DATASTREAM_ID, TEST_AT_2, TEST_VALUE, values);
    }

    @Test
    public void testGetDeviceInformation() throws ExecutionException, InterruptedException {
        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.getDeviceInformation(TEST_DEVICE_ID);
        Set<DatastreamValue> values = future.get();

        assertEquals(1, values.size());
        DatastreamValue value = values.toArray(new DatastreamValue[0])[0];
        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertEquals(TEST_AT, value.getAt());
        assertEquals(TEST_VALUE, value.getValue());
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
        verify(mockedSettersFinder).getSettersSatisfying(eq(TEST_DEVICE_ID), eq(Collections.singleton(TEST_DATASTREAM_ID)));
        verify(mockedSetter).set(eq(TEST_DEVICE_ID), eq(newValue));
        assertEquals(value, testState.getValue(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID)));
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
        verify(mockedSettersFinder).getSettersSatisfying(eq(TEST_DEVICE_ID), eq(Collections.singleton(TEST_DATASTREAM_ID)));
        verify(mockedSetter).set(eq(TEST_DEVICE_ID), eq(newValue));
        assertNotEquals(value, testState.getValue(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID)));
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
        verify(mockedSettersFinder).getSettersSatisfying(eq(TEST_DEVICE_ID), eq(Collections.singleton(TEST_DATASTREAM_ID)));
        verify(mockedSetter).set(eq(TEST_DEVICE_ID), eq(newValue));
        assertNotEquals(value, testState.getValue(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID)));
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
    public void testRegisterToEvents() {
        reset(mockedEventHandler);

        testStateManager.registerToEvents(mockedEventHandler);

        verify(mockedEventHandler).registerStateManager(eq(testStateManager));
    }

    @Test
    public void testOnReceivedEvent() {
        long newAt = System.currentTimeMillis();
        Object newValue = "newTest";
        Event testEvent = new Event(TEST_DATASTREAM_ID, TEST_DEVICE_ID, null, newAt, newValue);

        testStateManager.onReceivedEvent(testEvent);

        DatastreamValue value = testState.getValue(new DatastreamInfo(TEST_DEVICE_ID, TEST_DATASTREAM_ID));
        assertEquals(TEST_DEVICE_ID, value.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID, value.getDatastreamId());
        assertEquals(newAt, value.getAt());
        assertEquals(newValue, value.getValue());
        assertEquals(Status.OK, value.getStatus());
        assertNull(value.getError());
    }

    @Test
    public void testUnregisterToEvents() {
        testStateManager.unregisterToEvents(mockedEventHandler);

        verify(mockedEventHandler).unregisterStateManager();
    }

    @Test
    public void testClose() {
        testStateManager.close();

        verify(mockedEventHandler).unregisterStateManager();
    }
}