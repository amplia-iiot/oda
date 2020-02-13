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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.statemanager.realtime.RealTimeStateManager.VALUE_NOT_FOUND_ERROR;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RealTimeStateManagerTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DATASTREAM_ID_1 = "testDatastream1";
    private static final String TEST_DATASTREAM_ID_2 = "testDatastream2";
    private static final String TEST_DATASTREAM_ID_3 = "testDatastream3";
    private static final String TEST_DATASTREAM_ID_4 = "testDatastream4";
    private static final String TEST_DATASTREAM_ID_5 = "testDatastream5";
    private static final long TEST_AT_1 = 123456789L;
    private static final double TEST_VALUE_1 = 99.99;
    private static final int TEST_VALUE_2 = 5;
    private static final String TEST_VALUE_3 = "Hello World!";
    private static final String TEST_ERROR_2 = "Error processing datastream";
    private static final String TEST_ERROR_3 = "Exception is thrown";
    private static final Set<String> TEST_DATASTREAMS = new HashSet<>(
            Arrays.asList(TEST_DATASTREAM_ID_1, TEST_DATASTREAM_ID_2, TEST_DATASTREAM_ID_3, TEST_DATASTREAM_ID_4)
    );
    private static final String TEST_DEVICE_ID_2 = "testDevice2";
    private static final DevicePattern TEST_DEVICE_PATTERN = new DevicePattern("*");


    @Mock
    private DatastreamsGettersFinder mockedGettersFinder;
    @Mock
    private DatastreamsSettersFinder mockedSettersFinder;
    @Mock
    private EventHandler mockedEventHandler;
    @Mock
    private EventDispatcher mockedEventDispatcher;
    @InjectMocks
    private RealTimeStateManager testStateManager;

    @Mock
    private DatastreamsGetter mockedGetter1;
    @Mock
    private DatastreamsGetter mockedGetter2;
    @Mock
    private DatastreamsGetter mockedGetter3;
    @Mock
    private DatastreamsSetter mockedSetter1;
    @Mock
    private DatastreamsSetter mockedSetter2;
    @Mock
    private DatastreamsSetter mockedSetter3;
    @Mock
    private DatastreamsSetter mockedSetter4;


    @Test
    public void testConstructor() {
        verify(mockedEventHandler).registerStateManager(eq(testStateManager));
    }

    @Test
    public void testGetDatastreamInformation() throws ExecutionException, InterruptedException {
        DatastreamsGettersFinder.Return satisfyingGetters =
                new DatastreamsGettersFinder.Return(Collections.singletonList(mockedGetter1), Collections.emptySet());

        when(mockedGettersFinder.getGettersSatisfying(any(DevicePattern.class), any())).thenReturn(satisfyingGetters);
        when(mockedGetter1.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(mockedGetter1.get(anyString())).thenReturn(CompletableFuture.completedFuture(
                new DatastreamsGetter.CollectedValue(TEST_AT_1, TEST_VALUE_1)));

        CompletableFuture<DatastreamValue> future =
                testStateManager.getDatastreamInformation(TEST_DEVICE_ID, TEST_DATASTREAM_ID_1);
        DatastreamValue result = future.get();

        assertEquals(TEST_DEVICE_ID, result.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID_1, result.getDatastreamId());
        assertEquals(TEST_AT_1, result.getAt());
        assertEquals(DatastreamValue.Status.OK, result.getStatus());
        assertEquals(TEST_VALUE_1, result.getValue());
        assertNull(result.getError());
        verify(mockedGettersFinder).getGettersSatisfying(eq(new DevicePattern(TEST_DEVICE_ID)),
                eq(Collections.singleton(TEST_DATASTREAM_ID_1)));
        verify(mockedGetter1).getDatastreamIdSatisfied();
        verify(mockedGetter1).get(eq(TEST_DEVICE_ID));
    }

    @Test
    public void testGetDatastreamsInformation() throws ExecutionException, InterruptedException {
        DatastreamsGettersFinder.Return satisfyingGetters =
                new DatastreamsGettersFinder.Return(Arrays.asList(mockedGetter1, mockedGetter2, mockedGetter3),
                        Collections.singleton(TEST_DATASTREAM_ID_4));
        CompletableFuture<DatastreamsGetter.CollectedValue> futureWithError = new CompletableFuture<>();
        futureWithError.completeExceptionally(new RuntimeException(TEST_ERROR_2));

        when(mockedGettersFinder.getGettersSatisfying(any(DevicePattern.class), any())).thenReturn(satisfyingGetters);
        when(mockedGetter1.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(mockedGetter1.get(anyString())).thenReturn(CompletableFuture.completedFuture(
                new DatastreamsGetter.CollectedValue(TEST_AT_1, TEST_VALUE_1)));
        when(mockedGetter2.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_2);
        when(mockedGetter2.get(anyString())).thenReturn(futureWithError);
        when(mockedGetter3.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_3);
        when(mockedGetter3.get(anyString())).thenThrow(new RuntimeException(TEST_ERROR_3));

        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.getDatastreamsInformation(TEST_DEVICE_ID, TEST_DATASTREAMS);
        Set<DatastreamValue> result = future.get();

        assertEquals(4, result.size());
        assertResultContainsDatastreamWithValue(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_1, TEST_VALUE_1);
        assertResultContainsDatastreamWithError(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_2, TEST_ERROR_2);
        assertResultContainsDatastreamWithError(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_3, TEST_ERROR_3);
        assertResultContainsDatastreamNotFound(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_4);
        verify(mockedGettersFinder).getGettersSatisfying(eq(new DevicePattern(TEST_DEVICE_ID)), eq(TEST_DATASTREAMS));
        verify(mockedGetter1).getDatastreamIdSatisfied();
        verify(mockedGetter1).get(eq(TEST_DEVICE_ID));
        verify(mockedGetter2).getDatastreamIdSatisfied();
        verify(mockedGetter2).get(eq(TEST_DEVICE_ID));
        verify(mockedGetter3).getDatastreamIdSatisfied();
        verify(mockedGetter3).get(eq(TEST_DEVICE_ID));
    }

    @SuppressWarnings("SameParameterValue")
    private void assertResultContainsDatastreamWithValue(Set<DatastreamValue> datastreamValues, String device,
                                                         String datastream, Object value) {
        assertTrue(datastreamValues.stream().anyMatch(datastreamValue ->
                device.equals(datastreamValue.getDeviceId()) && datastream.equals(datastreamValue.getDatastreamId()) &&
                        DatastreamValue.Status.OK.equals(datastreamValue.getStatus()) &&
                        value.equals(datastreamValue.getValue()) &&
                        null == datastreamValue.getError()));
    }

    @SuppressWarnings("SameParameterValue")
    private void assertResultContainsDatastreamWithError(Set<DatastreamValue> datastreamValues, String device,
                                                         String datastream, String error) {
        assertTrue(datastreamValues.stream().anyMatch(datastreamValue ->
                device.equals(datastreamValue.getDeviceId()) && datastream.equals(datastreamValue.getDatastreamId()) &&
                        DatastreamValue.Status.PROCESSING_ERROR.equals(datastreamValue.getStatus()) &&
                        null == datastreamValue.getValue() &&
                        error.equals(datastreamValue.getError())));
    }

    @SuppressWarnings("SameParameterValue")
    private void assertResultContainsDatastreamNotFound(Set<DatastreamValue> datastreamValues, String device,
                                                        String datastream) {
        assertTrue(datastreamValues.stream().anyMatch(datastreamValue ->
                device.equals(datastreamValue.getDeviceId()) && datastream.equals(datastreamValue.getDatastreamId()) &&
                        DatastreamValue.Status.NOT_FOUND.equals(datastreamValue.getStatus()) &&
                        null == datastreamValue.getValue() &&
                        null == datastreamValue.getError()));
    }

    @Test
    public void testGetDatastreamInformationWithDevicePattern() throws ExecutionException, InterruptedException {
        DatastreamsGettersFinder.Return satisfyingGetters =
                new DatastreamsGettersFinder.Return(Arrays.asList(mockedGetter1, mockedGetter2), Collections.emptySet());

        when(mockedGettersFinder.getGettersSatisfying(any(DevicePattern.class), any())).thenReturn(satisfyingGetters);
        when(mockedGetter1.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(mockedGetter1.getDevicesIdManaged()).thenReturn(Collections.singletonList(TEST_DEVICE_ID));
        when(mockedGetter1.get(anyString())).thenReturn(CompletableFuture.completedFuture(
                new DatastreamsGetter.CollectedValue(TEST_AT_1, TEST_VALUE_1)));
        when(mockedGetter2.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(mockedGetter2.getDevicesIdManaged()).thenReturn(Collections.singletonList(TEST_DEVICE_ID_2));
        when(mockedGetter2.get(anyString())).thenReturn(CompletableFuture.completedFuture(
                new DatastreamsGetter.CollectedValue(TEST_AT_1, TEST_VALUE_2)));

        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.getDatastreamsInformation(TEST_DEVICE_PATTERN, TEST_DATASTREAM_ID_1);
        Set<DatastreamValue> values = future.get();

        assertNotNull(values);
        assertEquals(2, values.size());
        assertResultContainsDatastreamWithValue(values, TEST_DEVICE_ID, TEST_DATASTREAM_ID_1, TEST_VALUE_1);
        assertResultContainsDatastreamWithValue(values, TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_1, TEST_VALUE_2);
        verify(mockedGettersFinder).getGettersSatisfying(eq(TEST_DEVICE_PATTERN),
                eq(Collections.singleton(TEST_DATASTREAM_ID_1)));
        verify(mockedGetter1).getDatastreamIdSatisfied();
        verify(mockedGetter1).getDevicesIdManaged();
        verify(mockedGetter1).get(eq(TEST_DEVICE_ID));
        verify(mockedGetter2).getDatastreamIdSatisfied();
        verify(mockedGetter2).getDevicesIdManaged();
        verify(mockedGetter2).get(eq(TEST_DEVICE_ID_2));
    }

    @Test
    public void testGetDatastreamsInformationWithDevicePattern() throws ExecutionException, InterruptedException {
        DatastreamsGettersFinder.Return satisfyingGetters =
                new DatastreamsGettersFinder.Return(Arrays.asList(mockedGetter1, mockedGetter2, mockedGetter3),
                        Collections.singleton(TEST_DATASTREAM_ID_4));
        CompletableFuture<DatastreamsGetter.CollectedValue> futureWithError = new CompletableFuture<>();
        futureWithError.completeExceptionally(new RuntimeException(TEST_ERROR_2));

        when(mockedGettersFinder.getGettersSatisfying(any(DevicePattern.class), any())).thenReturn(satisfyingGetters);
        when(mockedGetter1.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(mockedGetter1.getDevicesIdManaged()).thenReturn(Arrays.asList(TEST_DEVICE_ID, TEST_DEVICE_ID_2));
        when(mockedGetter1.get(anyString())).thenReturn(CompletableFuture.completedFuture(
                new DatastreamsGetter.CollectedValue(TEST_AT_1, TEST_VALUE_1)));
        when(mockedGetter2.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_2);
        when(mockedGetter2.getDevicesIdManaged()).thenReturn(Collections.singletonList(TEST_DEVICE_ID));
        when(mockedGetter2.get(anyString())).thenReturn(futureWithError);
        when(mockedGetter3.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_3);
        when(mockedGetter3.getDevicesIdManaged()).thenReturn(Collections.singletonList(TEST_DEVICE_ID_2));
        when(mockedGetter3.get(anyString())).thenThrow(new RuntimeException(TEST_ERROR_3));

        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.getDatastreamsInformation(TEST_DEVICE_PATTERN, TEST_DATASTREAMS);
        Set<DatastreamValue> result = future.get();

        assertEquals(4, result.size());
        assertResultContainsDatastreamWithValue(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_1, TEST_VALUE_1);
        assertResultContainsDatastreamWithValue(result, TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_1, TEST_VALUE_1);
        assertResultContainsDatastreamWithError(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_2, TEST_ERROR_2);
        assertResultContainsDatastreamWithError(result, TEST_DEVICE_ID_2, TEST_DATASTREAM_ID_3, TEST_ERROR_3);
        verify(mockedGettersFinder).getGettersSatisfying(eq(TEST_DEVICE_PATTERN), eq(TEST_DATASTREAMS));
        verify(mockedGetter1, times(2)).getDatastreamIdSatisfied();
        verify(mockedGetter1).getDevicesIdManaged();
        verify(mockedGetter1).get(eq(TEST_DEVICE_ID));
        verify(mockedGetter1).get(eq(TEST_DEVICE_ID_2));
        verify(mockedGetter2).getDatastreamIdSatisfied();
        verify(mockedGetter2).getDevicesIdManaged();
        verify(mockedGetter2).get(eq(TEST_DEVICE_ID));
        verify(mockedGetter3).getDatastreamIdSatisfied();
        verify(mockedGetter3).getDevicesIdManaged();
        verify(mockedGetter3).get(eq(TEST_DEVICE_ID_2));
    }

    @Test
    public void testGetDeviceInformation() throws ExecutionException, InterruptedException {
        List<DatastreamsGetter> deviceGetters = Arrays.asList(mockedGetter1, mockedGetter2);
        CompletableFuture<DatastreamsGetter.CollectedValue> futureWithError = new CompletableFuture<>();
        futureWithError.completeExceptionally(new RuntimeException(TEST_ERROR_2));

        when(mockedGettersFinder.getGettersOfDevice(anyString())).thenReturn(deviceGetters);
        when(mockedGetter1.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(mockedGetter1.get(anyString())).thenReturn(CompletableFuture.completedFuture(
                new DatastreamsGetter.CollectedValue(TEST_AT_1, TEST_VALUE_1)));
        when(mockedGetter2.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_2);
        when(mockedGetter2.get(anyString())).thenReturn(futureWithError);

        CompletableFuture<Set<DatastreamValue>> future = testStateManager.getDeviceInformation(TEST_DEVICE_ID);
        Set<DatastreamValue> result = future.get();

        assertEquals(2, result.size());
        assertResultContainsDatastreamWithValue(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_1, TEST_VALUE_1);
        assertResultContainsDatastreamWithError(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_2, TEST_ERROR_2);
        verify(mockedGettersFinder).getGettersOfDevice(eq(TEST_DEVICE_ID));
        verify(mockedGetter1).getDatastreamIdSatisfied();
        verify(mockedGetter1).get(eq(TEST_DEVICE_ID));
        verify(mockedGetter2).getDatastreamIdSatisfied();
        verify(mockedGetter2).get(eq(TEST_DEVICE_ID));
    }

    @Test
    public void testSetDatastreamValue() throws ExecutionException, InterruptedException {
        DatastreamsSettersFinder.Return satisfyingSetters = new DatastreamsSettersFinder.Return(
                Collections.singletonMap(TEST_DATASTREAM_ID_1, mockedSetter1), Collections.emptySet());

        when(mockedSettersFinder.getSettersSatisfying(anyString(), any())).thenReturn(satisfyingSetters);
        when(mockedSetter1.set(anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));

        CompletableFuture<DatastreamValue> future =
                testStateManager.refreshDatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID_1, TEST_VALUE_1);
        DatastreamValue result = future.get();

        assertEquals(TEST_DEVICE_ID, result.getDeviceId());
        assertEquals(TEST_DATASTREAM_ID_1, result.getDatastreamId());
        assertNotEquals(0L, result.getAt());
        assertEquals(DatastreamValue.Status.OK, result.getStatus());
        assertEquals(TEST_VALUE_1, result.getValue());
        assertNull(result.getError());
        verify(mockedSettersFinder)
                .getSettersSatisfying(eq(TEST_DEVICE_ID), eq(Collections.singleton(TEST_DATASTREAM_ID_1)));
        verify(mockedSetter1).set(eq(TEST_DEVICE_ID), eq(TEST_VALUE_1));
    }

    @Test
    public void testSetDatastreamValues() throws ExecutionException, InterruptedException {
        Map<String, Object> testDatastreamsWithValues = new HashMap<>();
        testDatastreamsWithValues.put(TEST_DATASTREAM_ID_1, TEST_VALUE_1);
        testDatastreamsWithValues.put(TEST_DATASTREAM_ID_2, TEST_VALUE_2);
        testDatastreamsWithValues.put(TEST_DATASTREAM_ID_3, TEST_VALUE_3);
        testDatastreamsWithValues.put(TEST_DATASTREAM_ID_4, null);
        testDatastreamsWithValues.put(TEST_DATASTREAM_ID_5, "Not Found");
        Map<String, DatastreamsSetter> foundSetters = new HashMap<>();
        foundSetters.put(TEST_DATASTREAM_ID_1, mockedSetter1);
        foundSetters.put(TEST_DATASTREAM_ID_2, mockedSetter2);
        foundSetters.put(TEST_DATASTREAM_ID_3, mockedSetter3);
        foundSetters.put(TEST_DATASTREAM_ID_4, mockedSetter4);
        DatastreamsSettersFinder.Return satisfyingSetters =
                new DatastreamsSettersFinder.Return(foundSetters, Collections.singleton(TEST_DATASTREAM_ID_5));
        CompletableFuture<Void> futureWithError = new CompletableFuture<>();
        futureWithError.completeExceptionally(new RuntimeException(TEST_ERROR_2));

        when(mockedSettersFinder.getSettersSatisfying(anyString(), any())).thenReturn(satisfyingSetters);
        when(mockedSetter1.set(anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));
        when(mockedSetter2.set(anyString(), any())).thenReturn(futureWithError);
        when(mockedSetter3.set(anyString(), any())).thenThrow(new RuntimeException(TEST_ERROR_3));

        CompletableFuture<Set<DatastreamValue>> future =
                testStateManager.refreshDatastreamValues(TEST_DEVICE_ID, testDatastreamsWithValues);
        Set<DatastreamValue> result = future.get();

        assertEquals(5, result.size());
        assertResultContainsDatastreamWithValue(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_1, TEST_VALUE_1);
        assertResultContainsDatastreamWithError(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_2, TEST_ERROR_2);
        assertResultContainsDatastreamWithError(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_3, TEST_ERROR_3);
        assertResultContainsDatastreamWithError(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_4, VALUE_NOT_FOUND_ERROR);
        assertResultContainsDatastreamNotFound(result, TEST_DEVICE_ID, TEST_DATASTREAM_ID_5);
        verify(mockedSettersFinder).getSettersSatisfying(eq(TEST_DEVICE_ID), eq(testDatastreamsWithValues.keySet()));
        verify(mockedSetter1).set(eq(TEST_DEVICE_ID), eq(TEST_VALUE_1));
        verify(mockedSetter2).set(eq(TEST_DEVICE_ID), eq(TEST_VALUE_2));
        verify(mockedSetter3).set(eq(TEST_DEVICE_ID), eq(TEST_VALUE_3));
        verifyZeroInteractions(mockedSetter4);
    }

    @Test
    public void testRegisterToEvents() {
        reset(mockedEventHandler);

        testStateManager.registerToEvents(mockedEventHandler);

        verify(mockedEventHandler).registerStateManager(eq(testStateManager));
    }

    @Test
    public void testUnregisterToEvents() {
        testStateManager.unregisterToEvents(mockedEventHandler);

        verify(mockedEventHandler).unregisterStateManager();
    }

    @Test
    public void testOnReceivedEvent() {
        Event event = new Event(TEST_DATASTREAM_ID_1, TEST_DEVICE_ID, null, TEST_AT_1, TEST_VALUE_1);

        testStateManager.onReceivedEvent(event);

        verify(mockedEventDispatcher).publish(eq(event));
    }

    @Test
    public void testClose() {
        testStateManager.close();

        verify(mockedEventHandler).unregisterStateManager();
    }
}