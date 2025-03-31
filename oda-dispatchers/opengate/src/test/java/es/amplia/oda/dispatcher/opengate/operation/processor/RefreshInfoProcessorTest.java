package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.general.RequestGeneralOperation;
import es.amplia.oda.operation.api.OperationRefreshInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.dispatcher.opengate.operation.processor.OperationProcessorTemplate.SUCCESS_RESULT;
import static es.amplia.oda.dispatcher.opengate.operation.processor.RefreshInfoProcessor.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RefreshInfoProcessorTest {

    private static final String TEST_ID = "testOperationId";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] {"path", "to", "device"};
    private static final RequestGeneralOperation TEST_REQUEST = new RequestGeneralOperation();
    private static final String TEST_DATASTREAM = "testDatastream";
    private static final OperationRefreshInfo.RefreshInfoValue TEST_VALUE = new OperationRefreshInfo.RefreshInfoValue(TEST_DATASTREAM, null, OperationRefreshInfo.Status.OK, System.currentTimeMillis(), "Hello", null);
    private static final Map<String, List<OperationRefreshInfo.RefreshInfoValue>> TEST_OBTAINED = new HashMap<>();
    static {
        TEST_OBTAINED.put(TEST_DATASTREAM, Collections.singletonList(TEST_VALUE));
    }
    private static final OperationRefreshInfo.Result TEST_RESULT = new OperationRefreshInfo.Result(TEST_OBTAINED);


    @Mock
    private OperationRefreshInfo mockedRefreshInfo;
    @InjectMocks
    private RefreshInfoProcessor testProcessor;


    @Test
    public void testParseParameters() {
        assertNull(testProcessor.parseParameters(TEST_REQUEST));
    }

    @Test
    public void testProcessOperation() {
        testProcessor.processOperation(TEST_DEVICE_ID, TEST_ID, null);

        verify(mockedRefreshInfo).refreshInfo(eq(TEST_DEVICE_ID));
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
        assertEquals(REFRESH_INFO_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(REFRESH_INFO_OPERATION_NAME, step.getName());
        assertEquals(StepResultCode.SUCCESSFUL, step.getResult());
        List<Object> stepResponse = step.getResponse();
        assertNotNull(stepResponse);
        assertEquals(1, stepResponse.size());
        OutputVariable outputVariable = (OutputVariable) stepResponse.get(0);
        assertNotNull(outputVariable);
        assertEquals(TEST_DATASTREAM, outputVariable.getVariableName());
        assertEquals(Collections.singletonList(TEST_VALUE), outputVariable.getVariableValue());
        assertEquals(SUCCESS_RESULT, outputVariable.getResultCode());
    }
}