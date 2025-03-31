package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.OperationProcessor;
import es.amplia.oda.dispatcher.opengate.domain.*;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static es.amplia.oda.core.commons.utils.OdaCommonConstants.OPENGATE_VERSION;

abstract class OperationProcessorTemplate<T, R> implements OperationProcessor {

    static final String SUCCESS_RESULT = "SUCCESS";
    static final String ERROR_RESULT = "ERROR";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    @Override
    public CompletableFuture<Output> process(String deviceIdForOperations, String deviceIdForResponse, Request request) {
        return processRequest(deviceIdForOperations, deviceIdForResponse, request)
                .exceptionally(e -> translateThrowableToOutput(request.getName(), request.getId(), deviceIdForResponse,
                        request.getPath(), e));
    }

    private CompletableFuture<Output> processRequest(String deviceIdForOperations, String deviceIdForResponse, Request request) {
        T params = parseParameters(request);
        CompletableFuture<R> future = processOperation(deviceIdForOperations, request.getId(), params);
        if (future == null) {
            return CompletableFuture.completedFuture(
                    translateNoOperationToOutput(request.getId(), request.getName(), deviceIdForResponse,
                            request.getPath()));
        } else {
            return future.applyToEither(timeout(80), r -> translateToOutput(r, request.getId(), deviceIdForResponse, request.getPath()));
        }
    }

    public static <T> CompletableFuture<T> timeout(int seconds) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        scheduler.schedule(() -> {
            final TimeoutException ex = new TimeoutException("Timeout during the request processing");
            return future.completeExceptionally(ex);
        }, seconds, TimeUnit.SECONDS);
        return future;
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

    abstract CompletableFuture<R> processOperation(String deviceIdForOperations, String operationId, T params);

    abstract Output translateToOutput(R result, String requestId, String deviceId, String[] path);
}
