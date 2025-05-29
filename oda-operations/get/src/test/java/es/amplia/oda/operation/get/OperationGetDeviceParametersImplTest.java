package es.amplia.oda.operation.get;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsGetter.CollectedValue;
import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.core.commons.utils.DatastreamsGettersFinder;
import es.amplia.oda.core.commons.utils.Event;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationGetDeviceParametersImplTest {
    @SafeVarargs
    private static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final List<String> TEST_DEVICE_ID_LIST = Collections.singletonList(TEST_DEVICE_ID);
    private static final String TEST_FEED = "testFeed";
    private static final String TEST_DATASTREAM_ID_1 = "d1";
    private static final String TEST_VALUE_1 = "Hello";
    private static final long TEST_AT_1 = System.currentTimeMillis();
    private static final String TEST_DATASTREAM_ID_2 = "d2";
    private static final double TEST_VALUE_2 = 12.34;
    private static final long TEST_AT_2 = System.currentTimeMillis() - 3600000;
    private static final String TEST_DATASTREAM_ID_3 = "d3";
    private static final String TEST_DATASTREAM_ID_4 = "d4";
    private static final long TEST_AT_4 = System.currentTimeMillis() - 86400000;

    private static final Set<String> TEST_DATASTREAMS = new HashSet<>(
            Arrays.asList(TEST_DATASTREAM_ID_1, TEST_DATASTREAM_ID_2, TEST_DATASTREAM_ID_3, TEST_DATASTREAM_ID_4)
    );
    private static final String TEST_ERROR = "One of these is null : deviceId '" + TEST_DEVICE_ID + "', datastreamId '" + TEST_DATASTREAM_ID_4 + "' or value 'null'.";
    private static final List<Event> TEST_EVENTS_VALUES = Arrays.asList(
        new Event(TEST_DATASTREAM_ID_1, TEST_DEVICE_ID, null, null, TEST_AT_1, TEST_VALUE_1),
        new Event(TEST_DATASTREAM_ID_2, TEST_DEVICE_ID, null, null, TEST_AT_2, TEST_VALUE_2)
    );

    @Mock
    private DatastreamsGetter getterForId1;
    @Mock
    private DatastreamsGetter getterForId2;
    @Mock
    private DatastreamsGetter getterForId4;
    @Mock
    private StateManager mockedStateManager;
    @Mock
    private DatastreamsGettersFinder mockedDatastreamsGettersFinder;
    @InjectMocks
    private OperationGetDeviceParametersImpl testGetDeviceParametersImpl;

    @Test
    public void testGetDeviceParameters() throws ExecutionException, InterruptedException {
        OperationGetDeviceParameters.GetValue expectedValue1 =
                new OperationGetDeviceParameters.GetValue(TEST_DATASTREAM_ID_1, null, OperationGetDeviceParameters.Status.OK, TEST_AT_1, TEST_VALUE_1, null);
        OperationGetDeviceParameters.GetValue expectedValue2 =
                new OperationGetDeviceParameters.GetValue(TEST_DATASTREAM_ID_2, null, OperationGetDeviceParameters.Status.OK, TEST_AT_2, TEST_VALUE_2, null);
        OperationGetDeviceParameters.GetValue expectedValue3 =
                new OperationGetDeviceParameters.GetValue(TEST_DATASTREAM_ID_3, null, OperationGetDeviceParameters.Status.NOT_FOUND, 0, null, "No datastream getter found for this datastream");
        OperationGetDeviceParameters.GetValue expectedValue4 =
                new OperationGetDeviceParameters.GetValue(TEST_DATASTREAM_ID_4, TEST_FEED, OperationGetDeviceParameters.Status.PROCESSING_ERROR, TEST_AT_4, null, TEST_ERROR);

        DatastreamsGettersFinder.Return getters = new DatastreamsGettersFinder.Return(Arrays.asList(getterForId1, getterForId2, getterForId4), asSet(TEST_DATASTREAM_ID_3));
        when(getterForId1.getDevicesIdManaged()).thenReturn(TEST_DEVICE_ID_LIST);
        when(getterForId2.getDevicesIdManaged()).thenReturn(TEST_DEVICE_ID_LIST);
        when(getterForId4.getDevicesIdManaged()).thenReturn(TEST_DEVICE_ID_LIST);
        when(getterForId1.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_1);
        when(getterForId2.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_2);
        when(getterForId4.getDatastreamIdSatisfied()).thenReturn(TEST_DATASTREAM_ID_4);
        CompletableFuture<CollectedValue> futureForId1 = new CompletableFuture<>();
        CompletableFuture<CollectedValue> futureForId2 = new CompletableFuture<>();
        CompletableFuture<CollectedValue> futureForId4 = new CompletableFuture<>();
        when(getterForId1.get(TEST_DEVICE_ID)).thenReturn(futureForId1);
        when(getterForId2.get(TEST_DEVICE_ID)).thenReturn(futureForId2);
        when(getterForId4.get(TEST_DEVICE_ID)).thenReturn(futureForId4);

        CollectedValue value1 = new CollectedValue(TEST_AT_1, TEST_VALUE_1,null, null);
        CollectedValue value2 = new CollectedValue(TEST_AT_2, TEST_VALUE_2,null, null);
        CollectedValue value4 = new CollectedValue(TEST_AT_4, null,null, TEST_FEED);
        futureForId1.complete(value1);
        futureForId2.complete(value2);
        futureForId4.complete(value4);
        
        when(mockedDatastreamsGettersFinder.getGettersSatisfying(any(), any())).thenReturn(getters);

        CompletableFuture<OperationGetDeviceParameters.Result> future =
                testGetDeviceParametersImpl.getDeviceParameters(TEST_DEVICE_ID, TEST_DATASTREAMS);
        OperationGetDeviceParameters.Result result = future.get();

        assertNotNull(result);
        List<OperationGetDeviceParameters.GetValue> actualValues = result.getValues();
        assertEquals(4, actualValues.size());
        assertTrue(actualValues.contains(expectedValue1));
        assertTrue(actualValues.contains(expectedValue2));
        assertTrue(actualValues.contains(expectedValue3));
        assertTrue(actualValues.contains(expectedValue4));
        verify(mockedStateManager).publishValues(TEST_EVENTS_VALUES);
    }
}