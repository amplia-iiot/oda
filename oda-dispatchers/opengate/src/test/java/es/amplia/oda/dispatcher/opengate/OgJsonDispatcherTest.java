package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.operation.api.OperationRefreshInfo;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;
import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElementOperationType;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElementOption;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElementType;

import lombok.Value;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
In order to understand these tests, it is necessary to consider how the system has been modeled as the list of mocked objects
is huge:
    @Mock JsonParser jsonParser;
    @Mock JsonWriter jsonWriter;
    @Mock OperationGetDeviceParameters operationGetDeviceParameters;
    @Mock OperationSetDeviceParameters operationSetDeviceParameters;
    @Mock OperationRefreshInfo operationRefreshInfo;
    @Mock OperationUpdate operationUpdate;
    @Mock DeviceInfoProvider deviceInfoProvider;

The system has been modeled on the assumption that:
    - odaDeviceId is the ODA identifier in OpenGate, so that if an OpenGate request
      specifies this identifier, null must be passed in the deviceId of the referenced operation.
    - There are two datastreams: "id2" and "id3". Any other identifier, and in particular "id1", does not exist.
        "id2" is of Integer type
        "id3" is of String type
    - Each operation mock returns a future that will be completed in each test that needs it:
        operationGetDeviceParameters -> futureForOperationGet
        operationSetDeviceParameters -> futureForOperationSet
        operationRefreshInfo -> futureForOperationRefreshInfo
        operationUpdate -> futureForOperationUpdate
    - These tests should be distributed in more files, at least one per operation (Get, Set, Refresh and Update)
      Since this has not been done, attempts are being made to minimize the mess by the grouping the data 
      for each of the operations in a Java class (see GetData, SetData, RefreshInfoData and 
      UpdateData). This allows, for example, to perform functions that present all the necessary data in all the
      tests in a coordinated way (see getDataConstants, setDataConstants, refreshInfoDataConstants and updateDataConstants)
 */
public class OgJsonDispatcherTest {

    private static final String odaDeviceId = "odaDeviceId";
    private static final byte[] input = "whatever".getBytes();
    private static final byte[] output = "outputBytes".getBytes();
    private static final String operationId = "anOperationId";
    private static final Long timestamp = null;
    private static final GetData getData = getDataConstants();
    private static final SetData setData = setDataConstants();
    private static final RefreshInfoData refreshInfoData = refreshInfoDataConstants();
    private static final UpdateData updateData = updateDataConstants();
    private OgJsonDispatcher dispatcher;
    @Mock
    private JsonParser jsonParser;
    @Mock
    private JsonWriter jsonWriter;
    @Mock
    private OperationGetDeviceParameters operationGetDeviceParameters;
    @Mock
    private OperationSetDeviceParameters operationSetDeviceParameters;
    @Mock
    private OperationRefreshInfo operationRefreshInfo;
    @Mock
    private OperationUpdate operationUpdate;
    @Mock
    private DeviceInfoProvider deviceInfoProvider;
    private CompletableFuture<OperationGetDeviceParameters.Result> futureForOperationGet;
    private CompletableFuture<OperationRefreshInfo.Result> futureForOperationRefreshInfo;
    private CompletableFuture<OperationSetDeviceParameters.Result> futureForOperationSet;
    private CompletableFuture<OperationUpdate.Result> futureForOperationUpdate;

