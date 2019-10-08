package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationSetDeviceParameters.*;
import static es.amplia.oda.dispatcher.opengate.operation.processor.SetDeviceParametersProcessor.*;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SetDeviceParametersProcessorTest {

    private static final String TEST_ID = "testOperationId";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] {"path", "to", "device"};
    private static final String TEST_DATASTREAM = "testDatastream";
    private static final String TEST_VALUE = "test";
    private static final Map<String, String> TEST_SET = new HashMap<>();
    static {
        TEST_SET.put("variableName", TEST_DATASTREAM);
        TEST_SET.put("variableValue", TEST_VALUE);
    }
    private static final ValueObject TEST_VALUE_OBJECT =
            new ValueObject(null, null, null, Collections.singletonList(TEST_SET));
    private static final Parameter TEST_PARAMETER = new Parameter("variableList", "array", TEST_VALUE_OBJECT);
    private static final Request TEST_REQUEST =
            new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, SET_DEVICE_PARAMETERS_OPERATION_NAME,
                    Collections.singletonList(TEST_PARAMETER));
    private static final VariableValue TEST_VARIABLE_VALUE = new VariableValue(TEST_DATASTREAM, TEST_VALUE);
    private static final List<VariableValue> TEST_VARIABLE_VALUES = Collections.singletonList(TEST_VARIABLE_VALUE);
    private static final VariableResult TEST_VARIABLE_RESULT = new VariableResult(TEST_DATASTREAM, null);
    private static final Result TEST_RESULT = new Result(ResultCode.SUCCESSFUL, "",
            Collections.singletonList(TEST_VARIABLE_RESULT));



    @Mock
    private OperationSetDeviceParameters mockedSetDeviceParameters;
    @InjectMocks
    private SetDeviceParametersProcessor testProcessor;


    @Test
    public void testParseParameters() {
        List<VariableValue> variableValues = testProcessor.parseParameters(TEST_REQUEST);

        assertNotNull(variableValues);
        assertEquals(1, variableValues.size());
        VariableValue variableValue = variableValues.get(0);
        assertEquals(TEST_DATASTREAM, variableValue.getIdentifier());
        assertEquals(TEST_VALUE, variableValue.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoParams() {
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH,
                SET_DEVICE_PARAMETERS_OPERATION_NAME, null);

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoOneSizeParam() {
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH,
                SET_DEVICE_PARAMETERS_OPERATION_NAME, Collections.emptyList());

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoVariableListParam() {
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH,
                SET_DEVICE_PARAMETERS_OPERATION_NAME,
                Collections.singletonList(
                        new Parameter("otherParam", "string", new ValueObject("test", null, null, null))));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersIncorrectTypeVariableListParam() {
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH,
                SET_DEVICE_PARAMETERS_OPERATION_NAME,
                Collections.singletonList(
                        new Parameter("variableList", "string", new ValueObject("test", null, null, null))));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersEmptyVariableListParam() {
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH,
                SET_DEVICE_PARAMETERS_OPERATION_NAME,
                Collections.singletonList(
                        new Parameter("variableList", "string", new ValueObject(null, null, null,
                                Collections.singletonList(new HashMap<>())))));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test
    public void testProcessOperation() {
        testProcessor.processOperation(TEST_DEVICE_ID, Collections.singletonList(TEST_VARIABLE_VALUE));

        verify(mockedSetDeviceParameters).setDeviceParameters(eq(TEST_DEVICE_ID), eq(TEST_VARIABLE_VALUES));
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
        assertEquals(SET_DEVICE_PARAMETERS_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(SET_DEVICE_PARAMETERS_OPERATION_NAME, step.getName());
        assertEquals(StepResultCode.SUCCESSFUL, step.getResult());
        List<Object> stepResponse = step.getResponse();
        assertNotNull(stepResponse);
        assertEquals(1, stepResponse.size());
        OutputVariable outputVariable = (OutputVariable) stepResponse.get(0);
        assertNotNull(outputVariable);
        assertEquals(TEST_DATASTREAM, outputVariable.getVariableName());
        assertEquals(SUCCESS_RESULT, outputVariable.getResultCode());
    }

    @Test
    public void testTranslateToOutputWithErrors() {
        VariableResult okResult = new VariableResult(TEST_DATASTREAM, null);
        VariableResult errorResult = new VariableResult(TEST_DATASTREAM, "ERROR");
        Result TEST_RESULT = new Result(ResultCode.SUCCESSFUL, "", Arrays.asList(okResult, errorResult));

        Output output = testProcessor.translateToOutput(TEST_RESULT, TEST_ID, TEST_DEVICE_ID, TEST_PATH);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID, response.getDeviceId());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(SET_DEVICE_PARAMETERS_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(SET_DEVICE_PARAMETERS_OPERATION_NAME, step.getName());
        assertEquals(StepResultCode.SUCCESSFUL, step.getResult());
        List<Object> stepResponse = step.getResponse();
        assertNotNull(stepResponse);
        assertEquals(2, stepResponse.size());
        OutputVariable outputVariable = (OutputVariable) stepResponse.get(0);
        assertNotNull(outputVariable);
        assertEquals(TEST_DATASTREAM, outputVariable.getVariableName());
        assertEquals(SUCCESS_RESULT, outputVariable.getResultCode());
        outputVariable = (OutputVariable) stepResponse.get(1);
        assertNotNull(outputVariable);
        assertEquals(TEST_DATASTREAM, outputVariable.getVariableName());
        assertEquals(ERROR_RESULT, outputVariable.getResultCode());
        assertNotNull(outputVariable.getResultDescription());
    }

    @Test
    public void testTranslateToOutputErrorInParam() {
        Result TEST_RESULT = new Result(ResultCode.ERROR_IN_PARAM, "", null);

        Output output = testProcessor.translateToOutput(TEST_RESULT, TEST_ID, TEST_DEVICE_ID, TEST_PATH);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID, response.getDeviceId());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(SET_DEVICE_PARAMETERS_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.ERROR_IN_PARAM, response.getResultCode());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(SET_DEVICE_PARAMETERS_OPERATION_NAME, step.getName());
        assertEquals(StepResultCode.ERROR, step.getResult());
        assertNotNull(step.getDescription());
    }
}