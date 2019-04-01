package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.operation.api.OperationGetDeviceParameters;
import es.amplia.oda.operation.api.OperationRefreshInfo;
import es.amplia.oda.operation.api.OperationSetDeviceParameters;
import es.amplia.oda.operation.api.OperationUpdate;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

class OpenGateOperationDispatcher implements Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(OpenGateOperationDispatcher.class);

    private static final String UPDATE_OPERATION = "UPDATE";
    private static final String REFRESH_INFO_OPERATION = "REFRESH_INFO";
    private static final String GET_DEVICE_PARAMETERS_OPERATION = "GET_DEVICE_PARAMETERS";
    private static final String SET_DEVICE_PARAMETERS_OPERATION = "SET_DEVICE_PARAMETERS";

    private static final String SUCCESS_RESULT = "SUCCESS";
    private static final String ERROR_RESULT = "ERROR";

    private final Serializer serializer;
    private final OperationGetDeviceParameters operationGetDeviceParameters;
    private final OperationRefreshInfo operationRefreshInfo;
    private final OperationSetDeviceParameters operationSetDeviceParameters;
    private final OperationUpdate operationUpdate;
    private final DeviceInfoProvider deviceInfoProvider;

    OpenGateOperationDispatcher(
            Serializer serializer,
            DeviceInfoProvider deviceInfoProvider,
            OperationGetDeviceParameters operationGetDeviceParameters,
            OperationSetDeviceParameters operationSetDeviceParameters,
            OperationRefreshInfo operationRefreshInfo,
            OperationUpdate operationUpdate) {
        this.serializer = serializer;
        this.deviceInfoProvider = deviceInfoProvider;
        this.operationGetDeviceParameters = operationGetDeviceParameters;
        this.operationSetDeviceParameters = operationSetDeviceParameters;
        this.operationRefreshInfo = operationRefreshInfo;
        this.operationUpdate = operationUpdate;
    }

    private static Set<String> parseInput(RequestGetDeviceParameters requestGetDeviceParameters) {
        if (requestGetDeviceParameters.getParameters() == null) {
            throw new IllegalArgumentException("No parameters in GET_DEVICE_PARAMETERS");
        } else if (requestGetDeviceParameters.getParameters().size() != 1) {
            throw new IllegalArgumentException("Expected only one parameter in GET_DEVICE_PARAMETERS");
        }

        RequestGetDeviceParameters.Parameter param = requestGetDeviceParameters.getParameters().get(0);
        if (param == null) {
            throw new IllegalArgumentException("Null parameter in GET_DEVICE_PARAMETERS");
        }
        if (!param.getName().equals("variableList")){
            throw new IllegalArgumentException("Illegal parameter in GET_DEVICE_PARAMETERS");
        }
        RequestGetDeviceParameters.ValueArray valueVariableList = param.getValue();
        if (valueVariableList == null){
            throw new IllegalArgumentException("Null value of variableList in GET_DEVICE_PARAMETERS");
        }
        if (valueVariableList.getArray() == null){
            throw new IllegalArgumentException("Null value of array in variableList in GET_DEVICE_PARAMETERS");
        }

        return valueVariableList.getArray().stream()
                .map(RequestGetDeviceParameters.VariableListElement::getVariableName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private static UpdateParameters parseInput(RequestUpdate requestUpdate) {
        if (requestUpdate.getParameters() == null) {
            throw new IllegalArgumentException("No parameters in UPDATE");
        }
        Map<String, RequestUpdate.ValueType> params = requestUpdate.getParameters().stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getName() != null)
                .filter(p -> p.getValue() != null)
                .collect(Collectors.toMap(RequestUpdate.Parameter::getName, RequestUpdate.Parameter::getValue)
                );

        if (params.size() != 3) {
            throw new IllegalArgumentException("Expected three parameters in UPDATE");
        }

        RequestUpdate.ValueType bundleNameStruct = params.get("bundleName");
        RequestUpdate.ValueType bundleVersionStruct = params.get("bundleVersion");
        RequestUpdate.ValueType deploymentElementsStruct = params.get("deploymentElements");

        if (bundleNameStruct == null) {
            throw new IllegalArgumentException("Parameter bundleName not found");
        }
        if (bundleVersionStruct == null) {
            throw new IllegalArgumentException("Parameter bundleVersion not found");
        }
        if (deploymentElementsStruct == null){
            throw new IllegalArgumentException("Parameter deploymentElements not found");
        }

        String bundleName = bundleNameStruct.getString();
        String bundleVersion = bundleVersionStruct.getString();
        if (bundleName == null) {
            throw new IllegalArgumentException("Parameter bundleName of incorrect type");
        }
        if (bundleVersion == null) {
            throw new IllegalArgumentException("Parameter bundleVersion of incorrect type");
        }
        if (deploymentElementsStruct.getArray() == null) {
            throw new IllegalArgumentException("Parameter deploymentElements of incorrect type");
        }
        List<OperationUpdate.DeploymentElement> deploymentElements = deploymentElementsStruct.getArray().stream()
                .filter(Objects::nonNull)
                .map(p -> new OperationUpdate.DeploymentElement(p.getName(), p.getVersion(), p.getType(),
                        p.getDownloadUrl(), p.getPath(), p.getOperation(), p.getOption(), p.getOrder()))
                .collect(Collectors.toList());
        if (deploymentElements.isEmpty()){
            throw new IllegalArgumentException("Parameter deploymentElements must have at least one not null element");
        }

        return new UpdateParameters(bundleName, bundleVersion, deploymentElements);
    }

    private static OutputVariable translateGetResultToOutput(OperationGetDeviceParameters.GetValue v) {
        switch (v.getStatus()) {
            case OK:
                return new OutputVariable(v.getDatastreamId(), v.getValue(), SUCCESS_RESULT, SUCCESS_RESULT);
            case PROCESSING_ERROR:
                return new OutputVariable(v.getDatastreamId(), null, ERROR_RESULT, v.getError());
            case NOT_FOUND:
            default:
                return new OutputVariable(v.getDatastreamId(), null, "NON_EXISTENT", "No datastream found");
        }
    }

    private static Output translateToOutputSet(OperationSetDeviceParameters.Result result, String operationId,
                                               String deviceId) {
        try {
            if (result.getResulCode() == OperationSetDeviceParameters.ResultCode.ERROR_IN_PARAM) {
                List<Step> steps =
                        Collections.singletonList(new Step(SET_DEVICE_PARAMETERS_OPERATION, StepResultCode.ERROR,
                                result.getResultDescription(), 0L, null));
                OutputOperation operation =
                        new OutputOperation(new Response(operationId, deviceId, SET_DEVICE_PARAMETERS_OPERATION,
                                OperationResultCode.ERROR_IN_PARAM, result.getResultDescription(), steps));
                return new Output(OPENGATE_VERSION, operation);
            } else {
                List<OutputVariable> outputVariables = result.getVariables().stream()
                        .map(OpenGateOperationDispatcher::translate)
                        .collect(Collectors.toList());
                List<Step> steps =
                        Collections.singletonList(new Step(SET_DEVICE_PARAMETERS_OPERATION, StepResultCode.SUCCESSFUL,
                                "", 0L, outputVariables));
                OutputOperation operation =
                        new OutputOperation(new Response(operationId, deviceId, SET_DEVICE_PARAMETERS_OPERATION,
                                OperationResultCode.SUCCESSFUL, result.getResultDescription(), steps));
                return new Output(OPENGATE_VERSION, operation);
            }
        } catch (Exception e) {
            return translateThrowableToOutput(SET_DEVICE_PARAMETERS_OPERATION, operationId, deviceId, e);
        }
    }

    private static OutputVariable translate(OperationSetDeviceParameters.VariableResult vr) {
        if (vr.getError() != null) {
            return new OutputVariable(vr.getIdentifier(), null, ERROR_RESULT, vr.getError());
        }
        return new OutputVariable(vr.getIdentifier(), null, SUCCESS_RESULT, SUCCESS_RESULT);
    }

    private static Output translateToOutputUpdate(OperationUpdate.Result result, String operationId, String deviceId) {
        try {
            List<Step> steps = result.getSteps().stream()
                    .map(r -> new Step(translate(r.getName()), translate(r.getCode()), r.getDescription(), 0L, null))
                    .collect(Collectors.toList());
            OutputOperation operation =
                    new OutputOperation(new Response(operationId, deviceId, UPDATE_OPERATION,
                            translate(result.getResultCode()), result.getResultDescription(), steps));
            return new Output(OPENGATE_VERSION, operation);
        } catch (Exception e) {
            return translateThrowableToOutput(UPDATE_OPERATION, operationId, deviceId, e);
        }
    }

    private static String translate(OperationUpdate.UpdateStepName name) {
        switch (name) {
            case BEGINUPDATE:
                return "BEGINUPDATE";
            case ENDUPDATE:
                return "ENDUPDATE";
            case DOWNLOADFILE:
                return "DOWNLOADFILE";
            case BEGININSTALL:
                return "BEGININSTALL";
            case ENDINSTALL:
                return "ENDINSTALL";
            default:
                return "UNKNOWN";
        }
    }

    private static OperationResultCode translate(OperationUpdate.OperationResultCodes resultCode) {
        switch (resultCode) {
            case SUCCESSFUL:
                return OperationResultCode.SUCCESSFUL;
            case OPERATION_PENDING:
                return OperationResultCode.OPERATION_PENDING;
            case ERROR_IN_PARAM:
                return OperationResultCode.ERROR_IN_PARAM;
            case ALREADY_IN_PROGRESS:
                return OperationResultCode.ALREADY_IN_PROGRESS;
            case ERROR_PROCESSING:
                return OperationResultCode.ERROR_PROCESSING;
            case ERROR_TIMEOUT:
                return OperationResultCode.ERROR_TIMEOUT;
            case TIMEOUT_CANCELLED:
                return OperationResultCode.TIMEOUT_CANCELLED;
            case CANCELLED:
                return OperationResultCode.CANCELLED;
            case CANCELLED_INTERNAL:
                return OperationResultCode.CANCELLED_INTERNAL;
            case NOT_SUPPORTED:
            default:
                return OperationResultCode.NOT_SUPPORTED;
        }
    }

    private static StepResultCode translate(OperationUpdate.StepResultCodes result) {
        switch (result) {
            case SUCCESSFUL:
                return StepResultCode.SUCCESSFUL;
            case NOT_EXECUTED:
                return StepResultCode.NOT_EXECUTED;
            case SKIPPED:
                return StepResultCode.SKIPPED;
            case ERROR:
            default:
                return StepResultCode.ERROR;
        }
    }

    private static Output translateThrowableToOutput(String operationName, String operationId, String deviceId,
                                                     Throwable e) {
        String errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
        List<Step> steps = Collections.singletonList(new Step(operationName, StepResultCode.ERROR, errorMsg, 0L, null));
        OutputOperation operation =
                new OutputOperation(new Response(operationId, deviceId, operationName,
                        OperationResultCode.ERROR_PROCESSING, errorMsg, steps));
        return new Output(OPENGATE_VERSION, operation);
    }

    @Override
    public CompletableFuture<byte[]> process(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("null parameter");
        }
        Input parsedInput = null;
        try {
            parsedInput = serializer.deserialize(input, Input.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("couldn't deserialize input");
        }
        if (parsedInput == null) {
            throw new IllegalArgumentException("null result of parsing");
        }
        if (parsedInput.getOperation() == null) {
            throw new IllegalArgumentException("null operation in result of parsing");
        }
        Request request = parsedInput.getOperation().getRequest();
        if (request == null) {
            throw new IllegalArgumentException("null request in operation in result of parsing");
        }

        final String deviceIdForOperations;
        final String deviceIdForResponse;
        final String deviceIdInRequest = request.getDeviceId();
        final String odaDeviceId = deviceInfoProvider.getDeviceId();
        if (deviceIdInRequest == null || deviceIdInRequest.equals("") || deviceIdInRequest.equals(odaDeviceId)) {
            deviceIdForOperations = "";
            deviceIdForResponse = odaDeviceId;
        } else {
            deviceIdForOperations = deviceIdInRequest;
            deviceIdForResponse = deviceIdInRequest;
        }

        return processSpecificRequest(request, deviceIdForOperations, deviceIdForResponse);
    }

    private CompletableFuture<byte[]> processSpecificRequest(Request request, String deviceIdForOperations,
                                                             String deviceIdForResponse) {
        CompletableFuture<byte[]> returnedFuture = new CompletableFuture<>();
        request.accept(new RequestVisitor() {
            @Override
            public void visit(RequestUpdate requestUpdate) {
                UpdateParameters parsedUpdate = parseInput(requestUpdate);
                CompletableFuture<OperationUpdate.Result> future =
                        operationUpdate.update(parsedUpdate.getBundleName(), parsedUpdate.getBundleVersion(),
                                parsedUpdate.getDeploymentElements());
                if (future == null) {
                    returnedFuture.complete(noOperationFor(request.getId(), UPDATE_OPERATION, deviceIdForResponse));
                } else {
                    future.thenAccept(result -> {
                        Output output = translateToOutputUpdate(result, request.getId(), deviceIdForResponse);
                        byte[] resultAsBytes = new byte[0];
                        try {
                            resultAsBytes = serializer.serialize(output);
                        } catch (IOException e) {
                            logger.error("Error serializing response message. Will send void bytearray as result");
                        }
                        returnedFuture.complete(resultAsBytes);
                    });
                }
            }

            @Override
            public void visit(RequestRefreshInfo requestRefreshInfo) {
                CompletableFuture<OperationRefreshInfo.Result> future =
                        operationRefreshInfo.refreshInfo(deviceIdForOperations);
                if (future == null) {
                    returnedFuture.complete(noOperationFor(request.getId(), REFRESH_INFO_OPERATION,
                            deviceIdForResponse));
                } else {
                    future.thenAccept(result -> {
                        Output output = translateToOutputRefresh(result, request.getId(), deviceIdForResponse);
                        byte[] resultAsBytes = new byte[0];
                        try {
                            resultAsBytes = serializer.serialize(output);
                        } catch (IOException e) {
                            logger.error("Error serializing response message. Will send void bytearray as result");
                        }
                        returnedFuture.complete(resultAsBytes);
                    });
                }
            }

            @Override
            public void visit(RequestGetDeviceParameters requestGetDeviceParameters) {
                Set<String> dataStreamIds = parseInput(requestGetDeviceParameters);
                CompletableFuture<OperationGetDeviceParameters.Result> future =
                        operationGetDeviceParameters.getDeviceParameters(deviceIdForOperations, dataStreamIds);
                if (future == null) {
                    returnedFuture.complete(noOperationFor(request.getId(), GET_DEVICE_PARAMETERS_OPERATION,
                            deviceIdForResponse));
                } else {
                    future.thenAccept(result -> {
                        Output output = translateToOutputGet(result, request.getId(), deviceIdForResponse);
                        byte[] resultAsBytes = new byte[0];
                        try {
                            resultAsBytes = serializer.serialize(output);
                        } catch (IOException e) {
                            logger.error("Error serializing response message. Will send void bytearray as result");
                        }
                        returnedFuture.complete(resultAsBytes);
                    });
                }
            }

            @Override
            public void visit(RequestSetDeviceParameters requestSetDeviceParameters) {
                List<OperationSetDeviceParameters.VariableValue> values = parseInput(requestSetDeviceParameters);
                CompletableFuture<OperationSetDeviceParameters.Result> future =
                        operationSetDeviceParameters.setDeviceParameters(deviceIdForOperations, values);
                if (future == null) {
                    returnedFuture.complete(noOperationFor(request.getId(), SET_DEVICE_PARAMETERS_OPERATION,
                            deviceIdForResponse));
                } else {
                    future.thenAccept(result -> {
                        Output output = translateToOutputSet(result, request.getId(), deviceIdForResponse);
                        byte[] resultAsBytes = new byte[0];
                        try {
                            resultAsBytes = serializer.serialize(output);
                        } catch (IOException e) {
                            logger.error("Error serializing response message. Will send void bytearray as result");
                        }
                        returnedFuture.complete(resultAsBytes);
                    });
                }
            }

            @Override
            public void visit(RequestOperationNotSupported requestOperationNotSupported) {
                returnedFuture.complete(noOperationFor(request.getId(), requestOperationNotSupported.getOperationName(),
                        deviceIdForResponse));
            }
        });
        return returnedFuture;
    }

    private byte[] noOperationFor(String operationId, String operation, String deviceId) {
        Response notSupportedResponse =
                new Response(operationId, deviceId, operation, OperationResultCode.NOT_SUPPORTED,
                        "Operation not supported by the device", null);
        Output output = new Output(OPENGATE_VERSION, new OutputOperation(notSupportedResponse));

        try {
            return serializer.serialize(output);
        } catch (IOException e) {
            logger.error("Error serializing response message. Will send void bytearray as result");
            return new byte[0];
        }
    }

    private List<OperationSetDeviceParameters.VariableValue> parseInput(
            RequestSetDeviceParameters requestSetDeviceParameters) {
        if (requestSetDeviceParameters.getParameters() == null) {
            throw new IllegalArgumentException("No parameters in SET_DEVICE_PARAMETERS");
        }
        if (requestSetDeviceParameters.getParameters().size() != 1) {
            throw new IllegalArgumentException("Expected only one parameter in SET_DEVICE_PARAMETERS");
        }
        RequestSetDeviceParameters.Parameter param = requestSetDeviceParameters.getParameters().get(0);
        if (param == null) {
            throw new IllegalArgumentException("Null parameter in SET_DEVICE_PARAMETERS");
        }
        if (!param.getName().equals("variableList")) {
            throw new IllegalArgumentException("Illegal parameter in SET_DEVICE_PARAMETERS");
        }
        RequestSetDeviceParameters.ValueArray valueVariableList = param.getValue();
        if (valueVariableList == null) {
            throw new IllegalArgumentException("Null value of variableList in SET_DEVICE_PARAMETERS");
        }
        if (valueVariableList.getArray() == null) {
            throw new IllegalArgumentException("Null value of array in variableList in SET_DEVICE_PARAMETERS");
        }

        return valueVariableList.getArray().stream()
                .filter(v -> v.getVariableName() != null)
                .map(this::extractNameAndValue)
                .collect(Collectors.toList());
    }

    private OperationSetDeviceParameters.VariableValue extractNameAndValue(
            RequestSetDeviceParameters.VariableListElement es) {
        String variableName = es.getVariableName();
        Object value = es.getVariableValue();
        return new OperationSetDeviceParameters.VariableValue(variableName, value);
    }

    private Output translateToOutputGet(OperationGetDeviceParameters.Result result, String operationId,
                                        String deviceId) {
        try {
            List<OutputVariable> outputVariables = result.getValues().stream()
                    .map(OpenGateOperationDispatcher::translateGetResultToOutput)
                    .collect(Collectors.toList());

            List<Step> steps =
                    Collections.singletonList(new Step(GET_DEVICE_PARAMETERS_OPERATION, StepResultCode.SUCCESSFUL, "",
                            0L, outputVariables));
            OutputOperation operation = new OutputOperation(new Response(operationId, deviceId,
                    GET_DEVICE_PARAMETERS_OPERATION, OperationResultCode.SUCCESSFUL, "No Error.", steps));
            return new Output(OPENGATE_VERSION, operation);
        } catch (Exception e) {
            return translateThrowableToOutput(GET_DEVICE_PARAMETERS_OPERATION, operationId, deviceId, e);
        }
    }

    private Output translateToOutputRefresh(OperationRefreshInfo.Result result, String operationId, String deviceId) {
        try {
            List<OutputVariable> outputVariables = result.getObtained().entrySet().stream()
                    .map(es -> new OutputVariable(es.getKey(), es.getValue(), SUCCESS_RESULT, SUCCESS_RESULT))
                    .collect(Collectors.toList());
            List<Step> steps =
                    Collections.singletonList(new Step(REFRESH_INFO_OPERATION, StepResultCode.SUCCESSFUL, "", 0L,
                            outputVariables));
            OutputOperation operation =
                    new OutputOperation(new Response(operationId, deviceId, REFRESH_INFO_OPERATION,
                            OperationResultCode.SUCCESSFUL, "No Error.", steps));
            return new Output(OPENGATE_VERSION, operation);
        } catch (Exception e) {
            return translateThrowableToOutput(REFRESH_INFO_OPERATION, operationId, deviceId, e);
        }
    }

    @Value
    private static class UpdateParameters {
        String bundleName;
        String bundleVersion;
        List<OperationUpdate.DeploymentElement> deploymentElements;
    }
}
