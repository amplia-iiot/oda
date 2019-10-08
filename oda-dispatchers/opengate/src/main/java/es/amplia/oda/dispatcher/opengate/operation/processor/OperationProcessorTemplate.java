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

        Output output;
        try {
            T params = parseParameters(request);
            CompletableFuture<R> future = processOperation(deviceIdForOperations, params);
            if (future == null) {
                output = translateNoOperationToOutput(request.getId(), request.getName(), deviceIdForResponse,
                        request.getPath());
            } else {
                output = translateToOutput(future.get(), request.getId(), deviceIdForResponse, request.getPath());
            }
        } catch (Exception e) {
            LOGGER.error("Uncontrolled exception processing request {}", request, e);
            output = translateThrowableToOutput(request.getName(), request.getId(), deviceIdForResponse,
                    request.getPath(), e);
        }

        try {
            byte[] resultAsBytes = serializer.serialize(output);
            responseFuture.complete(resultAsBytes);
            return responseFuture;
        } catch (IOException e) {
            LOGGER.error("Error serializing response message. Will sent void byte array as result: ", e);
            return null;
        }
    }

    private Output translateNoOperationToOutput(String operationId, String operationName, String deviceId, String[] path) {
        Response notSupportedResponse =
                new Response(operationId, deviceId, path, operationName, OperationResultCode.NOT_SUPPORTED,
                        "Operation not supported by the device", Collections.emptyList());
        return new Output(OPENGATE_VERSION, new OutputOperation(notSupportedResponse));
    }

    private Output translateThrowableToOutput(String operationName, String operationId, String deviceId, String[] path,
                                              Throwable e) {
        String errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
        List<Step> steps =
                Collections.singletonList(new Step(operationName, StepResultCode.ERROR, errorMsg, null, null));
        OutputOperation operation =
                new OutputOperation(new Response(operationId, deviceId, path, operationName,
                        OperationResultCode.ERROR_PROCESSING, errorMsg, steps));
        return new Output(OPENGATE_VERSION, operation);
    }

    abstract T parseParameters(Request request);

    abstract CompletableFuture<R> processOperation(String deviceIdForOperations, T params);

    abstract Output translateToOutput(R result, String requestId, String deviceId, String[] path);
}
