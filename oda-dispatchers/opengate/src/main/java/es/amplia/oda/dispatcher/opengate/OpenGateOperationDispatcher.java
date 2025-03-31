package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.OperationSender;
import es.amplia.oda.core.commons.interfaces.SerializerProvider;
import es.amplia.oda.core.commons.utils.operation.request.OperationRequest;
import es.amplia.oda.dispatcher.opengate.domain.OperationResultCode;
import es.amplia.oda.dispatcher.opengate.domain.Output;
import es.amplia.oda.dispatcher.opengate.domain.OutputOperation;
import es.amplia.oda.dispatcher.opengate.domain.Response;
import es.amplia.oda.dispatcher.opengate.domain.custom.InputCustomOperation;
import es.amplia.oda.dispatcher.opengate.domain.custom.RequestCustomOperation;
import es.amplia.oda.dispatcher.opengate.domain.general.InputGeneralOperation;
import es.amplia.oda.dispatcher.opengate.domain.general.RequestGeneralOperation;
import es.amplia.oda.dispatcher.opengate.domain.get.InputGetOperation;
import es.amplia.oda.dispatcher.opengate.domain.get.RequestGetOperation;
import es.amplia.oda.dispatcher.opengate.domain.setclock.InputSetClockOperation;
import es.amplia.oda.dispatcher.opengate.domain.setclock.RequestSetClockOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.InputSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.RequestSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.update.InputUpdateOperation;
import es.amplia.oda.dispatcher.opengate.domain.update.RequestUpdateOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;
import static es.amplia.oda.dispatcher.opengate.operation.processor.GetDeviceParametersProcessor.GET_DEVICE_PARAMETERS_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.SetClockEquipmentProcessor.SET_CLOCK_EQUIPMENT_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.SetDeviceParametersProcessor.SET_DEVICE_PARAMETERS_OPERATION_NAME;
import static es.amplia.oda.dispatcher.opengate.operation.processor.UpdateProcessor.UPDATE_OPERATION_NAME;

