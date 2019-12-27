package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.dispatcher.opengate.OperationProcessor;
import es.amplia.oda.dispatcher.opengate.domain.Output;
import es.amplia.oda.dispatcher.opengate.domain.Request;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

class OpenGateOperationProcessor implements OperationProcessor {

    private final Map<String, OperationProcessor> catalogueOperationProcessors;
    private final OperationProcessor customOperationProcessor;


    OpenGateOperationProcessor(Map<String, OperationProcessor> catalogueOperationProcessors,
                               OperationProcessor customOperationProcessor) {
        this.catalogueOperationProcessors = catalogueOperationProcessors;
        this.customOperationProcessor = customOperationProcessor;
    }

    @Override
    public CompletableFuture<Output> process(String deviceIdForOperations, String deviceIdForResponse, Request request) {
        return catalogueOperationProcessors.getOrDefault(request.getName(), customOperationProcessor)
                .process(deviceIdForOperations, deviceIdForResponse, request);
    }
}
