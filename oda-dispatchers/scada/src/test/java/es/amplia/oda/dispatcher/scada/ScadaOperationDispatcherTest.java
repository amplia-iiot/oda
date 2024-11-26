package es.amplia.oda.dispatcher.scada;

import es.amplia.oda.core.commons.exceptions.DataNotFoundException;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.core.commons.interfaces.ScadaDispatcher.ScadaOperation;
import static es.amplia.oda.core.commons.interfaces.ScadaDispatcher.ScadaOperationResult;
import static es.amplia.oda.core.commons.interfaces.ScadaTableTranslator.ScadaInfo;
import static es.amplia.oda.operation.api.OperationGetDeviceParameters.GetValue;
import static es.amplia.oda.operation.api.OperationGetDeviceParameters.Status;
import static es.amplia.oda.operation.api.OperationSetDeviceParameters.VariableResult;
import static es.amplia.oda.operation.api.OperationSetDeviceParameters.VariableValue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScadaOperationDispatcherTest {

    private static final int TEST_INDEX = 1;
    private static final double TEST_VALUE = 18.95;
    private static final long TEST_AT = System.currentTimeMillis();
    private static final Object TEST_TYPE = null;
    private static final String TEST_FEED = "feed";
    private static final String TEST_EVENT_PUBLISH = "stateManager";
    private static final String TEST_DEVICE_ID = "";
    private static final String TEST_DATASTREAM_ID = "testDatastream";

    @Mock
    private ScadaTableTranslator mockedTranslator;
    @Mock
    private OperationGetDeviceParameters mockedGetDeviceParameters;
    @Mock
    private OperationSetDeviceParameters mockedSetDeviceParameters;
    @InjectMocks
    private ScadaOperationDispatcher testOperationDispatcher;

    private final ScadaTableTranslator.ScadaTranslationInfo datastreamInfo = new ScadaTableTranslator.ScadaTranslationInfo(
            TEST_DEVICE_ID, TEST_DATASTREAM_ID, TEST_FEED, TEST_EVENT_PUBLISH);

    @Test
    public void testProcessSelectOperation() throws ExecutionException, InterruptedException {
        GetValue getValue =  new GetValue(TEST_DATASTREAM_ID, null, Status.OK, TEST_AT, TEST_VALUE, null);
        OperationGetDeviceParameters.Result getResult =
                new OperationGetDeviceParameters.Result(Collections.singletonList(getValue));

        when(mockedTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(datastreamInfo);
        when(mockedGetDeviceParameters.getDeviceParameters(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(getResult));

        CompletableFuture<ScadaOperationResult> future =
            testOperationDispatcher.process(ScadaOperation.SELECT, TEST_INDEX, TEST_VALUE, TEST_TYPE);

        assertEquals(ScadaOperationResult.SUCCESS, future.get());
        verify(mockedTranslator).getTranslationInfo(eq(new ScadaInfo(TEST_INDEX, TEST_TYPE)), eq(false));
        verify(mockedGetDeviceParameters)
                .getDeviceParameters(eq(TEST_DEVICE_ID), eq(Collections.singleton(TEST_DATASTREAM_ID)));
        verifyZeroInteractions(mockedSetDeviceParameters);
    }

    @Test
    public void testProcessSelectOperationGetError() throws ExecutionException, InterruptedException {
        GetValue getValue =  new GetValue(TEST_DATASTREAM_ID, null, Status.PROCESSING_ERROR, TEST_AT, TEST_VALUE, null);
        OperationGetDeviceParameters.Result getResult =
                new OperationGetDeviceParameters.Result(Collections.singletonList(getValue));

        when(mockedTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(datastreamInfo);
        when(mockedGetDeviceParameters.getDeviceParameters(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(getResult));

        CompletableFuture<ScadaOperationResult> future =
                testOperationDispatcher.process(ScadaOperation.SELECT, TEST_INDEX, TEST_VALUE, TEST_TYPE);

        assertEquals(ScadaOperationResult.ERROR, future.get());
        verify(mockedTranslator).getTranslationInfo(eq(new ScadaInfo(TEST_INDEX, TEST_TYPE)), eq(false));
        verify(mockedGetDeviceParameters)
                .getDeviceParameters(eq(TEST_DEVICE_ID), eq(Collections.singleton(TEST_DATASTREAM_ID)));
        verifyZeroInteractions(mockedSetDeviceParameters);
    }

    // Should never happen
    @Test
    public void testProcessSelectOperationGetResultWithoutRequestedDatastreamId() throws ExecutionException,
            InterruptedException {
        GetValue getValue =  new GetValue("otherDatastream", null, Status.OK, TEST_AT, TEST_VALUE, null);
        OperationGetDeviceParameters.Result getResult =
                new OperationGetDeviceParameters.Result(Collections.singletonList(getValue));

        when(mockedTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(datastreamInfo);
        when(mockedGetDeviceParameters.getDeviceParameters(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(getResult));

        CompletableFuture<ScadaOperationResult> future =
                testOperationDispatcher.process(ScadaOperation.SELECT, TEST_INDEX, TEST_VALUE, TEST_TYPE);

        assertEquals(ScadaOperationResult.ERROR, future.get());
        verify(mockedTranslator).getTranslationInfo(eq(new ScadaInfo(TEST_INDEX, TEST_TYPE)), eq(false));
        verify(mockedGetDeviceParameters)
                .getDeviceParameters(eq(TEST_DEVICE_ID), eq(Collections.singleton(TEST_DATASTREAM_ID)));
        verifyZeroInteractions(mockedSetDeviceParameters);
    }

    // Should never happen
    @Test
    public void testProcessSelectOperationGetResultEmpty() throws ExecutionException, InterruptedException {
        OperationGetDeviceParameters.Result getResult =
                new OperationGetDeviceParameters.Result(Collections.emptyList());

        when(mockedTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(datastreamInfo);
        when(mockedTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(null);
        when(mockedGetDeviceParameters.getDeviceParameters(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(getResult));

        CompletableFuture<ScadaOperationResult> future =
                testOperationDispatcher.process(ScadaOperation.SELECT, TEST_INDEX, TEST_VALUE, TEST_TYPE);

        assertEquals(ScadaOperationResult.ERROR, future.get());
        verify(mockedTranslator).getTranslationInfo(eq(new ScadaInfo(TEST_INDEX, TEST_TYPE)), eq(false));
        verify(mockedGetDeviceParameters)
                .getDeviceParameters(eq(TEST_DEVICE_ID), eq(Collections.singleton(TEST_DATASTREAM_ID)));
        verifyZeroInteractions(mockedSetDeviceParameters);
    }

    @Test
    public void testProcessDirectOperateOperation() throws ExecutionException, InterruptedException {
        VariableValue setValue = new VariableValue(TEST_DATASTREAM_ID, TEST_VALUE);
        VariableResult variableResult = new VariableResult(TEST_DATASTREAM_ID, null);
        OperationSetDeviceParameters.Result setResult =
                new OperationSetDeviceParameters.Result(OperationSetDeviceParameters.ResultCode.SUCCESSFUL,
                        "", Collections.singletonList(variableResult));

        when(mockedTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(datastreamInfo);
        when(mockedTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(TEST_VALUE);
        when(mockedSetDeviceParameters.setDeviceParameters(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(setResult));

        CompletableFuture<ScadaOperationResult> future =
                testOperationDispatcher.process(ScadaOperation.DIRECT_OPERATE, TEST_INDEX, TEST_VALUE, TEST_TYPE);

        assertEquals(ScadaOperationResult.SUCCESS, future.get());
        verify(mockedTranslator).getTranslationInfo(eq(new ScadaInfo(TEST_INDEX, TEST_TYPE)), eq(false));
        verify(mockedSetDeviceParameters)
                .setDeviceParameters(eq(TEST_DEVICE_ID), eq(Collections.singletonList(setValue)));
        verifyZeroInteractions(mockedGetDeviceParameters);
    }

    @Test
    public void testProcessDirectOperateOperationSetError() throws ExecutionException, InterruptedException {
        VariableValue setValue = new VariableValue(TEST_DATASTREAM_ID, TEST_VALUE);
        VariableResult variableResult = new VariableResult(TEST_DATASTREAM_ID, "error");
        OperationSetDeviceParameters.Result setResult =
                new OperationSetDeviceParameters.Result(OperationSetDeviceParameters.ResultCode.SUCCESSFUL,
                        "", Collections.singletonList(variableResult));

        when(mockedTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(datastreamInfo);
        when(mockedTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(TEST_VALUE);
        when(mockedSetDeviceParameters.setDeviceParameters(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(setResult));

        CompletableFuture<ScadaOperationResult> future =
                testOperationDispatcher.process(ScadaOperation.DIRECT_OPERATE, TEST_INDEX, TEST_VALUE, TEST_TYPE);

        assertEquals(ScadaOperationResult.ERROR, future.get());
        verify(mockedTranslator).getTranslationInfo(eq(new ScadaInfo(TEST_INDEX, TEST_TYPE)), eq(false));
        verify(mockedSetDeviceParameters)
                .setDeviceParameters(eq(TEST_DEVICE_ID), eq(Collections.singletonList(setValue)));
        verifyZeroInteractions(mockedGetDeviceParameters);
    }

    // Should not happen
    @Test
    public void testProcessDirectOperateOperationSetResultWithRequestedDatastreamId() throws ExecutionException,
            InterruptedException {
        VariableValue setValue = new VariableValue(TEST_DATASTREAM_ID, TEST_VALUE);
        VariableResult variableResult = new VariableResult("otherDatastream", null);
        OperationSetDeviceParameters.Result setResult =
                new OperationSetDeviceParameters.Result(OperationSetDeviceParameters.ResultCode.SUCCESSFUL,
                        "", Collections.singletonList(variableResult));

        when(mockedTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(datastreamInfo);
        when(mockedTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(TEST_VALUE);
        when(mockedSetDeviceParameters.setDeviceParameters(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(setResult));

        CompletableFuture<ScadaOperationResult> future =
                testOperationDispatcher.process(ScadaOperation.DIRECT_OPERATE, TEST_INDEX, TEST_VALUE, TEST_TYPE);

        assertEquals(ScadaOperationResult.ERROR, future.get());
        verify(mockedTranslator).getTranslationInfo(eq(new ScadaInfo(TEST_INDEX, TEST_TYPE)), eq(false));
        verify(mockedSetDeviceParameters)
                .setDeviceParameters(eq(TEST_DEVICE_ID), eq(Collections.singletonList(setValue)));
        verifyZeroInteractions(mockedGetDeviceParameters);
    }

    // Should not happen
    @Test
    public void testProcessDirectOperateOperationSetResultEmpty() throws ExecutionException, InterruptedException {
        VariableValue setValue = new VariableValue(TEST_DATASTREAM_ID, TEST_VALUE);
        OperationSetDeviceParameters.Result setResult =
                new OperationSetDeviceParameters.Result(OperationSetDeviceParameters.ResultCode.SUCCESSFUL,
                        "", Collections.emptyList());

        when(mockedTranslator.getTranslationInfo(any(), anyBoolean())).thenReturn(datastreamInfo);
        when(mockedTranslator.transformValue(anyInt(), any(), anyBoolean(), any())).thenReturn(TEST_VALUE);
        when(mockedSetDeviceParameters.setDeviceParameters(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(setResult));

        CompletableFuture<ScadaOperationResult> future =
                testOperationDispatcher.process(ScadaOperation.DIRECT_OPERATE, TEST_INDEX, TEST_VALUE, TEST_TYPE);

        assertEquals(ScadaOperationResult.ERROR, future.get());
        verify(mockedTranslator).getTranslationInfo(eq(new ScadaInfo(TEST_INDEX, TEST_TYPE)), eq(false));
        verify(mockedSetDeviceParameters)
                .setDeviceParameters(eq(TEST_DEVICE_ID), eq(Collections.singletonList(setValue)));
        verifyZeroInteractions(mockedGetDeviceParameters);
    }

    @Test
    public void testProcessIndexNotFound() throws ExecutionException, InterruptedException {
        when(mockedTranslator.getTranslationInfo(eq(new ScadaInfo(TEST_INDEX, TEST_TYPE)), eq(false)))
                .thenThrow(new DataNotFoundException(""));

        CompletableFuture<ScadaOperationResult> future =
                testOperationDispatcher.process(ScadaOperation.SELECT, TEST_INDEX, TEST_VALUE, TEST_TYPE);

        assertEquals(ScadaOperationResult.ERROR, future.get());
    }
}