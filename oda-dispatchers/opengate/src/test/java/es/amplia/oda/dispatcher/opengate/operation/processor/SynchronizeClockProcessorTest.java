package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ParameterSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.RequestSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.ValueSetting;
import es.amplia.oda.operation.api.OperationSynchronizeClock;
import es.amplia.oda.operation.api.OperationSynchronizeClock.Result;
import es.amplia.oda.operation.api.OperationSynchronizeClock.ResultCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.dispatcher.opengate.operation.processor.SynchronizeClockProcessor.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SynchronizeClockProcessorTest {

    private static final String TEST_ID = "testOperationId";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] {"path", "to", "device"};
    private static final String TEST_SOURCE = "testSource";
    private static final ParameterSetOrConfigureOperation TEST_PARAMETER =
            new ParameterSetOrConfigureOperation(Collections.singletonList(new ValueSetting("source", "sync")));
    private static final RequestSetOrConfigureOperation TEST_REQUEST =
            new RequestSetOrConfigureOperation(TEST_PARAMETER);
    private static final String TEST_RESULT_DESCRIPTION = "result description";
    private static final Result TEST_RESULT = new Result(ResultCode.SUCCESSFUL, TEST_RESULT_DESCRIPTION);


    @Mock
    private OperationSynchronizeClock mockedSynchronizeClock;
    @InjectMocks
    private SynchronizeClockProcessor testProcessor;


    @Test
    public void testParseParameters() {
        String source = testProcessor.parseParameters(TEST_REQUEST);

        assertEquals("sync", source);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNullParams() {
        RequestSetOrConfigureOperation nullParamsRequest = new RequestSetOrConfigureOperation(null);

        assertNull(testProcessor.parseParameters(nullParamsRequest));
    }

    @Test
    public void testParseParametersEmptyParams() {
        RequestSetOrConfigureOperation emptyParamsRequest = new RequestSetOrConfigureOperation(new ParameterSetOrConfigureOperation(null));

        assertNull(testProcessor.parseParameters(emptyParamsRequest));
    }

    @Test
    public void testProcessOperation() {
        testProcessor.processOperation(TEST_DEVICE_ID, TEST_ID, TEST_SOURCE);

        verify(mockedSynchronizeClock).synchronizeClock(eq(TEST_DEVICE_ID), eq(TEST_SOURCE));
    }

    @Test
    public void testTranslateToOutput() {
        Output output = testProcessor.translateToOutput(TEST_RESULT, TEST_ID, TEST_DEVICE_ID, TEST_PATH);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID, response.getDeviceId());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(SYNCHRONIZE_CLOCK_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        assertEquals(TEST_RESULT_DESCRIPTION, response.getResultDescription());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(SYNCHRONIZE_CLOCK_OPERATION_NAME, step.getName());
        assertEquals(StepResultCode.SUCCESSFUL, step.getResult());
        assertEquals(TEST_RESULT_DESCRIPTION, step.getDescription());
    }

    @Test
    public void testTranslateToOutputResultWithError() {
        String errorDescription = "Error description";
        Result errorResult = new Result(ResultCode.ERROR_PROCESSING, errorDescription);

        Output output = testProcessor.translateToOutput(errorResult, TEST_ID, TEST_DEVICE_ID, TEST_PATH);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID, response.getDeviceId());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(SYNCHRONIZE_CLOCK_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.ERROR_PROCESSING, response.getResultCode());
        assertEquals(errorDescription, response.getResultDescription());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(SYNCHRONIZE_CLOCK_OPERATION_NAME, step.getName());
        assertEquals(StepResultCode.ERROR, step.getResult());
        assertEquals(errorDescription, step.getDescription());
    }
}