package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.OperationProcessor;
import es.amplia.oda.dispatcher.opengate.domain.Request;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

class OpenGateOperationProcessor implements OperationProcessor {

    private final Map<String, OperationProcessor> catalogueOperationProcessors;
    private final OperationProcessor unsupportedOperationProcessor;

    OpenGateOperationProcessor(Map<String, OperationProcessor> catalogueOperationProcessors,
                               OperationProcessor unsupportedOperationProcessor) {
        this.catalogueOperationProcessors = catalogueOperationProcessors;
        this.unsupportedOperationProcessor = unsupportedOperationProcessor;
    }

    @Override
    public CompletableFuture<byte[]> process(String deviceIdForOperations, String deviceIdForResponse, Request request) {
        return catalogueOperationProcessors.getOrDefault(request.getName(), unsupportedOperationProcessor)
                .process(deviceIdForOperations, deviceIdForResponse, request);
    }
}
