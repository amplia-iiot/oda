package es.amplia.oda.operation.get;

import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.statemanager.api.StateManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
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

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DATASTREAM_ID_1 = "d1";
    private static final String TEST_VALUE_1 = "Hello";
    private static final String TEST_DATASTREAM_ID_2 = "d2";
    private static final double TEST_VALUE_2 = 12.34;
    private static final String TEST_DATASTREAM_ID_3 = "d3";
    private static final String TEST_DATASTREAM_ID_4 = "d4";
    private static final Set<String> TEST_DATASTREAMS = new HashSet<>(
            Arrays.asList(TEST_DATASTREAM_ID_1, TEST_DATASTREAM_ID_2, TEST_DATASTREAM_ID_3, TEST_DATASTREAM_ID_4)
    );
    private static final long TEST_AT = System.currentTimeMillis();
    private static final String TEST_ERROR = "Error!";
    private static final Set<DatastreamValue> TEST_DATASTREAM_VALUES = new HashSet<>(
            Arrays.asList(
                    new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID_1, TEST_AT, TEST_VALUE_1, DatastreamValue.Status.OK, null, false),
                    new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID_2, TEST_AT, TEST_VALUE_2, DatastreamValue.Status.OK, null, false),
                    new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID_3, TEST_AT, null, DatastreamValue.Status.NOT_FOUND, null, false),
                    new DatastreamValue(TEST_DEVICE_ID, TEST_DATASTREAM_ID_4, TEST_AT, null, DatastreamValue.Status.PROCESSING_ERROR, TEST_ERROR, false)
            )
    );


    @Mock
    private StateManager mockedStateManager;
    @InjectMocks
    private OperationGetDeviceParametersImpl testGetDeviceParametersImpl;

    @Test
    public void testGetDeviceParameters() throws ExecutionException, InterruptedException {
        OperationGetDeviceParameters.GetValue expectedValue1 =
                new OperationGetDeviceParameters.GetValue(TEST_DATASTREAM_ID_1, OperationGetDeviceParameters.Status.OK, TEST_VALUE_1, null);
        OperationGetDeviceParameters.GetValue expectedValue2 =
                new OperationGetDeviceParameters.GetValue(TEST_DATASTREAM_ID_2, OperationGetDeviceParameters.Status.OK, TEST_VALUE_2, null);
        OperationGetDeviceParameters.GetValue expectedValue3 =
                new OperationGetDeviceParameters.GetValue(TEST_DATASTREAM_ID_3, OperationGetDeviceParameters.Status.NOT_FOUND, null, null);
        OperationGetDeviceParameters.GetValue expectedValue4 =
                new OperationGetDeviceParameters.GetValue(TEST_DATASTREAM_ID_4, OperationGetDeviceParameters.Status.PROCESSING_ERROR, null, TEST_ERROR);

        when(mockedStateManager.getDatastreamsInformation(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(TEST_DATASTREAM_VALUES));

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
        verify(mockedStateManager).getDatastreamsInformation(eq(TEST_DEVICE_ID), eq(TEST_DATASTREAMS));
    }
}