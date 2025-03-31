package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.general.ParameterGeneralOperation;
import es.amplia.oda.dispatcher.opengate.domain.general.RequestGeneralOperation;
import es.amplia.oda.operation.api.OperationDiscover;
import es.amplia.oda.operation.api.OperationDiscover.Result;
import es.amplia.oda.operation.api.OperationDiscover.ResultCode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.dispatcher.opengate.operation.processor.DiscoverProcessor.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DiscoverProcessorTest {

    private static final String TEST_ID = "testOperationId";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] {"path", "to", "device"};
    private static final ParameterGeneralOperation TEST_PARAMETERS =
            new ParameterGeneralOperation(Collections.emptyList());
    private static final RequestGeneralOperation TEST_REQUEST =
            new RequestGeneralOperation(TEST_PARAMETERS);
    private static final Result TEST_RESULT = new Result(ResultCode.SUCCESSFUL, null);


    @Mock
    private OperationDiscover mockedDiscover;
    @InjectMocks
    private DiscoverProcessor testProcessor;

    @Test
    public void testParseParameters() {
        assertNull(testProcessor.parseParameters(TEST_REQUEST));
    }

    @Test
    public void testProcessOperation() {
        testProcessor.processOperation(TEST_DEVICE_ID, TEST_ID, null);

        verify(mockedDiscover).discover();
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
        assertEquals(DISCOVER_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(DISCOVER_OPERATION_NAME, step.getName());
        assertEquals(StepResultCode.SUCCESSFUL, step.getResult());
    }
}