    private static GetData getDataConstants() {
        Input opengateInput = new Input(new InputOperation(new RequestGetDeviceParameters(
                operationId,
                null,
                timestamp,
                Collections.singletonList(
                        new RequestGetDeviceParameters.Parameter("variableList", new RequestGetDeviceParameters.ValueArray(
                                Arrays.asList(
                                        new RequestGetDeviceParameters.VariableListElement("id1"),
                                        new RequestGetDeviceParameters.VariableListElement(null),
                                        new RequestGetDeviceParameters.VariableListElement("id2"),
                                        new RequestGetDeviceParameters.VariableListElement("id3")
                                )
                        ))
                )
        )));
        Set<String> parsedInput = asSet("id1", "id2", "id3");
        List<OperationGetDeviceParameters.GetValue> values = Arrays.asList(
                new OperationGetDeviceParameters.GetValue("id1", OperationGetDeviceParameters.Status.NOT_FOUND, null, null),
                new OperationGetDeviceParameters.GetValue("id2", OperationGetDeviceParameters.Status.OK, 42, null),
                new OperationGetDeviceParameters.GetValue("id3", OperationGetDeviceParameters.Status.OK, "hi", null)
        );

        OperationGetDeviceParameters.Result operationResult = new OperationGetDeviceParameters.Result(values);
        Output opengateOutput = new Output(
                "8.0",
                new OutputOperation(
                        new Response(
                                operationId,
                                odaDeviceId,
                                "GET_DEVICE_PARAMETERS",
                                OperationResultCode.SUCCESSFUL,
                                "No Error.",
                                Collections.singletonList(
                                        new Step(
                                                "GET_DEVICE_PARAMETERS",
                                                StepResultCode.SUCCESSFUL,
                                                "",
                                                0L,
                                                Arrays.asList(
                                                        new OutputVariable("id1", null, "NON_EXISTENT", "No datastream found"),
                                                        new OutputVariable("id2", 42, "SUCCESS", "SUCCESS"),
                                                        new OutputVariable("id3", "hi", "SUCCESS", "SUCCESS")
                                                )
                                        )
                                )
                        )
                )
        );
        return new GetData(opengateInput, parsedInput, operationResult, opengateOutput);
    }

    private static SetData setDataConstants() {
        Input opengateInput = new Input(new InputOperation(new RequestSetDeviceParameters(
                operationId,
                null,
                timestamp,
                Collections.singletonList(
                        new RequestSetDeviceParameters.Parameter("variableList", new RequestSetDeviceParameters.ValueArray(
                                Arrays.asList(
                                        new RequestSetDeviceParameters.VariableListElement("id1", null),
                                        new RequestSetDeviceParameters.VariableListElement("id2", 42),
                                        new RequestSetDeviceParameters.VariableListElement("id3", "hi")
                                )
                        ))
                )
        )));
        List<OperationSetDeviceParameters.VariableValue> parsedInput = Arrays.asList(
                new OperationSetDeviceParameters.VariableValue("id1", null),
                new OperationSetDeviceParameters.VariableValue("id2", 42),
                new OperationSetDeviceParameters.VariableValue("id3", "hi")
        );


        String resultDescription = "An ok message";
        String errorOfFirstSet = "A first error";
        String errorOfSecondSet = "A second error";
        OperationSetDeviceParameters.Result operationResult = new OperationSetDeviceParameters.Result(
                OperationSetDeviceParameters.ResultCode.SUCCESSFUL,
                resultDescription,
                Arrays.asList(
                        new OperationSetDeviceParameters.VariableResult("id1", errorOfFirstSet),
                        new OperationSetDeviceParameters.VariableResult("id2", null),
                        new OperationSetDeviceParameters.VariableResult("id3", errorOfSecondSet)
                )
        );

        Output opengateOutput = new Output(
                "8.0",
                new OutputOperation(
                        new Response(
                                operationId,
                                odaDeviceId,
                                "SET_DEVICE_PARAMETERS",
                                OperationResultCode.SUCCESSFUL,
                                resultDescription,
                                Collections.singletonList(
                                        new Step(
                                                "SET_DEVICE_PARAMETERS",
                                                StepResultCode.SUCCESSFUL,
                                                "",
                                                0L,
                                                Arrays.asList(
                                                        new OutputVariable("id1", null, "ERROR", errorOfFirstSet),
                                                        new OutputVariable("id2", null, "SUCCESS", "SUCCESS"),
                                                        new OutputVariable("id3", null, "ERROR", errorOfSecondSet)
                                                )
                                        )
                                )
                        )
                )
        );
        return new SetData(opengateInput, parsedInput, operationResult, opengateOutput);
    }

