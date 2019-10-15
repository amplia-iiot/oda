package es.amplia.oda.operation.set;

import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.DatastreamsSettersFinder;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;
import es.amplia.oda.operation.api.OperationSetDeviceParameters.Result;
import es.amplia.oda.operation.api.OperationSetDeviceParameters.ResultCode;
import es.amplia.oda.operation.api.OperationSetDeviceParameters.VariableResult;
import es.amplia.oda.operation.api.OperationSetDeviceParameters.VariableValue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OperationSetDeviceParametersImplTest {

    private static final String DEVICE_ID = "aDeviceId";
    private static final String ID1 = "id1";
    private static final String ID2 = "id2";
    private static final String ID3 = "id3";
    private static final Object VALUE_1_FOR_ID_1 = 40;
    private static final Object VALUE_2_FOR_ID_1 = 42;
    private static final Object VALUE_1_FOR_ID_2 = "hi";
    private static final Object VALUE_1_FOR_ID_3 = true;
    private static final List<VariableValue> operationSetParameters = Arrays.asList(
            new VariableValue(ID1, VALUE_1_FOR_ID_1),
            new VariableValue(ID2, VALUE_1_FOR_ID_2),
            new VariableValue(ID1, VALUE_2_FOR_ID_1)
        );
    private static final Set<String> SETTERS_IN_SET_PARAMETERS = asSet(ID1,ID2);
    
    private OperationSetDeviceParameters operationSetDeviceParameters;
    
    @Mock
    private DatastreamsSettersFinder datastreamsSettersFinder;
    @Mock
    private DatastreamsSetter datastreamsSetterId1;
    @Mock
    private DatastreamsSetter datastreamsSetterId2;
    
    private final CompletableFuture<Void> future1ForID1 = new CompletableFuture<>();
    private final CompletableFuture<Void> future2ForID1 = new CompletableFuture<>();
    private final CompletableFuture<Void> futureForID2 = new CompletableFuture<>();
    
    @SafeVarargs
    private static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }


    @SuppressWarnings("SameParameterValue")
    private static <K,V> Map<K, V> mapOf(K key1, V value1, K key2, V value2) {
        Map<K, V> ret = new HashMap<>();
        ret.put(key1, value1);
        ret.put(key2, value2);
        return ret;
    }
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        operationSetDeviceParameters = new OperationSetDeviceParametersImpl(datastreamsSettersFinder);
        
        Map<String, DatastreamsSetter> setters = mapOf(ID1, datastreamsSetterId1, ID2, datastreamsSetterId2);
        DatastreamsSettersFinder.Return value = new DatastreamsSettersFinder.Return(setters, asSet());
        
        when(datastreamsSettersFinder.getSettersSatisfying(DEVICE_ID, SETTERS_IN_SET_PARAMETERS)).thenReturn(value);
        
        when(datastreamsSetterId1.getDatastreamIdSatisfied()).thenReturn(ID1);
        when(datastreamsSetterId1.getDatastreamType()).thenReturn(Integer.class);
        when(datastreamsSetterId1.getDevicesIdManaged()).thenReturn(Collections.singletonList(DEVICE_ID));
        when(datastreamsSetterId1.set(eq(DEVICE_ID), any(Integer.class))).thenReturn(future1ForID1).thenReturn(future2ForID1);

        when(datastreamsSetterId2.getDatastreamIdSatisfied()).thenReturn(ID2);
        when(datastreamsSetterId2.getDatastreamType()).thenReturn(String.class);
        when(datastreamsSetterId2.getDevicesIdManaged()).thenReturn(Collections.singletonList(DEVICE_ID));
        when(datastreamsSetterId2.set(eq(DEVICE_ID), any(String.class))).thenReturn(futureForID2);
}

    @Test
    public void finderIsUsedToGetTheListOfSetters() {
        operationSetDeviceParameters.setDeviceParameters(DEVICE_ID, operationSetParameters);
        
        verify(datastreamsSettersFinder).getSettersSatisfying(DEVICE_ID, SETTERS_IN_SET_PARAMETERS);
    }
    
    @Test
    public void ifUnknownIdsAreUsedAnAlreadyCompletedCompletableFutureIsReturnedWithErrorDescription() throws InterruptedException, ExecutionException {
        List<VariableValue> aSetOfID3 = Collections.singletonList(new VariableValue(ID3, VALUE_1_FOR_ID_3));
        when(datastreamsSettersFinder.getSettersSatisfying(DEVICE_ID, asSet(ID3))).thenReturn(new DatastreamsSettersFinder.Return(new HashMap<>(), asSet(ID3)));
        
        CompletableFuture<Result> future = operationSetDeviceParameters.setDeviceParameters(DEVICE_ID, aSetOfID3);
        
        assertTrue(future.isDone());
        Result result = future.get();
        assertEquals(ResultCode.SUCCESSFUL, result.getResultCode());
        List<VariableResult> variables = result.getVariables();
        assertNotNull(variables);
        assertEquals(1, variables.size());
        VariableResult variableResult = variables.get(0);
        assertEquals(ID3, variableResult.getIdentifier());
        assertNotNull(variableResult.getError());
    }

    @Test
    public void ifNullValuesAreUsedAnAlreadyCompletedCompletableFutureIsReturnedWithErrorDescription() throws InterruptedException, ExecutionException {
        List<VariableValue> aSetOfID1 = Collections.singletonList(new VariableValue(ID1, null));
        Map<String, DatastreamsSetter> foundSetters = new HashMap<>();
        foundSetters.put(ID1, datastreamsSetterId1);
        when(datastreamsSettersFinder.getSettersSatisfying(DEVICE_ID, asSet(ID1)))
                .thenReturn(new DatastreamsSettersFinder.Return(foundSetters, Collections.emptySet()));

        CompletableFuture<Result> future = operationSetDeviceParameters.setDeviceParameters(DEVICE_ID, aSetOfID1);

        assertTrue(future.isDone());
        Result result = future.get();
        assertEquals(ResultCode.SUCCESSFUL, result.getResultCode());
        List<VariableResult> variables = result.getVariables();
        assertNotNull(variables);
        assertEquals(1, variables.size());
        VariableResult variableResult = variables.get(0);
        assertEquals(ID1, variableResult.getIdentifier());
        assertNotNull(variableResult.getError());
    }
    
    @Test
    public void theReturnedCompletableFutureCompletesWhenAllFuturesCompletes() {
        CompletableFuture<Result> future = operationSetDeviceParameters.setDeviceParameters(DEVICE_ID, operationSetParameters);
        
        assertNotNull(future);
        assertFalse(future.isDone());
        future1ForID1.complete(null);
        futureForID2.complete(null);
        future2ForID1.complete(null);
        assertTrue(future.isDone());
    }
    
    @Test
    public void theCompleteResultAccumulatesPartialResults() throws InterruptedException, ExecutionException {
        CompletableFuture<Result> future = operationSetDeviceParameters.setDeviceParameters(DEVICE_ID, operationSetParameters);
        String errorMsg = "whatever";
        
        future1ForID1.complete(null);
        futureForID2.completeExceptionally(new Throwable(errorMsg));
        future2ForID1.completeExceptionally(new Throwable(errorMsg));
        Result actual = future.get();
        
        Result expected = new Result(
            ResultCode.SUCCESSFUL,
            "SUCCESSFUL",
            Arrays.asList(
                new VariableResult(ID1, null),
                new VariableResult(ID2, errorMsg),
                new VariableResult(ID1, errorMsg)
            )
        );
        assertEquals(expected, actual);
    }
}
