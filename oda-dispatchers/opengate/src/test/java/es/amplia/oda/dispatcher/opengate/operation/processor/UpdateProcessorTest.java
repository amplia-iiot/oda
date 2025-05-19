package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.update.ParameterUpdateOperation;
import es.amplia.oda.dispatcher.opengate.domain.update.RequestUpdateOperation;
import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElementOperationType;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElementOption;
import es.amplia.oda.operation.api.OperationUpdate.OperationResultCodes;
import es.amplia.oda.operation.api.OperationUpdate.Result;
import es.amplia.oda.operation.api.OperationUpdate.StepResult;
import es.amplia.oda.operation.api.OperationUpdate.StepResultCodes;
import es.amplia.oda.operation.api.OperationUpdate.UpdateStepName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.dispatcher.opengate.operation.processor.UpdateProcessor.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UpdateProcessorTest {

    private static final String TEST_ID = "testOperationId";
    private static final String TEST_DEVICE_ID = "testDevice";
    private static final String[] TEST_PATH = new String[] {"path", "to", "device"};
    private static final String TEST_BUNDLE_NAME = "testBundle";
    private static final String TEST_BUNDLE_VERSION = "1.0.0";
    private static final String TEST_DEPLOYMENT_ELEMENT_NAME = "testDeploymentElement";
    private static final String TEST_DEPLOYMENT_ELEMENT_VERSION = "1.0.0";
    private static final String TEST_DEPLOYMENT_ELEMENT_TYPE = "SOFTWARE";
    private static final String TEST_DEPLOYMENT_ELEMENT_DOWNLOAD_URL = "http://test.url";
    private static final String TEST_DEPLOYMENT_ELEMENT_PATH = "deploy";
    private static final String TEST_DEPLOYMENT_ELEMENT_OPERATION = "INSTALL";
    private static final String TEST_DEPLOYMENT_ELEMENT_OPTION = "MANDATORY";
    private static final Long TEST_DEPLOYMENT_ELEMENT_ORDER = 1L;
    private static final List<String> TEST_DEPLOYMENT_ELEMENT_VALIDATORS = new ArrayList<>();
    private static final Long TEST_DEPLOYMENT_ELEMENT_SIZE = 1024L;
    private static final String TEST_DEPLOYMENT_ELEMENT_OLDVERSION = "0.9";
    private static final DeploymentElement TEST_DEPLOYMENT_ELEMENT =
            new DeploymentElement(
                    TEST_DEPLOYMENT_ELEMENT_NAME,
                    TEST_DEPLOYMENT_ELEMENT_VERSION,
                    TEST_DEPLOYMENT_ELEMENT_TYPE,
                    TEST_DEPLOYMENT_ELEMENT_DOWNLOAD_URL,
                    TEST_DEPLOYMENT_ELEMENT_PATH,
                    TEST_DEPLOYMENT_ELEMENT_ORDER,
                    TEST_DEPLOYMENT_ELEMENT_OPERATION,
                    TEST_DEPLOYMENT_ELEMENT_VALIDATORS,
                    TEST_DEPLOYMENT_ELEMENT_SIZE,
                    TEST_DEPLOYMENT_ELEMENT_OLDVERSION,
                    TEST_DEPLOYMENT_ELEMENT_OPTION
            );
    private static final List<DeploymentElement> TEST_DEPLOYMENT_ELEMENTS = Collections.singletonList(TEST_DEPLOYMENT_ELEMENT);
    private static final ParameterUpdateOperation TEST_BUNDLE_PARAM =
            new ParameterUpdateOperation(TEST_BUNDLE_NAME, TEST_BUNDLE_VERSION, TEST_DEPLOYMENT_ELEMENTS);
    private static final RequestUpdateOperation TEST_REQUEST = new RequestUpdateOperation(TEST_BUNDLE_PARAM);
    private static final String TEST_RESULT_DESCRIPTION = "result description";
    private static final UpdateStepName TEST_STEP_NAME = UpdateStepName.ENDINSTALL;
    private static final String TEST_STEP_DESCRIPTION = "Installation description";
    private static final StepResult TEST_STEP = new StepResult(TEST_STEP_NAME, StepResultCodes.SUCCESSFUL,
            TEST_STEP_DESCRIPTION);
    private static final Result TEST_RESULT = new Result(OperationResultCodes.SUCCESSFUL, TEST_RESULT_DESCRIPTION,
            Collections.singletonList(TEST_STEP));


    @Mock
    private OperationUpdate mockedUpdate;
    @InjectMocks
    private UpdateProcessor testProcessor;


    @Test
    public void testParseParameters() {
        ParameterUpdateOperation updateParameters = testProcessor.parseParameters(TEST_REQUEST);

        assertNotNull(updateParameters);
        assertEquals(TEST_BUNDLE_NAME, updateParameters.getBundleName());
        assertEquals(TEST_BUNDLE_VERSION, updateParameters.getBundleVersion());
        List<DeploymentElement> deploymentElements = updateParameters.getDeploymentElements();
        assertNotNull(deploymentElements);
        assertEquals(1, deploymentElements.size());
        DeploymentElement deploymentElement = deploymentElements.get(0);
        assertEquals(TEST_DEPLOYMENT_ELEMENT_NAME, deploymentElement.getName());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_VERSION, deploymentElement.getVersion());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_TYPE, deploymentElement.getType().toString());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_DOWNLOAD_URL, deploymentElement.getDownloadUrl());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_PATH, deploymentElement.getPath());
        assertEquals(DeploymentElementOperationType.valueOf(TEST_DEPLOYMENT_ELEMENT_OPERATION),
                deploymentElement.getOperation());
        assertEquals(DeploymentElementOption.valueOf(TEST_DEPLOYMENT_ELEMENT_OPTION), deploymentElement.getOption());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_ORDER, deploymentElement.getOrder());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_VALIDATORS, deploymentElement.getValidators());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_SIZE, deploymentElement.getSize());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_OLDVERSION, deploymentElement.getOldVersion());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoParams() {
        RequestUpdateOperation invalidRequest = new RequestUpdateOperation(null);

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoBundleNameParam() {
        RequestUpdateOperation invalidRequest = new RequestUpdateOperation(new ParameterUpdateOperation(null, "1.0.0", TEST_DEPLOYMENT_ELEMENTS));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoBundleVersionParam() {
        RequestUpdateOperation invalidRequest = new RequestUpdateOperation(new ParameterUpdateOperation("thisIsAName", null, TEST_DEPLOYMENT_ELEMENTS));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoDeploymentElementParam() {
        RequestUpdateOperation invalidRequest = new RequestUpdateOperation(new ParameterUpdateOperation("thisIsAName", "1.0.0", null));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersEmptyDeploymentElementsParam() {
        RequestUpdateOperation invalidRequest = new RequestUpdateOperation(new ParameterUpdateOperation("thisIsAName", "1.0.0", Collections.emptyList()));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test
    public void testProcessOperation() {
        testProcessor.processOperation(TEST_DEVICE_ID, TEST_ID, TEST_BUNDLE_PARAM);

        verify(mockedUpdate).update(eq(TEST_ID), eq(TEST_BUNDLE_NAME), eq(TEST_BUNDLE_VERSION), eq(TEST_DEPLOYMENT_ELEMENTS));
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
        assertEquals(UPDATE_OPERATION_NAME, response.getName());
        assertEquals(OperationResultCode.SUCCESSFUL, response.getResultCode());
        assertEquals(TEST_RESULT_DESCRIPTION, response.getResultDescription());
        List<Step> steps = response.getSteps();
        assertNotNull(steps);
        assertEquals(1, steps.size());
        Step step = steps.get(0);
        assertEquals(UpdateStepName.ENDINSTALL.toString(), step.getName());
        assertEquals(StepResultCode.SUCCESSFUL, step.getResult());
        assertEquals(TEST_STEP_DESCRIPTION, step.getDescription());
    }
}