    private static RefreshInfoData refreshInfoDataConstants() {
        Input opengateRefreshInfoRequest = new Input(new InputOperation(new RequestRefreshInfo(operationId, null, timestamp)));
        HashMap<String, Object> obtainedValues = new HashMap<>();
        obtainedValues.put("id2", 42);
        obtainedValues.put("id3", "hi");
        OperationRefreshInfo.Result operationRefreshInfoResult = new OperationRefreshInfo.Result(obtainedValues);
        Output operationRefreshInfoResultAsOpenGateStructure = new Output(
                "8.0",
                new OutputOperation(
                        new Response(
                                operationId,
                                odaDeviceId,
                                "REFRESH_INFO",
                                OperationResultCode.SUCCESSFUL,
                                "No Error.",
                                Collections.singletonList(
                                        new Step(
                                                "REFRESH_INFO",
                                                StepResultCode.SUCCESSFUL,
                                                "",
                                                0L,
                                                Arrays.asList(
                                                        new OutputVariable("id2", 42, "SUCCESS", "SUCCESS"),
                                                        new OutputVariable("id3", "hi", "SUCCESS", "SUCCESS")
                                                )
                                        )
                                )
                        )
                )
        );
        return new RefreshInfoData(opengateRefreshInfoRequest, operationRefreshInfoResult, operationRefreshInfoResultAsOpenGateStructure);
    }

