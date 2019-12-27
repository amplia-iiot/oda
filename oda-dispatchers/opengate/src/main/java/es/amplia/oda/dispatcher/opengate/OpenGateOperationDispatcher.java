package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.SerializerProvider;
import es.amplia.oda.dispatcher.opengate.domain.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

class OpenGateOperationDispatcher implements Dispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenGateOperationDispatcher.class);


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

        Input openGateInput;
        try {
            openGateInput = serializerProvider.getSerializer(contentType).deserialize(input, Input.class);
            LOGGER.info("Operation received: {}", openGateInput);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize input");
        }
        if (openGateInput == null) {
            throw new IllegalArgumentException("Input has wrong format");
        }
        if (openGateInput.getOperation() == null) {
            throw new IllegalArgumentException("No operation specified in deserialized input");
        }
        Request request = openGateInput.getOperation().getRequest();
        if (request == null) {
            throw new IllegalArgumentException("No request specified in deserialized input");
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

        return operationProcessor.process(deviceIdForOperations, deviceIdForResponse, request)
                .thenApply(output -> serializeOutput(output, contentType));
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
}
