package es.amplia.oda.operation.set;

import es.amplia.oda.core.commons.interfaces.StateManager;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;
import es.amplia.oda.operation.api.OperationSetDeviceParameters.VariableValue;
import es.amplia.oda.core.commons.utils.DatastreamValue;

import lombok.Value;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationSetDeviceParametersImplTest {

    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String TEST_DATASTREAM_ID_1 = "d1";
    private static final String TEST_VALUE_1 = "Hello";
    private static final String TEST_DATASTREAM_ID_2 = "d2";
    private static final double TEST_VALUE_2 = 12.34;
    private static final String TEST_DATASTREAM_ID_3 = "d3";
    private static final int TEST_VALUE_3 = 5;
    private static final String TEST_DATASTREAM_ID_4 = "d4";
    private static final Object TEST_VALUE_4 = new TestObject("Bye", 99.99, 9);
    private static final List<VariableValue> TEST_SET_VALUES = Arrays.asList(
            new VariableValue(TEST_DATASTREAM_ID_1, TEST_VALUE_1),
            new VariableValue(TEST_DATASTREAM_ID_2, TEST_VALUE_2),
            new VariableValue(TEST_DATASTREAM_ID_3, TEST_VALUE_3),
            new VariableValue(TEST_DATASTREAM_ID_4, TEST_VALUE_4)
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

    @Value
    private static class TestObject {
        private String v1;
        private double v2;
        private int v3;
    }

    @Mock
    private StateManager mockedStateManager;
    @InjectMocks
    private OperationSetDeviceParametersImpl testSetDeviceParameters;

    @Test

    public void testSetDeviceParameters() throws ExecutionException, InterruptedException {
        Map<String, Object> expectedDatastreamValues = new HashMap<>();
        expectedDatastreamValues.put(TEST_DATASTREAM_ID_1, TEST_VALUE_1);
        expectedDatastreamValues.put(TEST_DATASTREAM_ID_2, TEST_VALUE_2);
        expectedDatastreamValues.put(TEST_DATASTREAM_ID_3, TEST_VALUE_3);
        expectedDatastreamValues.put(TEST_DATASTREAM_ID_4, TEST_VALUE_4);

        when(mockedStateManager.setDatastreamValues(anyString(), any())).thenReturn(
                CompletableFuture.completedFuture(TEST_DATASTREAM_VALUES));

        CompletableFuture<OperationSetDeviceParameters.Result> future =
                testSetDeviceParameters.setDeviceParameters(TEST_DEVICE_ID, TEST_SET_VALUES);
        OperationSetDeviceParameters.Result result = future.get();

        assertNotNull(result);
        assertEquals(OperationSetDeviceParameters.ResultCode.SUCCESSFUL, result.getResultCode());
        List<OperationSetDeviceParameters.VariableResult> variableResults = result.getVariables();
        assertEquals(4, variableResults.size());
        assertTrue(variableResults.contains(new OperationSetDeviceParameters.VariableResult(TEST_DATASTREAM_ID_1, null)));
        assertTrue(variableResults.contains(new OperationSetDeviceParameters.VariableResult(TEST_DATASTREAM_ID_2, null)));
        assertTrue(variableResults.contains(new OperationSetDeviceParameters.VariableResult(TEST_DATASTREAM_ID_3, "Not found")));
        assertTrue(variableResults.contains(new OperationSetDeviceParameters.VariableResult(TEST_DATASTREAM_ID_4, "Error processing: " + TEST_ERROR)));
        verify(mockedStateManager).setDatastreamValues(eq(TEST_DEVICE_ID), eq(expectedDatastreamValues));
    }
}
