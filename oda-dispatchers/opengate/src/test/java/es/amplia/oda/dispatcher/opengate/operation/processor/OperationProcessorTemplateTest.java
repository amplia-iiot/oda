package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;

import es.amplia.oda.dispatcher.opengate.domain.general.RequestGeneralOperation;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OperationProcessorTemplateTest {

    private static final String TEST_DEVICE_ID_FOR_OPERATION = "testDeviceOperation";
    private static final String TEST_DEVICE_ID_FOR_RESPONSE = "testDeviceResponse";
    private static final String TEST_ID = "testOperationId";
    private static final String[] TEST_PATH = new String[] {"path", "to", "device"};
    private static final String TEST_OPERATION = "testOperation";
    private static final Request TEST_REQUEST = new RequestGeneralOperation();
    private static final TestParams TEST_PARAMS = new TestParams();
    private static final TestResult TEST_RESULT = new TestResult();
    private static final Output TEST_OUTPUT = new Output(OPENGATE_VERSION, null);


    private static class TestParams {}
    private static class TestResult {}

    @SuppressWarnings("unused")
    private static class InnerProcessor {
        TestParams parseParameters(Request request) { return TEST_PARAMS; }
        CompletableFuture<TestResult> processOperation(String deviceIdForOperation, TestParams testParams) {
            return CompletableFuture.completedFuture(TEST_RESULT); }
        Output translateToOutput(TestResult result, String requestId, String deviceId, String[] path) {
            return TEST_OUTPUT;
        }
    }

    private static class TestOperationProcessor extends OperationProcessorTemplate<TestParams, TestResult> {
        private final InnerProcessor innerProcessor;

        TestOperationProcessor(InnerProcessor innerProcessor) {
            this.innerProcessor = innerProcessor;
        }

        @Override
        TestParams parseParameters(Request request) {
            return innerProcessor.parseParameters(request);
        }

        @Override
        CompletableFuture<TestResult> processOperation(String deviceIdForOperations, String operationId, TestParams params) {
            return innerProcessor.processOperation(deviceIdForOperations, params);
        }

        @Override
        Output translateToOutput(TestResult result, String requestId, String deviceId, String[] path) {
            return innerProcessor.translateToOutput(result, requestId, deviceId, path);
        }
    }

    @Spy
    private InnerProcessor spiedInnerProcessor = new InnerProcessor();
    @InjectMocks
    private TestOperationProcessor testProcessor;

    @Test
    public void testProcess() throws ExecutionException, InterruptedException {
        TEST_REQUEST.setPath(TEST_PATH);
        TEST_REQUEST.setId(TEST_ID);

        CompletableFuture<Output> future =
                testProcessor.process(TEST_DEVICE_ID_FOR_OPERATION, TEST_DEVICE_ID_FOR_RESPONSE, TEST_REQUEST, 90);
        Output output = future.get();

        assertEquals(TEST_OUTPUT, output);
        verify(spiedInnerProcessor).parseParameters(eq(TEST_REQUEST));
        verify(spiedInnerProcessor).processOperation(eq(TEST_DEVICE_ID_FOR_OPERATION), eq(TEST_PARAMS));
        verify(spiedInnerProcessor)
                .translateToOutput(eq(TEST_RESULT), eq(TEST_ID), eq(TEST_DEVICE_ID_FOR_RESPONSE), aryEq(TEST_PATH));
    }

    private static class NoOperationInnerProcessor extends InnerProcessor {
        @Override
        CompletableFuture<TestResult> processOperation(String deviceIdForOperation, TestParams testParams) {
            return null;
        }
    }

    @Test
    public void testProcessNoOperation() throws ExecutionException, InterruptedException {
        TEST_REQUEST.setId(TEST_ID);
        TEST_REQUEST.setName(TEST_OPERATION);
        TEST_REQUEST.setPath(TEST_PATH);

        TestOperationProcessor noOperationProcessor = new TestOperationProcessor(new NoOperationInnerProcessor());

        CompletableFuture<Output> future =
                noOperationProcessor.process(TEST_DEVICE_ID_FOR_OPERATION, TEST_DEVICE_ID_FOR_RESPONSE, TEST_REQUEST, 90);
        Output output = future.get();

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
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
    public void testProcessErrorProcessingOperation() throws ExecutionException, InterruptedException {
        TEST_REQUEST.setId(TEST_ID);
        TEST_REQUEST.setName(TEST_OPERATION);
        TEST_REQUEST.setPath(TEST_PATH);

        when(spiedInnerProcessor.processOperation(anyString(), any(TestParams.class))).thenReturn(null);

        CompletableFuture<Output> future =
                testProcessor.process(TEST_DEVICE_ID_FOR_OPERATION, TEST_DEVICE_ID_FOR_RESPONSE, TEST_REQUEST, 90);
        Output output = future.get();

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
        assertNotNull(outputOperation);
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID_FOR_RESPONSE, response.getDeviceId());
        assertEquals(TEST_OPERATION, response.getName());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(OperationResultCode.NOT_SUPPORTED, response.getResultCode());
        assertNotNull(response.getResultDescription());
        verify(spiedInnerProcessor).parseParameters(eq(TEST_REQUEST));
        verify(spiedInnerProcessor).processOperation(eq(TEST_DEVICE_ID_FOR_OPERATION), eq(TEST_PARAMS));
        verify(spiedInnerProcessor, never())
                .translateToOutput(any(TestResult.class),anyString(), anyString(), any(String[].class));
    }

    private static class FailInnerProcessor extends InnerProcessor {
        @Override
        Output translateToOutput(TestResult result, String requestId, String deviceId, String[] path) {
            throw new RuntimeException("Error");
        }
    }

    @Test
    public void testProcessExceptionTranslatingToOutput() throws ExecutionException, InterruptedException {
        TEST_REQUEST.setId(TEST_ID);
        TEST_REQUEST.setName(TEST_OPERATION);
        TEST_REQUEST.setPath(TEST_PATH);

        TestOperationProcessor failOperationProcessor = new TestOperationProcessor(new FailInnerProcessor());

        CompletableFuture<Output> future =
                failOperationProcessor.process(TEST_DEVICE_ID_FOR_OPERATION, TEST_DEVICE_ID_FOR_RESPONSE, TEST_REQUEST, 90);
        Output output = future.get();

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
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
}