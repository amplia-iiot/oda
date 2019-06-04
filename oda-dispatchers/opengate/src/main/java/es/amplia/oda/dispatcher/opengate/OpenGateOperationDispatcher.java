package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.interfaces.Dispatcher;
import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.domain.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

class OpenGateOperationDispatcher implements Dispatcher {

    private final Serializer serializer;
    private final DeviceInfoProvider deviceInfoProvider;
    private final OperationProcessor operationProcessor;


    OpenGateOperationDispatcher(Serializer serializer, DeviceInfoProvider deviceInfoProvider,
                                OperationProcessor operationProcessor) {
        this.serializer = serializer;
        this.deviceInfoProvider = deviceInfoProvider;
        this.operationProcessor = operationProcessor;
    }

    @Override
    public CompletableFuture<byte[]> process(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input is null");
        }

        Input openGateInput;
        try {
            openGateInput = serializer.deserialize(input, Input.class);
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

        return operationProcessor.process(deviceIdForOperations, deviceIdForResponse, request);
    }
}