    private static UpdateData updateDataConstants() {
        String bundleName = "aBundleName";
        String bundleVersion = "aBundleVersion";
        List<OperationUpdate.DeploymentElement> deploymentElements = Arrays.asList(
                new OperationUpdate.DeploymentElement("name1", "version1", OperationUpdate.DeploymentElementType.SOFTWARE, "downloadUrl1", "path1", OperationUpdate.DeploymentElementOperationType.INSTALL, OperationUpdate.DeploymentElementOption.MANDATORY, 1L),
                new OperationUpdate.DeploymentElement("name2", "version2", OperationUpdate.DeploymentElementType.CONFIGURATION, "downloadUrl2", "path2", OperationUpdate.DeploymentElementOperationType.UNINSTALL, OperationUpdate.DeploymentElementOption.OPTIONAL, 2L)
        );
        Input opengateInput = new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType(bundleName, null)),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType(bundleVersion, null)),
                        new RequestUpdate.Parameter("deploymentElements",
                                new RequestUpdate.ValueType(
                                        null,
                                        Arrays.asList(
                                                new RequestUpdate.VariableListElement("name1", "version1", DeploymentElementType.SOFTWARE, "downloadUrl1", "path1", DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 1L),
                                                new RequestUpdate.VariableListElement("name2", "version2", DeploymentElementType.CONFIGURATION, "downloadUrl2", "path2", DeploymentElementOperationType.UNINSTALL, DeploymentElementOption.OPTIONAL, 2L)
                                        )
                                )
                        )
                )
        )));
        List<OperationUpdate.StepResult> steps = Arrays.asList(
                new OperationUpdate.StepResult(OperationUpdate.UpdateStepName.BEGINUPDATE, OperationUpdate.StepResultCodes.NOT_EXECUTED, "description1"),
                new OperationUpdate.StepResult(OperationUpdate.UpdateStepName.DOWNLOADFILE, OperationUpdate.StepResultCodes.SKIPPED, "description2"),
                new OperationUpdate.StepResult(OperationUpdate.UpdateStepName.ENDUPDATE, OperationUpdate.StepResultCodes.SUCCESSFUL, "description3")
        );
        String resultDescription = "resultDescription";
        OperationUpdate.Result operationResult = new OperationUpdate.Result(OperationUpdate.OperationResultCodes.SUCCESSFUL, resultDescription, steps);
        Output opengateOutput = new Output(
                "8.0",
                new OutputOperation(
                        new Response(
                                operationId,
                                odaDeviceId,
                                "UPDATE",
                                OperationResultCode.SUCCESSFUL,
                                resultDescription,
                                Arrays.asList(
                                        new Step("BEGINUPDATE", StepResultCode.NOT_EXECUTED, "description1", 0L, null),
                                        new Step("DOWNLOADFILE", StepResultCode.SKIPPED, "description2", 0L, null),
                                        new Step("ENDUPDATE", StepResultCode.SUCCESSFUL, "description3", 0L, null))
                        )
                )
        );
        return new UpdateData(opengateInput, bundleName, bundleVersion, deploymentElements, operationResult, opengateOutput);
    }

    @SafeVarargs
    private static <T> Set<T> asSet(T... ts) {
        return new HashSet<>(Arrays.asList(ts));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        dispatcher = new OgJsonDispatcher(
                jsonParser,
                jsonWriter,
                deviceInfoProvider,
                operationGetDeviceParameters,
                operationSetDeviceParameters,
                operationRefreshInfo,
                operationUpdate
        );
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestRefreshInfo(operationId, null, timestamp))));

        futureForOperationGet = new CompletableFuture<>();
        futureForOperationSet = new CompletableFuture<>();
        futureForOperationRefreshInfo = new CompletableFuture<>();
        futureForOperationUpdate = new CompletableFuture<>();

        when(operationGetDeviceParameters.getDeviceParameters("", getData.getParsedInput())).thenReturn(futureForOperationGet);
        when(operationSetDeviceParameters.setDeviceParameters("", setData.getParsedInput())).thenReturn(futureForOperationSet);
        when(operationRefreshInfo.refreshInfo("")).thenReturn(futureForOperationRefreshInfo);
        when(operationUpdate.update(updateData.getBundleName(), updateData.getBundleVersion(), updateData.getDeploymentElements())).thenReturn(futureForOperationUpdate);

        when(deviceInfoProvider.getDeviceId()).thenReturn(odaDeviceId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void withNullParameter_Throws() {
        dispatcher.process(null);
    }

    @Test
    public void jsonParserIsUsedToParseIncomingMessage() {
        dispatcher.process(input);

        verify(jsonParser).parseInput(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifParserReturnsNull_Throws() {
        when(jsonParser.parseInput(input)).thenReturn(null);

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifParserReturnsAnInputWithNullOperation_Throws() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(null));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifParserReturnsAnInputWithNullRequest_Throws() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(null)));

        dispatcher.process(input);
    }

    //-----------------------------------------------------
    // -- GET_DEVICE_PARAMETERS
    //-----------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void getOperationMustHaveParameters() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                operationId,
                null,
                timestamp,
                null
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationMustHaveExactlyOneElementInParameters() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestGetDeviceParameters.Parameter("unknown", null),
                        new RequestGetDeviceParameters.Parameter("unknown", null)
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationMustHaveExactlyOneElementNotNullInParameters() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                operationId,
                null,
                timestamp,
                Collections.singletonList(null)
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationMustHaveExactlyTheParameterVariableListInParameter() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                operationId,
                null,
                timestamp,
                Collections.singletonList(
                        new RequestGetDeviceParameters.Parameter("unknown", null)
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationVariableListMustHaveValue() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                operationId,
                null,
                timestamp,
                Collections.singletonList(
                        new RequestGetDeviceParameters.Parameter("variableList", null)
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationVariableListMustHaveValueWithArray() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                operationId,
                null,
                timestamp,
                Collections.singletonList(
                        new RequestGetDeviceParameters.Parameter("variableList", new RequestGetDeviceParameters.ValueArray())
                )
        ))));

        dispatcher.process(input);
    }

    @Test
    public void getOperationsAreDispatchedToOperationGetDeviceParameters() {
        when(jsonParser.parseInput(input)).thenReturn(getData.getOpengateInput());

        dispatcher.process(input);

        verify(operationGetDeviceParameters).getDeviceParameters("", getData.getParsedInput());
    }

    @Test
    public void ifOperationGetDeviceParametersReturnNull_aNotSupportedOperationOutputIsGivenToJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(getData.getOpengateInput());
        when(operationGetDeviceParameters.getDeviceParameters("", getData.getParsedInput())).thenReturn(null);
        Response notSupportedResponse = new Response(operationId, odaDeviceId, "GET_DEVICE_PARAMETERS", OperationResultCode.NOT_SUPPORTED, "Operation not supported by the device", null);
        Output expected = new Output("8.0", new OutputOperation(notSupportedResponse));

        CompletableFuture<byte[]> future = dispatcher.process(input);

        assertTrue(future.isDone());
        verify(jsonWriter).dumpOutput(eq(expected));
    }

    @Test
    public void aCompletableFutureIsReturnedThatWillBeCompletedWhenTheOperationGetIsCompleted() {
        when(jsonParser.parseInput(input)).thenReturn(getData.getOpengateInput());

        CompletableFuture<byte[]> future = dispatcher.process(input);
        assertFalse(future.isDone());

        futureForOperationGet.complete(null);
        assertTrue(future.isDone());
    }

    @Test
    public void whenTheOperationGetDeviceParametersCompletes_JsonWriterIsUsedToDumpTheResponse() {
        when(jsonParser.parseInput(input)).thenReturn(getData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationGet.complete(null);

        verify(jsonWriter).dumpOutput(isA(Output.class));
    }

    @Test
    public void ifGetOperationResultIsNullAnErrorOutputIsInjectedInJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(getData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationGet.complete(null);

        List<Step> steps = Collections.singletonList(new Step("GET_DEVICE_PARAMETERS", StepResultCode.ERROR, "NullPointerException: null", 0L, null));
        OutputOperation operation = new OutputOperation(new Response(operationId, odaDeviceId, "GET_DEVICE_PARAMETERS", OperationResultCode.ERROR_PROCESSING, "NullPointerException: null", steps));
        Output expected = new Output("8.0", operation);
        verify(jsonWriter).dumpOutput(expected);
    }

    @Test
    public void getOperationResultIsTranslatedToOutputAndInjectedInJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(getData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationGet.complete(getData.getOperationResult());

        verify(jsonWriter).dumpOutput(getData.getOpengateOutput());
    }

    @Test
    public void inAGetOperationTheByteArrayReturnedByTheJsonWriterIsReturnedInTheDispatcherFuture() throws InterruptedException, ExecutionException {
        when(jsonParser.parseInput(input)).thenReturn(getData.getOpengateInput());
        when(jsonWriter.dumpOutput(getData.getOpengateOutput())).thenReturn(output);

        CompletableFuture<byte[]> dispatcherFuture = dispatcher.process(input);
        futureForOperationGet.complete(getData.getOperationResult());

        assertTrue(dispatcherFuture.isDone());
        byte[] actual = dispatcherFuture.get();
        assertEquals(actual, output);
    }

    //-----------------------------------------------------
    // -- REFRESH_INFO
    //-----------------------------------------------------
    @Test
    public void ifOperationRefreshInfoReturnsNull_aNotSupportedOperationOutputIsGivenToJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(refreshInfoData.getOpengateInput());
        when(operationRefreshInfo.refreshInfo("")).thenReturn(null);
        Response notSupportedResponse = new Response(operationId, odaDeviceId, "REFRESH_INFO", OperationResultCode.NOT_SUPPORTED, "Operation not supported by the device", null);
        Output expected = new Output("8.0", new OutputOperation(notSupportedResponse));

        CompletableFuture<byte[]> future = dispatcher.process(input);

        assertTrue(future.isDone());
        verify(jsonWriter).dumpOutput(eq(expected));
    }

    @Test
    public void refreshInfoOperationsAreDispatchedToOperationRefreshInfo() {
        when(jsonParser.parseInput(input)).thenReturn(refreshInfoData.getOpengateInput());

        dispatcher.process(input);

        verify(operationRefreshInfo).refreshInfo("");
    }

    @Test
    public void aCompletableFutureIsReturnedThatWillBeCompletedWhenTheOperationRefreshInfoIsCompleted() {
        when(jsonParser.parseInput(input)).thenReturn(refreshInfoData.getOpengateInput());

        CompletableFuture<byte[]> future = dispatcher.process(input);
        assertFalse(future.isDone());

        futureForOperationRefreshInfo.complete(refreshInfoData.getOperationResult());
        assertTrue(future.isDone());
    }

    @Test
    public void whenTheOperationRefreshInfoCompletes_JsonWriterIsUsedToDumpTheResponse() {
        when(jsonParser.parseInput(input)).thenReturn(refreshInfoData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationRefreshInfo.complete(refreshInfoData.getOperationResult());

        verify(jsonWriter).dumpOutput(isA(Output.class));
    }

    @Test
    public void theResultOfARefreshInfoIsTranslatedAndPassedToJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(refreshInfoData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationRefreshInfo.complete(refreshInfoData.getOperationResult());

        verify(jsonWriter).dumpOutput(refreshInfoData.getOpengateOutput());
    }

    @Test
    public void ifRefreshInfoResultIsNullAnErrorOutputIsInjectedInJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(refreshInfoData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationRefreshInfo.complete(null);

        List<Step> steps = Collections.singletonList(new Step("REFRESH_INFO", StepResultCode.ERROR, "NullPointerException: null", 0L, null));
        OutputOperation operation = new OutputOperation(new Response(operationId, odaDeviceId, "REFRESH_INFO", OperationResultCode.ERROR_PROCESSING, "NullPointerException: null", steps));
        Output expected = new Output("8.0", operation);
        verify(jsonWriter).dumpOutput(expected);
    }

    @Test
    public void inARefreshInfoTheByteArrayReturnedByTheJsonWriterIsReturnedInTheDispatcherFuture() throws InterruptedException, ExecutionException {
        when(jsonParser.parseInput(input)).thenReturn(refreshInfoData.getOpengateInput());
        when(jsonWriter.dumpOutput(refreshInfoData.getOpengateOutput())).thenReturn(output);

        CompletableFuture<byte[]> dispatcherFuture = dispatcher.process(input);
        futureForOperationRefreshInfo.complete(refreshInfoData.getOperationResult());

        assertTrue(dispatcherFuture.isDone());
        byte[] actual = dispatcherFuture.get();
        assertEquals(actual, output);
    }

    //-----------------------------------------------------
    // -- SET_DEVICE_PARAMETERS
    //-----------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void setOperationMustHaveParameters() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                operationId,
                null,
                timestamp,
                null
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperationMustHaveExactlyOneElementInParameters() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestSetDeviceParameters.Parameter("unknown", null),
                        new RequestSetDeviceParameters.Parameter("unknown", null)
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperationMustHaveExactlyOneElementNotNullInParameters() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                operationId,
                null,
                timestamp,
                Collections.singletonList(null)
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperationMustHaveExactlyTheParameterVariableListInParameter() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                operationId,
                null,
                timestamp,
                Collections.singletonList(
                        new RequestSetDeviceParameters.Parameter("unknown", null)
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperationVariableListMustHaveValue() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                operationId,
                null,
                timestamp,
                Collections.singletonList(
                        new RequestSetDeviceParameters.Parameter("variableList", null)
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperationVariableListMustHaveValueWithArray() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                operationId,
                null,
                timestamp,
                Collections.singletonList(
                        new RequestSetDeviceParameters.Parameter("variableList", new RequestSetDeviceParameters.ValueArray())
                )
        ))));

        dispatcher.process(input);
    }

    @Test
    public void ifOperationSetDeviceParametersReturnsNull_aNotSupportedOperationOutputIsGivenToJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(setData.getOpengateInput());
        when(operationSetDeviceParameters.setDeviceParameters("", setData.getParsedInput())).thenReturn(null);
        Response notSupportedResponse = new Response(operationId, odaDeviceId, "SET_DEVICE_PARAMETERS", OperationResultCode.NOT_SUPPORTED, "Operation not supported by the device", null);
        Output expected = new Output("8.0", new OutputOperation(notSupportedResponse));

        CompletableFuture<byte[]> future = dispatcher.process(input);

        assertTrue(future.isDone());
        verify(jsonWriter).dumpOutput(eq(expected));
    }

    @Test
    public void setOperationsAreDispatchedToOperationSet() {
        when(jsonParser.parseInput(input)).thenReturn(setData.getOpengateInput());

        dispatcher.process(input);

        verify(operationSetDeviceParameters).setDeviceParameters("", setData.getParsedInput());
    }

    @Test
    public void aCompletableFutureIsReturnedThatWillBeCompletedWhenTheOperationSetIsCompleted() {
        when(jsonParser.parseInput(input)).thenReturn(setData.getOpengateInput());

        CompletableFuture<byte[]> future = dispatcher.process(input);
        assertFalse(future.isDone());

        futureForOperationSet.complete(null);
        assertTrue(future.isDone());
    }

    @Test
    public void whenTheOperationSetDeviceParametersCompletes_JsonWriterIsUsedToDumpTheResponse() {
        when(jsonParser.parseInput(input)).thenReturn(setData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationSet.complete(null);

        verify(jsonWriter).dumpOutput(isA(Output.class));
    }

    @Test
    public void ifSetOperationResultIsNullAnErrorOutputIsInjectedInJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(setData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationSet.complete(null);

        List<Step> steps = Collections.singletonList(new Step("SET_DEVICE_PARAMETERS", StepResultCode.ERROR, "NullPointerException: null", 0L, null));
        OutputOperation operation = new OutputOperation(new Response(operationId, odaDeviceId, "SET_DEVICE_PARAMETERS", OperationResultCode.ERROR_PROCESSING, "NullPointerException: null", steps));
        Output expected = new Output("8.0", operation);
        verify(jsonWriter).dumpOutput(expected);
    }

    @Test
    public void setOperationResultIsTranslatedToOutputAndInjectedInJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(setData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationSet.complete(setData.getOperationResult());

        verify(jsonWriter).dumpOutput(setData.getOpengateOutput());
    }

    @Test
    public void ifSetFinishedWithErrors_ThatErrorsAreTranslatedToOutput() {
        when(jsonParser.parseInput(input)).thenReturn(setData.getOpengateInput());

        dispatcher.process(input);
        String resultDescription = "An error description";
        OperationSetDeviceParameters.Result operationResult = new OperationSetDeviceParameters.Result(OperationSetDeviceParameters.ResultCode.ERROR_IN_PARAM, resultDescription, null);
        futureForOperationSet.complete(operationResult);

        List<Step> steps = Collections.singletonList(new Step("SET_DEVICE_PARAMETERS", StepResultCode.ERROR, resultDescription, 0L, null));
        Response response = new Response(operationId, odaDeviceId, "SET_DEVICE_PARAMETERS", OperationResultCode.ERROR_IN_PARAM, resultDescription, steps);
        OutputOperation operation = new OutputOperation(response);
        Output opengateOutput = new Output("8.0", operation);
        verify(jsonWriter).dumpOutput(opengateOutput);
    }

    @Test
    public void inASetOperationTheByteArrayReturnedByTheJsonWriterIsReturnedInTheDispatcherFuture() throws InterruptedException, ExecutionException {
        when(jsonParser.parseInput(input)).thenReturn(setData.getOpengateInput());
        when(jsonWriter.dumpOutput(setData.getOpengateOutput())).thenReturn(output);

        CompletableFuture<byte[]> dispatcherFuture = dispatcher.process(input);
        futureForOperationSet.complete(setData.getOperationResult());

        assertTrue(dispatcherFuture.isDone());
        byte[] actual = dispatcherFuture.get();
        assertEquals(actual, output);
    }

    //-----------------------------------------------------
    // -- UPDATE
    //-----------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveParameters() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                null
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveExactlyThreeElementsInParameters() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestUpdate.Parameter("unknown", null),
                        new RequestUpdate.Parameter("unknown", null)
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveExactlyThreeNotNullElementsInParameters() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestUpdate.Parameter("unknown", null),
                        new RequestUpdate.Parameter("unknown", null),
                        null
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveBundleNameParameter() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestUpdate.Parameter("should be bundleName", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType())
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveBundleVersionParameter() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("should be bundleVersion", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType())
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveDeploymentElementsParameter() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("should be deploymentElements", new RequestUpdate.ValueType())
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustBundleNameParameterOfTheCorrectType() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType(null, Collections.singletonList(new RequestUpdate.VariableListElement()))),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType(null, Collections.singletonList(new RequestUpdate.VariableListElement("", "", DeploymentElementType.SOFTWARE, "", "", DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 1L))))
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustBundleVersionParameterOfTheCorrectType() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType(null, Collections.singletonList(new RequestUpdate.VariableListElement()))),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType(null, Collections.singletonList(new RequestUpdate.VariableListElement("", "", DeploymentElementType.SOFTWARE, "", "", DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 1L))))
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveDeploymentElementsParameterOfTheCorrectType() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType("", null))
                )
        ))));

        dispatcher.process(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void inAnUpdateOperationTheParameterDeploymentElementsMustHaveAtLeastOneNonNullElement() {
        when(jsonParser.parseInput(input)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                operationId,
                null,
                timestamp,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType(null, Collections.singletonList(null)))
                )
        ))));

        dispatcher.process(input);
    }

    @Test
    public void updateOperationsAreDispatchedToOperationUpdate() {
        when(jsonParser.parseInput(input)).thenReturn(updateData.getOpengateInput());

        dispatcher.process(input);

        verify(operationUpdate).update(updateData.getBundleName(), updateData.getBundleVersion(), updateData.getDeploymentElements());
    }

    @Test
    public void ifOperationUpdateReturnsNull_aNotSupportedOperationOutputIsGivenToJonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(updateData.getOpengateInput());
        when(operationUpdate.update(updateData.getBundleName(), updateData.getBundleVersion(), updateData.getDeploymentElements())).thenReturn(null);
        Response notSupportedResponse = new Response(operationId, odaDeviceId, "UPDATE", OperationResultCode.NOT_SUPPORTED, "Operation not supported by the device", null);
        Output expected = new Output("8.0", new OutputOperation(notSupportedResponse));

        CompletableFuture<byte[]> future = dispatcher.process(input);

        assertTrue(future.isDone());
        verify(jsonWriter).dumpOutput(eq(expected));
    }

    @Test
    public void aCompletableFutureIsReturnedThatWillBeCompletedWhenTheOperationUpdateIsCompleted() {
        when(jsonParser.parseInput(input)).thenReturn(updateData.getOpengateInput());

        CompletableFuture<byte[]> future = dispatcher.process(input);
        assertFalse(future.isDone());

        futureForOperationUpdate.complete(null);
        assertTrue(future.isDone());
    }

    @Test
    public void whenTheOperationUpdateCompletes_JsonWriterIsUsedToDumpTheResponse() {
        when(jsonParser.parseInput(input)).thenReturn(updateData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationUpdate.complete(null);

        verify(jsonWriter).dumpOutput(isA(Output.class));
    }

    @Test
    public void ifUpdateOperationResultIsNullAnErrorOutputIsInjectedInJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(updateData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationUpdate.complete(null);

        List<Step> steps = Collections.singletonList(new Step("UPDATE", StepResultCode.ERROR, "NullPointerException: null", 0L, null));
        OutputOperation operation = new OutputOperation(new Response(operationId, odaDeviceId, "UPDATE", OperationResultCode.ERROR_PROCESSING, "NullPointerException: null", steps));
        Output expected = new Output("8.0", operation);
        verify(jsonWriter).dumpOutput(expected);
    }

    @Test
    public void updateOperationResultIsTranslatedToOutputAndInjectedInJsonWriter() {
        when(jsonParser.parseInput(input)).thenReturn(updateData.getOpengateInput());

        dispatcher.process(input);
        futureForOperationUpdate.complete(updateData.getOperationResult());

        verify(jsonWriter).dumpOutput(updateData.getOpengateOutput());
    }

    @Test
    public void inAnUpdateOperationTheByteArrayReturnedByTheJsonWriterIsReturnedInTheDispatcherFuture() throws InterruptedException, ExecutionException {
        when(jsonParser.parseInput(input)).thenReturn(updateData.getOpengateInput());
        when(jsonWriter.dumpOutput(updateData.getOpengateOutput())).thenReturn(output);

        CompletableFuture<byte[]> dispatcherFuture = dispatcher.process(input);
        futureForOperationUpdate.complete(updateData.getOperationResult());

        assertTrue(dispatcherFuture.isDone());
        byte[] actual = dispatcherFuture.get();
        assertEquals(actual, output);
    }

    @Value
    private static class GetData {
        Input opengateInput;
        Set<String> parsedInput;
        OperationGetDeviceParameters.Result operationResult;
        Output opengateOutput;
    }

    @Value
    private static class SetData {
        Input opengateInput;
        List<OperationSetDeviceParameters.VariableValue> parsedInput;
        OperationSetDeviceParameters.Result operationResult;
        Output opengateOutput;
    }

    @Value
    private static class RefreshInfoData {
        Input opengateInput;
        OperationRefreshInfo.Result operationResult;
        Output opengateOutput;
    }

    @Value
    private static class UpdateData {
        Input opengateInput;
        String bundleName;
        String bundleVersion;
        List<OperationUpdate.DeploymentElement> deploymentElements;
        OperationUpdate.Result operationResult;
        Output opengateOutput;
    }
}
