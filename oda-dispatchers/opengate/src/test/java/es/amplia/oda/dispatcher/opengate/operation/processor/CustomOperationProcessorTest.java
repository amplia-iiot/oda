package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.utils.ServiceLocator;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.custom.RequestCustomOperation;
import es.amplia.oda.operation.api.CustomOperation;
import es.amplia.oda.operation.api.engine.OperationEngineProxy;
import lombok.Value;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CustomOperationProcessorTest {

    @Value
    private static class TestObject {
        private String test;
    }

    private static final String TEST_ID = "testOperationId";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] {"path", "to", "device"};
    private static final String TEST_CUSTOM_OPERATION_NAME = "customOperation";
    private static final String TEST_STRING_VALUE = "test";
    private static final Map<String, Object> TEST_STRING_PARAM =
            new HashMap<>();
    private static final RequestCustomOperation TEST_REQUEST =
            new RequestCustomOperation(TEST_STRING_PARAM);
    private static final String TEST_INVALID_VALUE = null;
    private static final Map<String, Object> TEST_INVALID_PARAM =
            new HashMap<>();
    private static final RequestCustomOperation TEST_INVALID_REQUEST =
            new RequestCustomOperation(TEST_INVALID_PARAM);

    private static final String TEST_DESCRIPTION = "description of the operation result";
    private static final CustomOperation.Result TEST_RESULT =
            new CustomOperation.Result(CustomOperation.Status.SUCCESSFUL, TEST_DESCRIPTION, null);


    @Mock
    private ServiceLocator<CustomOperation> mockedOperationServiceLocator;
    @Mock
    private OperationEngineProxy mockedEngine;
    @InjectMocks
    private CustomOperationProcessor testProcessor;

    @Mock
    private CustomOperation mockedOperation1;
    @Mock
    private CustomOperation mockedOperation2;

    @Before
    public void setUp() {
        TEST_STRING_PARAM.put("string", TEST_STRING_VALUE);
        TEST_INVALID_PARAM.put("invalidParam", TEST_INVALID_VALUE);
    }

    @Test
    public void testParseParameters() {
        TEST_REQUEST.setName(TEST_CUSTOM_OPERATION_NAME);

        Map<String, Object> params = testProcessor.parseParameters(TEST_REQUEST);

        assertNotNull(params);
        assertEquals(1, params.size());
        assertEquals(TEST_STRING_VALUE, params.get("string"));
        assertEquals(TEST_CUSTOM_OPERATION_NAME, Whitebox.getInternalState(testProcessor, "customOperationName"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersInvalidParam() {
        testProcessor.parseParameters(TEST_INVALID_REQUEST);

        fail("Illegal Argument Exception is thrown");
    }

    @Test
    public void testProcessOperation() {
        Map<String, Object> params = new HashMap<>();
        params.put("testParam", "test");

        Whitebox.setInternalState(testProcessor, "customOperationName", TEST_CUSTOM_OPERATION_NAME);

        when(mockedOperationServiceLocator.findAll()).thenReturn(Arrays.asList(mockedOperation1, mockedOperation2));
        when(mockedOperation1.getOperationSatisfied()).thenReturn("otherOperation");
        when(mockedOperation2.getOperationSatisfied()).thenReturn(TEST_CUSTOM_OPERATION_NAME);
        when(mockedEngine.engine(anyString(), anyString(), anyString(), any(), any())).thenReturn(null);

        testProcessor.processOperation(TEST_DEVICE_ID, TEST_ID, params);

        verify(mockedOperationServiceLocator).findAll();
        verify(mockedOperation1).getOperationSatisfied();
        verify(mockedOperation2).getOperationSatisfied();
        verify(mockedOperation2).execute(eq(TEST_DEVICE_ID), eq(TEST_ID), eq(params));
        verify(mockedOperation1, never()).execute(anyString(), anyString(), any());
    }

    @Test
    public void testProcessOperationNotSupported() throws InterruptedException, ExecutionException {
        Whitebox.setInternalState(testProcessor, "customOperationName", TEST_CUSTOM_OPERATION_NAME);

        when(mockedOperationServiceLocator.findAll()).thenReturn(Arrays.asList(mockedOperation1, mockedOperation2));
        when(mockedOperation1.getOperationSatisfied()).thenReturn("otherOperation");
        when(mockedOperation2.getOperationSatisfied()).thenReturn("otherOperation2");
        when(mockedEngine.engine(anyString(), anyString(), anyString(), any(), any())).thenReturn(null);

        assertNull(testProcessor.processOperation(TEST_DEVICE_ID, TEST_ID, null).get());

        verify(mockedOperationServiceLocator).findAll();
        verify(mockedOperation1).getOperationSatisfied();
        verify(mockedOperation2).getOperationSatisfied();
        verify(mockedOperation1, never()).execute(anyString(), anyString(), any());
        verify(mockedOperation2, never()).execute(anyString(), anyString(), any());
    }

    @Test
    public void testProcessOperationNotCustomOperationRegistered() throws InterruptedException, ExecutionException {
        Whitebox.setInternalState(testProcessor, "customOperationName", TEST_CUSTOM_OPERATION_NAME);

        when(mockedOperationServiceLocator.findAll()).thenReturn(Collections.emptyList());
        when(mockedEngine.engine(anyString(), anyString(), anyString(), any(), any())).thenReturn(null);

        assertNull(testProcessor.processOperation(TEST_DEVICE_ID, TEST_ID, null).get());

        verify(mockedOperationServiceLocator).findAll();
    }

    @Test
    public void testTranslateToOutputSuccessfulResultWithoutSteps() {
        Whitebox.setInternalState(testProcessor, "customOperationName", TEST_CUSTOM_OPERATION_NAME);

        Output output = testProcessor.translateToOutput(TEST_RESULT, TEST_ID, TEST_DEVICE_ID, TEST_PATH);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID, response.getDeviceId());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(TEST_CUSTOM_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(TEST_CUSTOM_OPERATION_NAME, step.getName());
        assertEquals(StepResultCode.SUCCESSFUL, step.getResult());
    }

    @Test
    public void testTranslateToOutputErrorResultWithoutSteps() {
        CustomOperation.Result errorResult =
                new CustomOperation.Result(CustomOperation.Status.ERROR_PROCESSING, TEST_DESCRIPTION, null);

        Whitebox.setInternalState(testProcessor, "customOperationName", TEST_CUSTOM_OPERATION_NAME);

        Output output = testProcessor.translateToOutput(errorResult, TEST_ID, TEST_DEVICE_ID, TEST_PATH);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID, response.getDeviceId());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(TEST_CUSTOM_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.ERROR_PROCESSING, response.getResultCode());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertTrue(steps.isEmpty());
    }

    @Test
    public void testTranslateToOutputMultipleSteps() {
        List<Object> stepResponse = Collections.singletonList(new TestObject("test"));
        CustomOperation.Step completeStep = new CustomOperation.Step("step1", CustomOperation.StepStatus.SUCCESSFUL,
                "step description 1", System.currentTimeMillis(), stepResponse);
        CustomOperation.Step skippedStep = new CustomOperation.Step("step2", CustomOperation.StepStatus.SKIPPED,
                "step description 2", System.currentTimeMillis(), null);
        CustomOperation.Step notExecutedStep = new CustomOperation.Step("step3", CustomOperation.StepStatus.NOT_EXECUTED,
                "step description 3", System.currentTimeMillis(), null);
        CustomOperation.Step errorStep = new CustomOperation.Step("step4", CustomOperation.StepStatus.ERROR,
                "step description 4", System.currentTimeMillis(), null);
        CustomOperation.Result resultWithSteps =
                new CustomOperation.Result(CustomOperation.Status.SUCCESSFUL, TEST_DESCRIPTION,
                        Arrays.asList(completeStep, skippedStep, notExecutedStep, errorStep));

        Whitebox.setInternalState(testProcessor, "customOperationName", TEST_CUSTOM_OPERATION_NAME);

        Output output = testProcessor.translateToOutput(resultWithSteps, TEST_ID, TEST_DEVICE_ID, TEST_PATH);

        assertEquals(OPENGATE_VERSION, output.getVersion());
        OutputOperation outputOperation = output.getOperation();
        Response response = outputOperation.getResponse();
        assertEquals(TEST_ID, response.getId());
        assertEquals(TEST_DEVICE_ID, response.getDeviceId());
        assertArrayEquals(TEST_PATH, response.getPath());
        assertEquals(TEST_CUSTOM_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(4, steps.size());
        Step step = steps.get(0);
        assertEquals(StepResultCode.SUCCESSFUL, step.getResult());
        assertEquals("step1", step.getName());
        assertEquals("step description 1", step.getDescription());
        assertEquals(stepResponse, step.getResponse());
        step = steps.get(1);
        assertEquals(StepResultCode.SKIPPED, step.getResult());
        assertEquals("step2", step.getName());
        assertEquals("step description 2", step.getDescription());
        assertNull(step.getResponse());
        step = steps.get(2);
        assertEquals(StepResultCode.NOT_EXECUTED, step.getResult());
        assertEquals("step3", step.getName());
        assertEquals("step description 3", step.getDescription());
        assertNull(step.getResponse());
        step = steps.get(3);
        assertEquals(StepResultCode.ERROR, step.getResult());
        assertEquals("step4", step.getName());
        assertEquals("step description 4", step.getDescription());
        assertNull(step.getResponse());
    }
}