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

public class OpenGateOperationDispatcherTest {

    private static final String ODA_DEVICE_ID = "odaDeviceId";
    private static final byte[] INPUT = "whatever".getBytes();
    private static final byte[] OUTPUT = "outputBytes".getBytes();
    private static final String OPERATION_ID = "anOperationId";
    private static final Long TIMESTAMP = null;
    private static final GetData GET_DATA = getDataConstants();
    private static final SetData SET_DATA = setDataConstants();
    private static final RefreshInfoData REFRESH_INFO_DATA = refreshInfoDataConstants();
    private static final UpdateData UPDATE_DATA = updateDataConstants();

    private OpenGateOperationDispatcher dispatcher;
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
                OPERATION_ID,
                null,
                TIMESTAMP,
                Collections.singletonList(
                        new RequestGetDeviceParameters.Parameter("variableList",
                                new RequestGetDeviceParameters.ValueArray(
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
                                OPERATION_ID,
                                ODA_DEVICE_ID,
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
                OPERATION_ID,
                null,
                TIMESTAMP,
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
                                OPERATION_ID,
                                ODA_DEVICE_ID,
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
        Input opengateRefreshInfoRequest = new Input(new InputOperation(new RequestRefreshInfo(OPERATION_ID, null, TIMESTAMP)));
        HashMap<String, Object> obtainedValues = new HashMap<>();
        obtainedValues.put("id2", 42);
        obtainedValues.put("id3", "hi");
        OperationRefreshInfo.Result operationRefreshInfoResult = new OperationRefreshInfo.Result(obtainedValues);
        Output operationRefreshInfoResultAsOpenGateStructure = new Output(
                "8.0",
                new OutputOperation(
                        new Response(
                                OPERATION_ID,
                                ODA_DEVICE_ID,
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
                OPERATION_ID,
                null,
                TIMESTAMP,
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
                                OPERATION_ID,
                                ODA_DEVICE_ID,
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

        dispatcher = new OpenGateOperationDispatcher(
                jsonParser,
                jsonWriter,
                deviceInfoProvider,
                operationGetDeviceParameters,
                operationSetDeviceParameters,
                operationRefreshInfo,
                operationUpdate
        );
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestRefreshInfo(OPERATION_ID, null, TIMESTAMP))));

        futureForOperationGet = new CompletableFuture<>();
        futureForOperationSet = new CompletableFuture<>();
        futureForOperationRefreshInfo = new CompletableFuture<>();
        futureForOperationUpdate = new CompletableFuture<>();

        when(operationGetDeviceParameters.getDeviceParameters("", GET_DATA.getParsedInput())).thenReturn(futureForOperationGet);
        when(operationSetDeviceParameters.setDeviceParameters("", SET_DATA.getParsedInput())).thenReturn(futureForOperationSet);
        when(operationRefreshInfo.refreshInfo("")).thenReturn(futureForOperationRefreshInfo);
        when(operationUpdate.update(UPDATE_DATA.getBundleName(), UPDATE_DATA.getBundleVersion(), UPDATE_DATA.getDeploymentElements())).thenReturn(futureForOperationUpdate);

        when(deviceInfoProvider.getDeviceId()).thenReturn(ODA_DEVICE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void withNullParameterThrows() {
        dispatcher.process(null);
    }

    @Test
    public void jsonParserIsUsedToParseIncomingMessage() {
        dispatcher.process(INPUT);

        verify(jsonParser).parseInput(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifParserReturnsNullThrows() {
        when(jsonParser.parseInput(INPUT)).thenReturn(null);

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifParserReturnsAnInputWithNullOperation_Throws() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(null));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ifParserReturnsAnInputWithNullRequest_Throws() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(null)));

        dispatcher.process(INPUT);
    }

    //-----------------------------------------------------
    // -- GET_DEVICE_PARAMETERS
    //-----------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void getOperationMustHaveParameters() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                null
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationMustHaveExactlyOneElementInParameters() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestGetDeviceParameters.Parameter("unknown", null),
                        new RequestGetDeviceParameters.Parameter("unknown", null)
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationMustHaveExactlyOneElementNotNullInParameters() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Collections.singletonList(null)
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationMustHaveExactlyTheParameterVariableListInParameter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Collections.singletonList(
                        new RequestGetDeviceParameters.Parameter("unknown", null)
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationVariableListMustHaveValue() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Collections.singletonList(
                        new RequestGetDeviceParameters.Parameter("variableList", null)
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getOperationVariableListMustHaveValueWithArray() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestGetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Collections.singletonList(
                        new RequestGetDeviceParameters.Parameter("variableList", new RequestGetDeviceParameters.ValueArray())
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test
    public void getOperationsAreDispatchedToOperationGetDeviceParameters() {
        when(jsonParser.parseInput(INPUT)).thenReturn(GET_DATA.getOpengateInput());

        dispatcher.process(INPUT);

        verify(operationGetDeviceParameters).getDeviceParameters("", GET_DATA.getParsedInput());
    }

    @Test
    public void ifOperationGetDeviceParametersReturnNullANotSupportedOperationOutputIsGivenToJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(GET_DATA.getOpengateInput());
        when(operationGetDeviceParameters.getDeviceParameters("", GET_DATA.getParsedInput())).thenReturn(null);
        Response notSupportedResponse = new Response(OPERATION_ID, ODA_DEVICE_ID, "GET_DEVICE_PARAMETERS", OperationResultCode.NOT_SUPPORTED, "Operation not supported by the device", null);
        Output expected = new Output("8.0", new OutputOperation(notSupportedResponse));

        CompletableFuture<byte[]> future = dispatcher.process(INPUT);

        assertTrue(future.isDone());
        verify(jsonWriter).dumpOutput(eq(expected));
    }

    @Test
    public void aCompletableFutureIsReturnedThatWillBeCompletedWhenTheOperationGetIsCompleted() {
        when(jsonParser.parseInput(INPUT)).thenReturn(GET_DATA.getOpengateInput());

        CompletableFuture<byte[]> future = dispatcher.process(INPUT);
        assertFalse(future.isDone());

        futureForOperationGet.complete(null);
        assertTrue(future.isDone());
    }

    @Test
    public void whenTheOperationGetDeviceParametersCompletesJsonWriterIsUsedToDumpTheResponse() {
        when(jsonParser.parseInput(INPUT)).thenReturn(GET_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationGet.complete(null);

        verify(jsonWriter).dumpOutput(isA(Output.class));
    }

    @Test
    public void ifGetOperationResultIsNullAnErrorOutputIsInjectedInJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(GET_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationGet.complete(null);

        List<Step> steps = Collections.singletonList(new Step("GET_DEVICE_PARAMETERS", StepResultCode.ERROR, "NullPointerException: null", 0L, null));
        OutputOperation operation = new OutputOperation(new Response(OPERATION_ID, ODA_DEVICE_ID, "GET_DEVICE_PARAMETERS", OperationResultCode.ERROR_PROCESSING, "NullPointerException: null", steps));
        Output expected = new Output("8.0", operation);
        verify(jsonWriter).dumpOutput(expected);
    }

    @Test
    public void getOperationResultIsTranslatedToOutputAndInjectedInJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(GET_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationGet.complete(GET_DATA.getOperationResult());

        verify(jsonWriter).dumpOutput(GET_DATA.getOpengateOutput());
    }

    @Test
    public void inAGetOperationTheByteArrayReturnedByTheJsonWriterIsReturnedInTheDispatcherFuture() throws InterruptedException, ExecutionException {
        when(jsonParser.parseInput(INPUT)).thenReturn(GET_DATA.getOpengateInput());
        when(jsonWriter.dumpOutput(GET_DATA.getOpengateOutput())).thenReturn(OUTPUT);

        CompletableFuture<byte[]> dispatcherFuture = dispatcher.process(INPUT);
        futureForOperationGet.complete(GET_DATA.getOperationResult());

        assertTrue(dispatcherFuture.isDone());
        byte[] actual = dispatcherFuture.get();
        assertEquals(OUTPUT, actual);
    }

    //-----------------------------------------------------
    // -- REFRESH_INFO
    //-----------------------------------------------------
    @Test
    public void ifOperationRefreshInfoReturnsNullANotSupportedOperationOutputIsGivenToJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(REFRESH_INFO_DATA.getOpengateInput());
        when(operationRefreshInfo.refreshInfo("")).thenReturn(null);
        Response notSupportedResponse = new Response(OPERATION_ID, ODA_DEVICE_ID, "REFRESH_INFO", OperationResultCode.NOT_SUPPORTED, "Operation not supported by the device", null);
        Output expected = new Output("8.0", new OutputOperation(notSupportedResponse));

        CompletableFuture<byte[]> future = dispatcher.process(INPUT);

        assertTrue(future.isDone());
        verify(jsonWriter).dumpOutput(eq(expected));
    }

    @Test
    public void refreshInfoOperationsAreDispatchedToOperationRefreshInfo() {
        when(jsonParser.parseInput(INPUT)).thenReturn(REFRESH_INFO_DATA.getOpengateInput());

        dispatcher.process(INPUT);

        verify(operationRefreshInfo).refreshInfo("");
    }

    @Test
    public void aCompletableFutureIsReturnedThatWillBeCompletedWhenTheOperationRefreshInfoIsCompleted() {
        when(jsonParser.parseInput(INPUT)).thenReturn(REFRESH_INFO_DATA.getOpengateInput());

        CompletableFuture<byte[]> future = dispatcher.process(INPUT);
        assertFalse(future.isDone());

        futureForOperationRefreshInfo.complete(REFRESH_INFO_DATA.getOperationResult());
        assertTrue(future.isDone());
    }

    @Test
    public void whenTheOperationRefreshInfoCompletesJsonWriterIsUsedToDumpTheResponse() {
        when(jsonParser.parseInput(INPUT)).thenReturn(REFRESH_INFO_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationRefreshInfo.complete(REFRESH_INFO_DATA.getOperationResult());

        verify(jsonWriter).dumpOutput(isA(Output.class));
    }

    @Test
    public void theResultOfARefreshInfoIsTranslatedAndPassedToJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(REFRESH_INFO_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationRefreshInfo.complete(REFRESH_INFO_DATA.getOperationResult());

        verify(jsonWriter).dumpOutput(REFRESH_INFO_DATA.getOpengateOutput());
    }

    @Test
    public void ifRefreshInfoResultIsNullAnErrorOutputIsInjectedInJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(REFRESH_INFO_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationRefreshInfo.complete(null);

        List<Step> steps = Collections.singletonList(new Step("REFRESH_INFO", StepResultCode.ERROR, "NullPointerException: null", 0L, null));
        OutputOperation operation = new OutputOperation(new Response(OPERATION_ID, ODA_DEVICE_ID, "REFRESH_INFO", OperationResultCode.ERROR_PROCESSING, "NullPointerException: null", steps));
        Output expected = new Output("8.0", operation);
        verify(jsonWriter).dumpOutput(expected);
    }

    @Test
    public void inARefreshInfoTheByteArrayReturnedByTheJsonWriterIsReturnedInTheDispatcherFuture() throws InterruptedException, ExecutionException {
        when(jsonParser.parseInput(INPUT)).thenReturn(REFRESH_INFO_DATA.getOpengateInput());
        when(jsonWriter.dumpOutput(REFRESH_INFO_DATA.getOpengateOutput())).thenReturn(OUTPUT);

        CompletableFuture<byte[]> dispatcherFuture = dispatcher.process(INPUT);
        futureForOperationRefreshInfo.complete(REFRESH_INFO_DATA.getOperationResult());

        assertTrue(dispatcherFuture.isDone());
        byte[] actual = dispatcherFuture.get();
        assertEquals(OUTPUT, actual);
    }

    //-----------------------------------------------------
    // -- SET_DEVICE_PARAMETERS
    //-----------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void setOperationMustHaveParameters() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                null
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperationMustHaveExactlyOneElementInParameters() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestSetDeviceParameters.Parameter("unknown", null),
                        new RequestSetDeviceParameters.Parameter("unknown", null)
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperationMustHaveExactlyOneElementNotNullInParameters() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Collections.singletonList(null)
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperationMustHaveExactlyTheParameterVariableListInParameter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Collections.singletonList(
                        new RequestSetDeviceParameters.Parameter("unknown", null)
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperationVariableListMustHaveValue() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Collections.singletonList(
                        new RequestSetDeviceParameters.Parameter("variableList", null)
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setOperationVariableListMustHaveValueWithArray() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestSetDeviceParameters(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Collections.singletonList(
                        new RequestSetDeviceParameters.Parameter("variableList", new RequestSetDeviceParameters.ValueArray())
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test
    public void ifOperationSetDeviceParametersReturnsNullANotSupportedOperationOutputIsGivenToJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(SET_DATA.getOpengateInput());
        when(operationSetDeviceParameters.setDeviceParameters("", SET_DATA.getParsedInput())).thenReturn(null);
        Response notSupportedResponse = new Response(OPERATION_ID, ODA_DEVICE_ID, "SET_DEVICE_PARAMETERS", OperationResultCode.NOT_SUPPORTED, "Operation not supported by the device", null);
        Output expected = new Output("8.0", new OutputOperation(notSupportedResponse));

        CompletableFuture<byte[]> future = dispatcher.process(INPUT);

        assertTrue(future.isDone());
        verify(jsonWriter).dumpOutput(eq(expected));
    }

    @Test
    public void setOperationsAreDispatchedToOperationSet() {
        when(jsonParser.parseInput(INPUT)).thenReturn(SET_DATA.getOpengateInput());

        dispatcher.process(INPUT);

        verify(operationSetDeviceParameters).setDeviceParameters("", SET_DATA.getParsedInput());
    }

    @Test
    public void aCompletableFutureIsReturnedThatWillBeCompletedWhenTheOperationSetIsCompleted() {
        when(jsonParser.parseInput(INPUT)).thenReturn(SET_DATA.getOpengateInput());

        CompletableFuture<byte[]> future = dispatcher.process(INPUT);
        assertFalse(future.isDone());

        futureForOperationSet.complete(null);
        assertTrue(future.isDone());
    }

    @Test
    public void whenTheOperationSetDeviceParametersCompletesJsonWriterIsUsedToDumpTheResponse() {
        when(jsonParser.parseInput(INPUT)).thenReturn(SET_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationSet.complete(null);

        verify(jsonWriter).dumpOutput(isA(Output.class));
    }

    @Test
    public void ifSetOperationResultIsNullAnErrorOutputIsInjectedInJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(SET_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationSet.complete(null);

        List<Step> steps = Collections.singletonList(new Step("SET_DEVICE_PARAMETERS", StepResultCode.ERROR, "NullPointerException: null", 0L, null));
        OutputOperation operation = new OutputOperation(new Response(OPERATION_ID, ODA_DEVICE_ID, "SET_DEVICE_PARAMETERS", OperationResultCode.ERROR_PROCESSING, "NullPointerException: null", steps));
        Output expected = new Output("8.0", operation);
        verify(jsonWriter).dumpOutput(expected);
    }

    @Test
    public void setOperationResultIsTranslatedToOutputAndInjectedInJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(SET_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationSet.complete(SET_DATA.getOperationResult());

        verify(jsonWriter).dumpOutput(SET_DATA.getOpengateOutput());
    }

    @Test
    public void ifSetFinishedWithErrorsThatErrorsAreTranslatedToOutput() {
        when(jsonParser.parseInput(INPUT)).thenReturn(SET_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        String resultDescription = "An error description";
        OperationSetDeviceParameters.Result operationResult = new OperationSetDeviceParameters.Result(OperationSetDeviceParameters.ResultCode.ERROR_IN_PARAM, resultDescription, null);
        futureForOperationSet.complete(operationResult);

        List<Step> steps = Collections.singletonList(new Step("SET_DEVICE_PARAMETERS", StepResultCode.ERROR, resultDescription, 0L, null));
        Response response = new Response(OPERATION_ID, ODA_DEVICE_ID, "SET_DEVICE_PARAMETERS", OperationResultCode.ERROR_IN_PARAM, resultDescription, steps);
        OutputOperation operation = new OutputOperation(response);
        Output opengateOutput = new Output("8.0", operation);
        verify(jsonWriter).dumpOutput(opengateOutput);
    }

    @Test
    public void inASetOperationTheByteArrayReturnedByTheJsonWriterIsReturnedInTheDispatcherFuture() throws InterruptedException, ExecutionException {
        when(jsonParser.parseInput(INPUT)).thenReturn(SET_DATA.getOpengateInput());
        when(jsonWriter.dumpOutput(SET_DATA.getOpengateOutput())).thenReturn(OUTPUT);

        CompletableFuture<byte[]> dispatcherFuture = dispatcher.process(INPUT);
        futureForOperationSet.complete(SET_DATA.getOperationResult());

        assertTrue(dispatcherFuture.isDone());
        byte[] actual = dispatcherFuture.get();
        assertEquals(OUTPUT, actual);
    }

    //-----------------------------------------------------
    // -- UPDATE
    //-----------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveParameters() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                OPERATION_ID,
                null,
                TIMESTAMP,
                null
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveExactlyThreeElementsInParameters() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestUpdate.Parameter("unknown", null),
                        new RequestUpdate.Parameter("unknown", null)
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveExactlyThreeNotNullElementsInParameters() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestUpdate.Parameter("unknown", null),
                        new RequestUpdate.Parameter("unknown", null),
                        null
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveBundleNameParameter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestUpdate.Parameter("should be bundleName", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType())
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveBundleVersionParameter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("should be bundleVersion", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType())
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveDeploymentElementsParameter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType()),
                        new RequestUpdate.Parameter("should be deploymentElements", new RequestUpdate.ValueType())
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustBundleNameParameterOfTheCorrectType() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType(null, Collections.singletonList(new RequestUpdate.VariableListElement()))),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType(null, Collections.singletonList(new RequestUpdate.VariableListElement("", "", DeploymentElementType.SOFTWARE, "", "", DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 1L))))
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustBundleVersionParameterOfTheCorrectType() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType(null, Collections.singletonList(new RequestUpdate.VariableListElement()))),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType(null, Collections.singletonList(new RequestUpdate.VariableListElement("", "", DeploymentElementType.SOFTWARE, "", "", DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 1L))))
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateOperationMustHaveDeploymentElementsParameterOfTheCorrectType() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType("", null))
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void inAnUpdateOperationTheParameterDeploymentElementsMustHaveAtLeastOneNonNullElement() {
        when(jsonParser.parseInput(INPUT)).thenReturn(new Input(new InputOperation(new RequestUpdate(
                OPERATION_ID,
                null,
                TIMESTAMP,
                Arrays.asList(
                        new RequestUpdate.Parameter("bundleName", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("bundleVersion", new RequestUpdate.ValueType("", null)),
                        new RequestUpdate.Parameter("deploymentElements", new RequestUpdate.ValueType(null, Collections.singletonList(null)))
                )
        ))));

        dispatcher.process(INPUT);
    }

    @Test
    public void updateOperationsAreDispatchedToOperationUpdate() {
        when(jsonParser.parseInput(INPUT)).thenReturn(UPDATE_DATA.getOpengateInput());

        dispatcher.process(INPUT);

        verify(operationUpdate).update(UPDATE_DATA.getBundleName(), UPDATE_DATA.getBundleVersion(), UPDATE_DATA.getDeploymentElements());
    }

    @Test
    public void ifOperationUpdateReturnsNullANotSupportedOperationOutputIsGivenToJonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(UPDATE_DATA.getOpengateInput());
        when(operationUpdate.update(UPDATE_DATA.getBundleName(), UPDATE_DATA.getBundleVersion(), UPDATE_DATA.getDeploymentElements())).thenReturn(null);
        Response notSupportedResponse = new Response(OPERATION_ID, ODA_DEVICE_ID, "UPDATE", OperationResultCode.NOT_SUPPORTED, "Operation not supported by the device", null);
        Output expected = new Output("8.0", new OutputOperation(notSupportedResponse));

        CompletableFuture<byte[]> future = dispatcher.process(INPUT);

        assertTrue(future.isDone());
        verify(jsonWriter).dumpOutput(eq(expected));
    }

    @Test
    public void aCompletableFutureIsReturnedThatWillBeCompletedWhenTheOperationUpdateIsCompleted() {
        when(jsonParser.parseInput(INPUT)).thenReturn(UPDATE_DATA.getOpengateInput());

        CompletableFuture<byte[]> future = dispatcher.process(INPUT);
        assertFalse(future.isDone());

        futureForOperationUpdate.complete(null);
        assertTrue(future.isDone());
    }

    @Test
    public void whenTheOperationUpdateCompletesJsonWriterIsUsedToDumpTheResponse() {
        when(jsonParser.parseInput(INPUT)).thenReturn(UPDATE_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationUpdate.complete(null);

        verify(jsonWriter).dumpOutput(isA(Output.class));
    }

    @Test
    public void ifUpdateOperationResultIsNullAnErrorOutputIsInjectedInJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(UPDATE_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationUpdate.complete(null);

        List<Step> steps = Collections.singletonList(new Step("UPDATE", StepResultCode.ERROR, "NullPointerException: null", 0L, null));
        OutputOperation operation = new OutputOperation(new Response(OPERATION_ID, ODA_DEVICE_ID, "UPDATE", OperationResultCode.ERROR_PROCESSING, "NullPointerException: null", steps));
        Output expected = new Output("8.0", operation);
        verify(jsonWriter).dumpOutput(expected);
    }

    @Test
    public void updateOperationResultIsTranslatedToOutputAndInjectedInJsonWriter() {
        when(jsonParser.parseInput(INPUT)).thenReturn(UPDATE_DATA.getOpengateInput());

        dispatcher.process(INPUT);
        futureForOperationUpdate.complete(UPDATE_DATA.getOperationResult());

        verify(jsonWriter).dumpOutput(UPDATE_DATA.getOpengateOutput());
    }

    @Test
    public void inAnUpdateOperationTheByteArrayReturnedByTheJsonWriterIsReturnedInTheDispatcherFuture() throws InterruptedException, ExecutionException {
        when(jsonParser.parseInput(INPUT)).thenReturn(UPDATE_DATA.getOpengateInput());
        when(jsonWriter.dumpOutput(UPDATE_DATA.getOpengateOutput())).thenReturn(OUTPUT);

        CompletableFuture<byte[]> dispatcherFuture = dispatcher.process(INPUT);
        futureForOperationUpdate.complete(UPDATE_DATA.getOperationResult());

        assertTrue(dispatcherFuture.isDone());
        byte[] actual = dispatcherFuture.get();
        assertEquals(OUTPUT, actual);
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