class OpenGateOperationDispatcher implements Dispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGateOperationDispatcher.class);

    private static final String WRONG_SERIALIZED_OPERATION_EXCEPTION_MESSAGE = "No request specified in deserialized input";
    private static final String OPERATION_RECEIVED_EXCEPTION_MESSAGE = "Operation received as {}: {}";
    private static final String NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE = "No operation specified in deserialized input";

    private final SerializerProvider serializerProvider;
    private final DeviceInfoProvider deviceInfoProvider;
    private final OperationProcessor operationProcessor;
    private final OperationSender operationSender;


    OpenGateOperationDispatcher(SerializerProvider serializerProvider, DeviceInfoProvider deviceInfoProvider,
                                OperationProcessor operationProcessor, OperationSender operationSender) {
        this.serializerProvider = serializerProvider;
        this.deviceInfoProvider = deviceInfoProvider;
        this.operationProcessor = operationProcessor;
        this.operationSender = operationSender;
    }

    @Override
    public CompletableFuture<byte[]> process(byte[] input, ContentType contentType) {
        if (input == null) {
            throw new IllegalArgumentException("Input is null");
        }

        String opName = null;

        try {
            OperationRequest<Object> basicOperation = serializerProvider.getSerializer(contentType).deserialize(input, OperationRequest.class);
            LOGGER.debug(OPERATION_RECEIVED_EXCEPTION_MESSAGE, "basic operation", basicOperation);
            String[] path = basicOperation.getOperation().getRequest().getPath();
            String deviceId = basicOperation.getOperation().getRequest().getDeviceId();
            opName = basicOperation.getOperation().getRequest().getName();
            if ( (path != null) && (path.length > 0) && operationSender.isForNextLevel(path, deviceId)) {
                LOGGER.debug("Sending operation to level down: {}", basicOperation);
                try {
                    operationSender.downlink(basicOperation);
                    return null;
                } catch (Throwable e) {
                    LOGGER.error("Error sending operation to level down", e);
                    Response response = new Response(basicOperation.getOperation().getRequest().getId(), deviceId, path, opName,
                            OperationResultCode.ERROR_PROCESSING, "Unable to send operation to lower levels", null);
                    OutputOperation operation = new OutputOperation(response);
                    Output output = new Output(OPENGATE_VERSION, operation);
                    return CompletableFuture.completedFuture(serializeOutput(output, contentType));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not deserialize input as Basic Operation", e);
        }

        InputUpdateOperation openGateUpdateOperation = null;
        InputSetOrConfigureOperation openGateInputSetOrConfigureOperation = null;
        InputGetOperation openGateInputGetOperation = null;
        InputGeneralOperation openGateInputGeneralOperation = null;
        InputSetClockOperation openGateInputSetClockOperation = null;
        InputCustomOperation openGateInputCustomOperation = null;

        if (UPDATE_OPERATION_NAME.equals(opName)) {
            try {
                openGateUpdateOperation = serializerProvider.getSerializer(contentType).deserialize(input, InputUpdateOperation.class);
                LOGGER.debug(OPERATION_RECEIVED_EXCEPTION_MESSAGE, "update operation", openGateUpdateOperation);
                if (openGateUpdateOperation == null || openGateUpdateOperation.getOperation() == null || !openGateUpdateOperation.getOperation().getRequest().getName().equals("UPDATE")) {
                    throw new IllegalArgumentException(NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE);
                }
                return processUpdateOperation(openGateUpdateOperation, contentType);
            } catch (Exception e) {
                LOGGER.debug("Could not deserialize input as Update Operation: \n{}", e.getMessage());
            }
        } else if (GET_DEVICE_PARAMETERS_OPERATION_NAME.equals(opName)) {
            try {
                openGateInputGetOperation = serializerProvider.getSerializer(contentType).deserialize(input, InputGetOperation.class);
                LOGGER.debug(OPERATION_RECEIVED_EXCEPTION_MESSAGE, "get operation", openGateInputGetOperation);
                if (openGateInputGetOperation == null || openGateInputGetOperation.getOperation() == null || !openGateInputGetOperation.getOperation().getRequest().getName().equals("GET_DEVICE_PARAMETERS")) {
                    throw new IllegalArgumentException(NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE);
                }
                return processGetOperation(openGateInputGetOperation, contentType);
            } catch (Exception e) {
                LOGGER.debug("Could not deserialize input as Get Operation: \n{}", e.getMessage());
            }
        } else if (SET_CLOCK_EQUIPMENT_OPERATION_NAME.equals(opName)) {
            try {
                openGateInputSetClockOperation = serializerProvider.getSerializer(contentType).deserialize(input, InputSetClockOperation.class);
                LOGGER.debug(OPERATION_RECEIVED_EXCEPTION_MESSAGE, "set clock operation", openGateInputSetClockOperation);
                if (openGateInputSetClockOperation == null || openGateInputSetClockOperation.getOperation() == null) {
                    throw new IllegalArgumentException(NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE);
                }
                return processSetClockOperation(openGateInputSetClockOperation, contentType);
            } catch (Exception e) {
                LOGGER.debug("Could not deserialize input as Set Clock Operation: \n{}", e.getMessage());
            }
        } else if (SET_DEVICE_PARAMETERS_OPERATION_NAME.equals(opName)) {
            try {
                openGateInputSetOrConfigureOperation = serializerProvider.getSerializer(contentType).deserialize(input, InputSetOrConfigureOperation.class);
                LOGGER.debug(OPERATION_RECEIVED_EXCEPTION_MESSAGE, "set or configure operation", openGateInputSetOrConfigureOperation);
                if (openGateInputSetOrConfigureOperation == null || openGateInputSetOrConfigureOperation.getOperation() == null) {
                    throw new IllegalArgumentException(NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE);
                }
                return processSetOrConfigureOperation(openGateInputSetOrConfigureOperation, contentType);
            } catch (Exception e) {
                LOGGER.debug("Could not deserialize input as Set Operation: \n{}", e.getMessage());
            }
        } else {
            try {
                openGateInputGeneralOperation = serializerProvider.getSerializer(contentType).deserialize(input, InputGeneralOperation.class);
                LOGGER.debug(OPERATION_RECEIVED_EXCEPTION_MESSAGE, "general operation", openGateInputGeneralOperation);
                if (openGateInputGeneralOperation == null || openGateInputGeneralOperation.getOperation() == null) {
                    throw new IllegalArgumentException(NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE);
                }
                return processGeneralOperation(openGateInputGeneralOperation, contentType);
            } catch (Exception e) {
                LOGGER.debug("Could not deserialize input as General Operation: \n{}", e.getMessage());
            }
            try {
                openGateInputCustomOperation = serializerProvider.getSerializer(contentType).deserialize(input, InputCustomOperation.class);
                LOGGER.debug(OPERATION_RECEIVED_EXCEPTION_MESSAGE, "custom operation", openGateInputCustomOperation);
                if (openGateInputCustomOperation == null || openGateInputCustomOperation.getOperation() == null) {
                    throw new IllegalArgumentException(NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE);
                }
                return processCustomOperation(openGateInputCustomOperation, contentType);
            } catch (Exception e) {
                LOGGER.debug("Could not deserialize input as Custom Operation: \n{}", e.getMessage());
            }
        }

        if (openGateInputSetOrConfigureOperation == null
                && openGateInputGetOperation == null
                && openGateInputGeneralOperation == null
                && openGateUpdateOperation == null
                && openGateInputSetClockOperation == null
                && openGateInputCustomOperation == null) {
            throw new IllegalArgumentException("Input has wrong format and couldn't be deserialized as any kind of operation");
        }

        return null;
    }

    private byte[] serializeOutput(Output output, ContentType contentType) {
        if (output == null) {
            LOGGER.debug("Response output null, the response will send later");
            return null;
        }
        try {
            LOGGER.debug("Operation processed: {}", output);
            return serializerProvider.getSerializer(contentType).serialize(output);
        } catch (IOException e) {
            LOGGER.error("Error serializing Output", e);
            throw new IllegalArgumentException("Error serializing output " + output + " with content type " +
                    contentType);
        }
    }

    private CompletableFuture<byte[]> processUpdateOperation(InputUpdateOperation openGateUpdate, ContentType contentType) {
        if (openGateUpdate != null) {
            RequestUpdateOperation request = openGateUpdate.getOperation().getRequest();
            if (request == null) {
                throw new IllegalArgumentException(WRONG_SERIALIZED_OPERATION_EXCEPTION_MESSAGE);
            }

            String deviceIdForOperations;
            String deviceIdForResponse;
            String deviceIdInRequest = request.getDeviceId();
            String odaDeviceId = deviceInfoProvider.getDeviceId();
            if (deviceIdInRequest == null || "".equals(deviceIdInRequest) || deviceIdInRequest.equals(odaDeviceId)) {
                deviceIdForOperations = "";
                deviceIdForResponse = odaDeviceId;
            } else {
                deviceIdForOperations = deviceIdInRequest;
                deviceIdForResponse = deviceIdInRequest;
            }
            if(request.getPath() == null) {
                request.setPath(new String[0]);
            }

            return operationProcessor.process(deviceIdForOperations, deviceIdForResponse, request)
                    .thenApply(output -> serializeOutput(output, contentType));
        }
        else {
            return null;
        }
    }

    private CompletableFuture<byte[]> processSetClockOperation(InputSetClockOperation openGateSetClock, ContentType contentType) {
        if (openGateSetClock != null) {
            RequestSetClockOperation request = openGateSetClock.getOperation().getRequest();
            if (request == null) {
                throw new IllegalArgumentException(WRONG_SERIALIZED_OPERATION_EXCEPTION_MESSAGE);
            }

            String deviceIdForOperations;
            String deviceIdForResponse;
            String deviceIdInRequest = request.getDeviceId();
            String odaDeviceId = deviceInfoProvider.getDeviceId();
            if (deviceIdInRequest == null || "".equals(deviceIdInRequest) || deviceIdInRequest.equals(odaDeviceId)) {
                deviceIdForOperations = "";
                deviceIdForResponse = odaDeviceId;
            } else {
                deviceIdForOperations = deviceIdInRequest;
                deviceIdForResponse = deviceIdInRequest;
            }
            if(request.getPath() == null) {
                request.setPath(new String[0]);
            }

            return operationProcessor.process(deviceIdForOperations, deviceIdForResponse, request)
                    .thenApply(output -> serializeOutput(output, contentType));
        }
        else {
            return null;
        }
    }

    private CompletableFuture<byte[]> processSetOrConfigureOperation(InputSetOrConfigureOperation openGateInputSetOrConfigure, ContentType contentType) {
        if (openGateInputSetOrConfigure != null) {
            RequestSetOrConfigureOperation request = openGateInputSetOrConfigure.getOperation().getRequest();
            if (request == null) {
                throw new IllegalArgumentException(WRONG_SERIALIZED_OPERATION_EXCEPTION_MESSAGE);
            }

            String deviceIdForOperations;
            String deviceIdForResponse;
            String deviceIdInRequest = request.getDeviceId();
            String odaDeviceId = deviceInfoProvider.getDeviceId();
            if (deviceIdInRequest == null || "".equals(deviceIdInRequest) || deviceIdInRequest.equals(odaDeviceId)) {
                deviceIdForOperations = "";
                deviceIdForResponse = odaDeviceId;
            } else {
                deviceIdForOperations = deviceIdInRequest;
                deviceIdForResponse = deviceIdInRequest;
            }
            if(request.getPath() == null) {
                request.setPath(new String[0]);
            }

            return operationProcessor.process(deviceIdForOperations, deviceIdForResponse, request)
                    .thenApply(output -> serializeOutput(output, contentType));
        }
        else {
            return null;
        }
    }

    private CompletableFuture<byte[]> processGetOperation(InputGetOperation openGateInputGetOperation, ContentType contentType) {
        if (openGateInputGetOperation != null) {
            RequestGetOperation request = openGateInputGetOperation.getOperation().getRequest();
            if (request == null) {
                throw new IllegalArgumentException(WRONG_SERIALIZED_OPERATION_EXCEPTION_MESSAGE);
            }

            String deviceIdForOperations;
            String deviceIdForResponse;
            String deviceIdInRequest = request.getDeviceId();
            String odaDeviceId = deviceInfoProvider.getDeviceId();
            if (deviceIdInRequest == null || "".equals(deviceIdInRequest) || deviceIdInRequest.equals(odaDeviceId)) {
                deviceIdForOperations = "";
                deviceIdForResponse = odaDeviceId;
            } else {
                deviceIdForOperations = deviceIdInRequest;
                deviceIdForResponse = deviceIdInRequest;
            }
            if(request.getPath() == null) {
                request.setPath(new String[0]);
            }

            return operationProcessor.process(deviceIdForOperations, deviceIdForResponse, request)
                    .thenApply(output -> serializeOutput(output, contentType));
        }
        else {
            return null;
        }
    }

    private CompletableFuture<byte[]> processGeneralOperation(InputGeneralOperation openGateInputGeneralOperation, ContentType contentType) {
        if (openGateInputGeneralOperation != null) {
            RequestGeneralOperation request = openGateInputGeneralOperation.getOperation().getRequest();
            if (request == null) {
                throw new IllegalArgumentException(WRONG_SERIALIZED_OPERATION_EXCEPTION_MESSAGE);
            }

            String deviceIdForOperations;
            String deviceIdForResponse;
            String deviceIdInRequest = request.getDeviceId();
            String odaDeviceId = deviceInfoProvider.getDeviceId();
            if (deviceIdInRequest == null || "".equals(deviceIdInRequest) || deviceIdInRequest.equals(odaDeviceId)) {
                deviceIdForOperations = "";
                deviceIdForResponse = odaDeviceId;
            } else {
                deviceIdForOperations = deviceIdInRequest;
                deviceIdForResponse = deviceIdInRequest;
            }
            if(request.getPath() == null) {
                request.setPath(new String[0]);
            }

            return operationProcessor.process(deviceIdForOperations, deviceIdForResponse, request)
                    .thenApply(output -> serializeOutput(output, contentType));
        }
        else {
            return null;
        }
    }

    private CompletableFuture<byte[]> processCustomOperation(InputCustomOperation openGateInputCustomOperation, ContentType contentType) {
        if (openGateInputCustomOperation != null) {
            RequestCustomOperation request = openGateInputCustomOperation.getOperation().getRequest();
            if (request == null) {
                throw new IllegalArgumentException(WRONG_SERIALIZED_OPERATION_EXCEPTION_MESSAGE);
            }

            String deviceIdForOperations;
            String deviceIdForResponse;
            String deviceIdInRequest = request.getDeviceId();
            String odaDeviceId = deviceInfoProvider.getDeviceId();
            if (deviceIdInRequest == null || "".equals(deviceIdInRequest) || deviceIdInRequest.equals(odaDeviceId)) {
                deviceIdForOperations = "";
                deviceIdForResponse = odaDeviceId;
            } else {
                deviceIdForOperations = deviceIdInRequest;
                deviceIdForResponse = deviceIdInRequest;
            }
            if(request.getPath() == null) {
                request.setPath(new String[0]);
            }

            return operationProcessor.process(deviceIdForOperations, deviceIdForResponse, request)
                    .thenApply(output -> serializeOutput(output, contentType));
        }
        else {
            return null;
        }
    }
}
