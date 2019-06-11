package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.OperationProcessor;
import es.amplia.oda.dispatcher.opengate.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

abstract class OperationProcessorTemplate<T, R> implements OperationProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationProcessorTemplate.class);

    static final String SUCCESS_RESULT = "SUCCESS";
    static final String ERROR_RESULT = "ERROR";

    private final Serializer serializer;

    OperationProcessorTemplate(Serializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public CompletableFuture<byte[]> process(String deviceIdForOperations, String deviceIdForResponse, Request request) {
        CompletableFuture<byte[]> responseFuture = new CompletableFuture<>();

        T params = parseParameters(request);
        CompletableFuture<R> future = processOperation(deviceIdForOperations, deviceIdForResponse, params);
        if (future == null) {
            responseFuture.complete(noOperationFor(request.getId(), request.getName(), deviceIdForResponse));
        } else {
            future.thenAccept(result -> {
                Output output;
                try {
                    output = translateToOutput(result, request.getId(), deviceIdForResponse);
                } catch (Exception e) {
                    output = translateThrowableToOutput(request.getName(), request.getId(), request.getDeviceId(), e);
                }

                byte[] resultAsBytes = new byte[0];
                try {
                    resultAsBytes = serializer.serialize(output);
                } catch (IOException e) {
                    LOGGER.error("Error serializing response message. Will sent void byte array as result: ", e);
                }
                responseFuture.complete(resultAsBytes);
            });
        }

        return responseFuture;
    }

    private byte[] noOperationFor(String operationId, String operationName, String deviceId) {
        Response notSupportedResponse =
                new Response(operationId, deviceId, operationName, OperationResultCode.NOT_SUPPORTED,
                        "Operation not supported by the device", Collections.emptyList());
        Output output = new Output(OPENGATE_VERSION, new OutputOperation(notSupportedResponse));

        try {
            return serializer.serialize(output);
        } catch (IOException e) {
            LOGGER.error("Error serializing response message. Will sent void byte array as result: ", e);
            return new byte[0];
        }
    }

    private Output translateThrowableToOutput(String operationName, String operationId, String deviceId, Throwable e) {
        String errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
        List<Step> steps = Collections.singletonList(new Step(operationName, StepResultCode.ERROR, errorMsg, 0L, null));
        OutputOperation operation =
                new OutputOperation(new Response(operationId, deviceId, operationName,
                        OperationResultCode.ERROR_PROCESSING, errorMsg, steps));
        return new Output(OPENGATE_VERSION, operation);
    }

    abstract T parseParameters(Request request);

    abstract CompletableFuture<R> processOperation(String deviceIdForOperations, String deviceIdForResponse, T params);

    abstract Output translateToOutput(R result, String requestId, String deviceId);
}
