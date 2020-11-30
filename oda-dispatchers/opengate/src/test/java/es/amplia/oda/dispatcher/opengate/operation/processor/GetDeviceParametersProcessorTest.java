package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.get.ParameterGetOperation;
import es.amplia.oda.dispatcher.opengate.domain.get.RequestGetOperation;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationGetDeviceParameters.*;
import static es.amplia.oda.dispatcher.opengate.operation.processor.GetDeviceParametersProcessor.*;
import static es.amplia.oda.operation.api.OperationGetDeviceParameters.Status.NOT_FOUND;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GetDeviceParametersProcessorTest {

    private static final String TEST_ID = "testOperationId";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] {"path", "to", "device"};
    private static final String TEST_DATASTREAM = "testDatastream";
    private static final long TEST_AT = System.currentTimeMillis();
    private static final ParameterGetOperation TEST_PARAMETER = new ParameterGetOperation(Collections.singletonList(TEST_DATASTREAM));
    private static final RequestGetOperation TEST_REQUEST = new RequestGetOperation(TEST_PARAMETER);
    private static final Set<String> TEST_PARAMS = Collections.singleton(TEST_DATASTREAM);
    private static final String TEST_VALUE = "test";
    private static final GetValue TEST_GET_VALUE = new GetValue(TEST_DATASTREAM, Status.OK, TEST_AT, TEST_VALUE, null);
    private static final Result TEST_RESULT = new Result(Collections.singletonList(TEST_GET_VALUE));


    @Mock
    private OperationGetDeviceParameters mockedGetDeviceParameters;
    @InjectMocks
    private GetDeviceParametersProcessor testProcessor;


    @Test
    public void testParseParameters() {
        Set<String> params = testProcessor.parseParameters(TEST_REQUEST);

        assertNotNull(params);
        assertEquals(1, params.size());
        assertTrue(params.contains(TEST_DATASTREAM));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoParams() {
        RequestGetOperation invalidRequest = new RequestGetOperation(null);

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoVariableListParam() {
        RequestGetOperation invalidRequest = new RequestGetOperation(new ParameterGetOperation(null));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersEmptyVariableListParam() {
        RequestGetOperation invalidRequest = new RequestGetOperation(new ParameterGetOperation(Collections.emptyList()));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNullElementVariableListParam() {
        RequestGetOperation invalidRequest = new RequestGetOperation(new ParameterGetOperation(Collections.singletonList(null)));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test
    public void testProcessOperation() {
        testProcessor.processOperation(TEST_DEVICE_ID, TEST_PARAMS);

        verify(mockedGetDeviceParameters).getDeviceParameters(eq(TEST_DEVICE_ID), eq(TEST_PARAMS));
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
        assertEquals(GET_DEVICE_PARAMETERS_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(GET_DEVICE_PARAMETERS_OPERATION_NAME, step.getName());
        assertEquals(StepResultCode.SUCCESSFUL, step.getResult());
        List<Object> stepResponse = step.getResponse();
        assertNotNull(stepResponse);
        assertEquals(1, stepResponse.size());
        OutputVariable outputVariable = (OutputVariable) stepResponse.get(0);
        assertEquals(SUCCESS_RESULT, outputVariable.getResultCode());
        assertEquals(TEST_DATASTREAM, outputVariable.getVariableName());
        assertEquals(TEST_VALUE, outputVariable.getVariableValue());
    }

    @Test
    public void testTranslateToOutputErrorResults() {
        GetValue errorGetValue = new GetValue("errorDatastream", Status.PROCESSING_ERROR, TEST_AT, null, "error description");
        GetValue notFoundGetValue = new GetValue("notFoundDatastream", NOT_FOUND, TEST_AT, null, "error description");
        Result resultWithErrors = new Result(Arrays.asList(errorGetValue, notFoundGetValue));

        Output output = testProcessor.translateToOutput(resultWithErrors, TEST_ID, TEST_DEVICE_ID, TEST_PATH);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID, response.getDeviceId());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(GET_DEVICE_PARAMETERS_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(GET_DEVICE_PARAMETERS_OPERATION_NAME, step.getName());
        assertEquals(StepResultCode.SUCCESSFUL, step.getResult());
        List<Object> stepResponse = step.getResponse();
        assertNotNull(stepResponse);
        assertEquals(2, stepResponse.size());
        OutputVariable outputVariable = (OutputVariable) stepResponse.get(0);
        assertEquals(ERROR_RESULT, outputVariable.getResultCode());
        assertEquals("errorDatastream", outputVariable.getVariableName());
        assertNotNull(outputVariable.getResultDescription());
        outputVariable = (OutputVariable) stepResponse.get(1);
        assertEquals("NON_EXISTENT", outputVariable.getResultCode());
        assertEquals("notFoundDatastream", outputVariable.getVariableName());
        assertNotNull(outputVariable.getResultDescription());
    }
}