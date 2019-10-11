package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.domain.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OperationProcessorTemplateTest {

    private static final String TEST_DEVICE_ID_FOR_OPERATION = "testDeviceOperation";
    private static final String TEST_DEVICE_ID_FOR_RESPONSE = "testDeviceResponse";
    private static final String TEST_ID = "testOperationId";
    private static final String[] TEST_PATH = new String[] {"path", "to", "device"};
    private static final String TEST_OPERATION = "testOperation";
    private static final Request TEST_REQUEST =
            new Request(TEST_ID, 0L, TEST_DEVICE_ID_FOR_OPERATION, TEST_PATH, TEST_OPERATION, null);
    private static final TestParams TEST_PARAMS = new TestParams();
    private static final TestResult TEST_RESULT = new TestResult();
    private static final Output TEST_OUTPUT = new Output(OPENGATE_VERSION, null);
    private static final byte[] TEST_PROCESSED_OPERATION = new byte[] { 1, 2, 3, 4 };


    private static class TestParams {}
    private static class TestResult {}

    @SuppressWarnings("unused")
    private static class InnerProcessor {
        TestParams parseParameters(Request request) { return new TestParams(); }
        CompletableFuture<TestResult> processOperation(String deviceIdForOperation, TestParams testParams) {
            return CompletableFuture.completedFuture(new TestResult()); }
        Output translateToOutput(TestResult result, String requestId, String deviceId, String[] path) {
            return new Output(OPENGATE_VERSION, null);
        }
    }

    private static class TestOperationProcessor extends OperationProcessorTemplate<TestParams, TestResult> {
        private final InnerProcessor innerProcessor;

        TestOperationProcessor(Serializer serializer, InnerProcessor innerProcessor) {
            super(serializer);
            this.innerProcessor = innerProcessor;
        }

        @Override
        TestParams parseParameters(Request request) {
            return innerProcessor.parseParameters(request);
        }

        @Override
        CompletableFuture<TestResult> processOperation(String deviceIdForOperations, TestParams params) {
            return innerProcessor.processOperation(deviceIdForOperations, params);
        }

        @Override
        Output translateToOutput(TestResult result, String requestId, String deviceId, String[] path) {
            return innerProcessor.translateToOutput(result, requestId, deviceId, path);
        }
    }

    @Mock
    private Serializer mockedSerializer;
    @Mock
    private InnerProcessor mockedInnerProcessor;
    @InjectMocks
    private TestOperationProcessor testProcessor;

    @Captor
    private ArgumentCaptor<Output> outputCaptor;

    @Test
    public void testProcess() throws IOException, ExecutionException, InterruptedException {
        when(mockedInnerProcessor.parseParameters(any(Request.class))).thenReturn(TEST_PARAMS);
        when(mockedInnerProcessor.processOperation(anyString(), any(TestParams.class)))
                .thenReturn(CompletableFuture.completedFuture(TEST_RESULT));
        when(mockedInnerProcessor.translateToOutput(any(TestResult.class), anyString(), anyString(), any(String[].class)))
                .thenReturn(TEST_OUTPUT);
        when(mockedSerializer.serialize(any())).thenReturn(TEST_PROCESSED_OPERATION);

        CompletableFuture<byte[]> future =
                testProcessor.process(TEST_DEVICE_ID_FOR_OPERATION, TEST_DEVICE_ID_FOR_RESPONSE, TEST_REQUEST);
        byte[] processedOperation = future.get();

        assertArrayEquals(TEST_PROCESSED_OPERATION, processedOperation);
        verify(mockedInnerProcessor).parseParameters(eq(TEST_REQUEST));
        verify(mockedInnerProcessor).processOperation(eq(TEST_DEVICE_ID_FOR_OPERATION), eq(TEST_PARAMS));
        verify(mockedInnerProcessor).translateToOutput(eq(TEST_RESULT), eq(TEST_ID), eq(TEST_DEVICE_ID_FOR_RESPONSE), aryEq(TEST_PATH));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }

    @Test
    public void testProcessErrorProcessingOperation() throws IOException, ExecutionException, InterruptedException {
        when(mockedInnerProcessor.parseParameters(any(Request.class))).thenReturn(TEST_PARAMS);
        when(mockedInnerProcessor.processOperation(anyString(), any(TestParams.class))).thenReturn(null);
        when(mockedSerializer.serialize(any())).thenReturn(TEST_PROCESSED_OPERATION);

        CompletableFuture<byte[]> future =
                testProcessor.process(TEST_DEVICE_ID_FOR_OPERATION, TEST_DEVICE_ID_FOR_RESPONSE, TEST_REQUEST);
        byte[] processedOperation = future.get();

        assertArrayEquals(TEST_PROCESSED_OPERATION, processedOperation);
        verify(mockedInnerProcessor).parseParameters(eq(TEST_REQUEST));
        verify(mockedInnerProcessor).processOperation(eq(TEST_DEVICE_ID_FOR_OPERATION), eq(TEST_PARAMS));
        verify(mockedInnerProcessor, never())
                .translateToOutput(any(TestResult.class),anyString(), anyString(), any(String[].class));
        verify(mockedSerializer).serialize(outputCaptor.capture());
        Output capturedOutput = outputCaptor.getValue();
        assertEquals(OPENGATE_VERSION, capturedOutput.getVersion());
        OutputOperation outputOperation = capturedOutput.getOperation();
        assertNotNull(outputOperation);
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID_FOR_RESPONSE, response.getDeviceId());
        assertEquals(TEST_OPERATION, response.getName());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(OperationResultCode.NOT_SUPPORTED, response.getResultCode());
        assertNotNull(response.getResultDescription());
    }

    @Test
    public void testProcessExceptionTranslatingToOutput() throws IOException, ExecutionException, InterruptedException {
        when(mockedInnerProcessor.parseParameters(any(Request.class))).thenReturn(TEST_PARAMS);
        when(mockedInnerProcessor.processOperation(anyString(), any(TestParams.class)))
                .thenReturn(CompletableFuture.completedFuture(TEST_RESULT));
        when(mockedInnerProcessor.translateToOutput(any(TestResult.class), anyString(), anyString(), any(String[].class)))
                .thenThrow(new RuntimeException());
        when(mockedSerializer.serialize(any())).thenReturn(TEST_PROCESSED_OPERATION);

        CompletableFuture<byte[]> future =
                testProcessor.process(TEST_DEVICE_ID_FOR_OPERATION, TEST_DEVICE_ID_FOR_RESPONSE, TEST_REQUEST);
        byte[] processedOperation = future.get();

        assertArrayEquals(TEST_PROCESSED_OPERATION, processedOperation);
        verify(mockedInnerProcessor).parseParameters(eq(TEST_REQUEST));
        verify(mockedInnerProcessor).processOperation(eq(TEST_DEVICE_ID_FOR_OPERATION), eq(TEST_PARAMS));
        verify(mockedInnerProcessor)
                .translateToOutput(eq(TEST_RESULT), eq(TEST_ID), eq(TEST_DEVICE_ID_FOR_RESPONSE), aryEq(TEST_PATH));
        verify(mockedSerializer).serialize(outputCaptor.capture());
        Output capturedOutput = outputCaptor.getValue();
        assertEquals(OPENGATE_VERSION, capturedOutput.getVersion());
        OutputOperation outputOperation = capturedOutput.getOperation();
        assertNotNull(outputOperation);
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID_FOR_RESPONSE, response.getDeviceId());
        assertEquals(TEST_OPERATION, response.getName());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(OperationResultCode.ERROR_PROCESSING, response.getResultCode());
        assertNotNull(response.getResultDescription());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step errorStep = steps.get(0);
        assertEquals(TEST_OPERATION, errorStep.getName());
        assertEquals(StepResultCode.ERROR, errorStep.getResult());
        assertNotNull(errorStep.getDescription());
    }

    @Test
    public void testProcessSerializerIOException() throws IOException {
        when(mockedInnerProcessor.parseParameters(any(Request.class))).thenReturn(TEST_PARAMS);
        when(mockedInnerProcessor.processOperation(anyString(), any(TestParams.class)))
                .thenReturn(CompletableFuture.completedFuture(TEST_RESULT));
        when(mockedInnerProcessor.translateToOutput(any(TestResult.class), anyString(), anyString(), any(String[].class)))
                .thenReturn(TEST_OUTPUT);
        when(mockedSerializer.serialize(any())).thenThrow(new IOException());

        CompletableFuture<byte[]> future =
                testProcessor.process(TEST_DEVICE_ID_FOR_OPERATION, TEST_DEVICE_ID_FOR_RESPONSE, TEST_REQUEST);

        assertNull(future);
        verify(mockedInnerProcessor).parseParameters(eq(TEST_REQUEST));
        verify(mockedInnerProcessor).processOperation(eq(TEST_DEVICE_ID_FOR_OPERATION), eq(TEST_PARAMS));
        verify(mockedInnerProcessor).translateToOutput(eq(TEST_RESULT), eq(TEST_ID), eq(TEST_DEVICE_ID_FOR_RESPONSE), aryEq(TEST_PATH));
        verify(mockedSerializer).serialize(eq(TEST_OUTPUT));
    }
}