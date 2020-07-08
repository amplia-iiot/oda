package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.SerializerProvider;
import es.amplia.oda.dispatcher.opengate.domain.*;

import es.amplia.oda.dispatcher.opengate.domain.general.InputGeneralOperation;
import es.amplia.oda.dispatcher.opengate.domain.general.RequestGeneralOperation;
import es.amplia.oda.dispatcher.opengate.domain.get.InputGetOperation;
import es.amplia.oda.dispatcher.opengate.domain.get.RequestGetOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.InputSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.setorconfigure.RequestSetOrConfigureOperation;
import es.amplia.oda.dispatcher.opengate.domain.update.InputUpdateOperation;
import es.amplia.oda.dispatcher.opengate.domain.update.RequestUpdateOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

class OpenGateOperationDispatcher implements Dispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGateOperationDispatcher.class);

    private static final String WRONG_SERIALIZED_OPERATION_EXCEPTION_MESSAGE = "No request specified in deserialized input";
    private static final String OPERATION_RECEIVED_EXCEPTION_MESSAGE = "Operation received: {}";
    private static final String NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE = "No operation specified in deserialized input";


    private final SerializerProvider serializerProvider;
    private final DeviceInfoProvider deviceInfoProvider;
    private final OperationProcessor operationProcessor;


    OpenGateOperationDispatcher(SerializerProvider serializerProvider, DeviceInfoProvider deviceInfoProvider,
                                OperationProcessor operationProcessor) {
        this.serializerProvider = serializerProvider;
        this.deviceInfoProvider = deviceInfoProvider;
        this.operationProcessor = operationProcessor;
    }

    @Override
    public CompletableFuture<byte[]> process(byte[] input, ContentType contentType) {
        if (input == null) {
            throw new IllegalArgumentException("Input is null");
        }

        InputUpdateOperation openGateUpdateOperation = null;
        InputSetOrConfigureOperation openGateInputSetOrConfigureOperation = null;
        InputGetOperation openGateInputGetOperation = null;
        InputGeneralOperation openGateInputGeneralOperation = null;
        try {
            openGateUpdateOperation = serializerProvider.getSerializer(contentType).deserialize(input, InputUpdateOperation.class);
            LOGGER.info(OPERATION_RECEIVED_EXCEPTION_MESSAGE, openGateUpdateOperation);
            if (openGateUpdateOperation == null || openGateUpdateOperation.getOperation() == null || !openGateUpdateOperation.getOperation().getRequest().getName().equals("UPDATE")) {
                throw new IllegalArgumentException(NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE);
            }
            return processUpdateOperation(openGateUpdateOperation, contentType);
        } catch (Exception e) {
            LOGGER.error("Could not deserialize input as Update Operation: \n{}", e.getMessage());
        }

        try {
            openGateInputGetOperation = serializerProvider.getSerializer(contentType).deserialize(input, InputGetOperation.class);
            LOGGER.info(OPERATION_RECEIVED_EXCEPTION_MESSAGE, openGateInputGetOperation);
            if (openGateInputGetOperation == null || openGateInputGetOperation.getOperation() == null || !openGateInputGetOperation.getOperation().getRequest().getName().equals("GET_DEVICE_PARAMETERS")) {
                throw new IllegalArgumentException(NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE);
            }
            return processGetOperation(openGateInputGetOperation, contentType);
        } catch (Exception e) {
            LOGGER.error("Could not deserialize input as Get Operation: \n{}", e.getMessage());
        }

        try {
            openGateInputGeneralOperation = serializerProvider.getSerializer(contentType).deserialize(input, InputGeneralOperation.class);
            LOGGER.info(OPERATION_RECEIVED_EXCEPTION_MESSAGE, openGateInputGeneralOperation);
            if (openGateInputGeneralOperation == null || openGateInputGeneralOperation.getOperation() == null) {
                throw new IllegalArgumentException(NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE);
            }
            return processGeneralOperation(openGateInputGeneralOperation, contentType);
        } catch (Exception e) {
            LOGGER.error("Could not deserialize input as General Operation: \n{}", e.getMessage());
        }

        try {
            openGateInputSetOrConfigureOperation = serializerProvider.getSerializer(contentType).deserialize(input, InputSetOrConfigureOperation.class);
            LOGGER.info(OPERATION_RECEIVED_EXCEPTION_MESSAGE, openGateInputSetOrConfigureOperation);
            if(openGateInputSetOrConfigureOperation == null || openGateInputSetOrConfigureOperation.getOperation() == null) {
                throw new IllegalArgumentException(NO_OPERATION_SPECIFIED_EXCEPTION_MESSAGE);
            }
            return processSetOrConfigureOperation(openGateInputSetOrConfigureOperation, contentType);
        } catch (Exception e) {
            LOGGER.error("Could not deserialize input as Set Operation: \n{}", e.getMessage());
        }

        if (openGateInputSetOrConfigureOperation == null
                && openGateInputGetOperation == null
                && openGateInputGeneralOperation == null
                && openGateUpdateOperation == null) {
            throw new IllegalArgumentException("Input has wrong format");
        }

        return null;
    }

    private byte[] serializeOutput(Output output, ContentType contentType) {
        try {
            LOGGER.info("Operation processed: {}", output);
            return serializerProvider.getSerializer(contentType).serialize(output);
        } catch (IOException e) {
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
}
