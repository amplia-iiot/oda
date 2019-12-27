package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationUpdate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.operation.api.OperationUpdate.*;
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
    private static final ValueObject TEST_BUNDLE_NAME_OBJECT = new ValueObject(TEST_BUNDLE_NAME, null, null, null);
    private static final Parameter TEST_BUNDLE_NAME_PARAM =
            new Parameter("bundleName", "string", TEST_BUNDLE_NAME_OBJECT);
    private static final String TEST_BUNDLE_VERSION = "1.0.0";
    private static final ValueObject TEST_BUNDLE_VERSION_OBJECT = new ValueObject(TEST_BUNDLE_VERSION, null, null, null);
    private static final Parameter TEST_BUNDLE_VERSION_PARAM =
            new Parameter("bundleVersion", "string", TEST_BUNDLE_VERSION_OBJECT);
    private static final String TEST_DEPLOYMENT_ELEMENT_NAME = "testDeploymentElement";
    private static final String TEST_DEPLOYMENT_ELEMENT_VERSION = "1.0.0";
    private static final String TEST_DEPLOYMENT_ELEMENT_TYPE = "SOFTWARE";
    private static final String TEST_DEPLOYMENT_ELEMENT_DOWNLOAD_URL = "http://test.url";
    private static final String TEST_DEPLOYMENT_ELEMENT_PATH = "deploy";
    private static final String TEST_DEPLOYMENT_ELEMENT_OPERATION = "INSTALL";
    private static final String TEST_DEPLOYMENT_ELEMENT_OPTION = "MANDATORY";
    private static final Integer TEST_DEPLOYMENT_ELEMENT_ORDER = 1;
    private static final Map<String, Object> TEST_DEPLOYMENT_ELEMENT = new HashMap<>();
    static {
        TEST_DEPLOYMENT_ELEMENT.put("name", TEST_DEPLOYMENT_ELEMENT_NAME);
        TEST_DEPLOYMENT_ELEMENT.put("version", TEST_DEPLOYMENT_ELEMENT_VERSION);
        TEST_DEPLOYMENT_ELEMENT.put("type", TEST_DEPLOYMENT_ELEMENT_TYPE);
        TEST_DEPLOYMENT_ELEMENT.put("downloadUrl", TEST_DEPLOYMENT_ELEMENT_DOWNLOAD_URL);
        TEST_DEPLOYMENT_ELEMENT.put("path", TEST_DEPLOYMENT_ELEMENT_PATH);
        TEST_DEPLOYMENT_ELEMENT.put("operation", TEST_DEPLOYMENT_ELEMENT_OPERATION);
        TEST_DEPLOYMENT_ELEMENT.put("option", TEST_DEPLOYMENT_ELEMENT_OPTION);
        TEST_DEPLOYMENT_ELEMENT.put("order", TEST_DEPLOYMENT_ELEMENT_ORDER);
    }
    private static final ValueObject TEST_VALUE_OBJECT =
            new ValueObject(null, null, null, Collections.singletonList(TEST_DEPLOYMENT_ELEMENT));
    private static final Parameter TEST_DEPLOYMENT_ELEMENT_PARAM = new Parameter("deploymentElements", "array", TEST_VALUE_OBJECT);
    private static final Request TEST_REQUEST =
            new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, UPDATE_OPERATION_NAME,
                    Arrays.asList(TEST_BUNDLE_NAME_PARAM, TEST_BUNDLE_VERSION_PARAM, TEST_DEPLOYMENT_ELEMENT_PARAM));
    private static final Parameter TEST_OTHER_PARAM =
            new Parameter("otherParam", "string", new ValueObject("other", null, null, null));
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
        UpdateParameters updateParameters = testProcessor.parseParameters(TEST_REQUEST);

        assertNotNull(updateParameters);
        assertEquals(TEST_BUNDLE_NAME, updateParameters.getBundleName());
        assertEquals(TEST_BUNDLE_VERSION, updateParameters.getBundleVersion());
        List<DeploymentElement> deploymentElements = updateParameters.getDeploymentElements();
        assertNotNull(deploymentElements);
        assertEquals(1, deploymentElements.size());
        DeploymentElement deploymentElement = deploymentElements.get(0);
        assertEquals(TEST_DEPLOYMENT_ELEMENT_NAME, deploymentElement.getName());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_VERSION, deploymentElement.getVersion());
        assertEquals(DeploymentElementType.valueOf(TEST_DEPLOYMENT_ELEMENT_TYPE), deploymentElement.getType());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_DOWNLOAD_URL, deploymentElement.getDownloadUrl());
        assertEquals(TEST_DEPLOYMENT_ELEMENT_PATH, deploymentElement.getPath());
        assertEquals(DeploymentElementOperationType.valueOf(TEST_DEPLOYMENT_ELEMENT_OPERATION),
                deploymentElement.getOperation());
        assertEquals(DeploymentElementOption.valueOf(TEST_DEPLOYMENT_ELEMENT_OPTION), deploymentElement.getOption());
        assertEquals(Integer.toUnsignedLong(TEST_DEPLOYMENT_ELEMENT_ORDER), deploymentElement.getOrder().longValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoParams() {
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, UPDATE_OPERATION_NAME, null);

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoSizeThreeParams() {
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, UPDATE_OPERATION_NAME,
                Collections.emptyList());

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoBundleNameParam() {
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, UPDATE_OPERATION_NAME,
                Arrays.asList(TEST_BUNDLE_VERSION_PARAM, TEST_DEPLOYMENT_ELEMENT_PARAM, TEST_OTHER_PARAM));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoBundleVersionParam() {
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, UPDATE_OPERATION_NAME,
                Arrays.asList(TEST_BUNDLE_NAME_PARAM, TEST_DEPLOYMENT_ELEMENT_PARAM, TEST_OTHER_PARAM));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersNoDeploymentElementParam() {
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, UPDATE_OPERATION_NAME,
                Arrays.asList(TEST_BUNDLE_NAME_PARAM, TEST_BUNDLE_VERSION_PARAM, TEST_OTHER_PARAM));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersIncorrectTypeBundleNameParam() {
        ValueObject invalidBundleNameValue = new ValueObject(null, 99.0, null, null);
        Parameter invalidBundleNameParam = new Parameter("bundleName", "string", invalidBundleNameValue);
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, UPDATE_OPERATION_NAME,
                Arrays.asList(invalidBundleNameParam, TEST_BUNDLE_VERSION_PARAM, TEST_DEPLOYMENT_ELEMENT_PARAM));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersIncorrectTypeBundleVersionParam() {
        ValueObject invalidBundleVersionValue = new ValueObject(null, 99.0, null, null);
        Parameter invalidBundleVersionParam = new Parameter("bundleVersion", "string", invalidBundleVersionValue);
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, UPDATE_OPERATION_NAME,
                Arrays.asList(TEST_BUNDLE_NAME_PARAM, invalidBundleVersionParam, TEST_DEPLOYMENT_ELEMENT_PARAM));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersIncorrectDeploymentElementsParam() {
        ValueObject invalidDeploymentElementsValue = new ValueObject(null, 99.0, null, null);
        Parameter invalidDeploymentElementsParam =
                new Parameter("deploymentElements", "string", invalidDeploymentElementsValue);
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, UPDATE_OPERATION_NAME,
                Arrays.asList(TEST_BUNDLE_NAME_PARAM, TEST_BUNDLE_VERSION_PARAM, invalidDeploymentElementsParam));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseParametersEmptyDeploymentElementsParam() {
        ValueObject emptyDeploymentElementsValue = new ValueObject(null, null, null, Collections.emptyList());
        Parameter emptyDeploymentElementsParam =
                new Parameter("deploymentElements", "array", emptyDeploymentElementsValue);
        Request invalidRequest = new Request(TEST_ID, 0L, TEST_DEVICE_ID, TEST_PATH, UPDATE_OPERATION_NAME,
                Arrays.asList(TEST_BUNDLE_NAME_PARAM, TEST_BUNDLE_VERSION_PARAM, emptyDeploymentElementsParam));

        testProcessor.parseParameters(invalidRequest);

        fail("Illegal argument exception is thrown");
    }

    @Test
    public void testProcessOperation() {
        DeploymentElement deploymentElement = new DeploymentElement(TEST_DEPLOYMENT_ELEMENT_NAME,
                TEST_DEPLOYMENT_ELEMENT_VERSION, DeploymentElementType.SOFTWARE, TEST_DEPLOYMENT_ELEMENT_DOWNLOAD_URL,
                TEST_DEPLOYMENT_ELEMENT_PATH, DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY,
                1L);
        List<DeploymentElement> deploymentElements = Collections.singletonList(deploymentElement);
        UpdateParameters updateParameters =
                new UpdateParameters(TEST_BUNDLE_NAME, TEST_BUNDLE_VERSION, deploymentElements);

        testProcessor.processOperation(TEST_DEVICE_ID, updateParameters);

        verify(mockedUpdate).update(eq(TEST_BUNDLE_NAME), eq(TEST_BUNDLE_VERSION), eq(deploymentElements));
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