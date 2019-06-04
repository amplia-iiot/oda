package es.amplia.oda.dispatcher.opengate.operation.processor;

import es.amplia.oda.core.commons.interfaces.Serializer;
import es.amplia.oda.dispatcher.opengate.domain.Output;
import es.amplia.oda.dispatcher.opengate.domain.Request;

import java.util.concurrent.CompletableFuture;

class UnsupportedOperationProcessor extends OperationProcessorTemplate<Void, Void> {

    UnsupportedOperationProcessor(Serializer serializer) {
        super(serializer);
    }

    @Override
    Void parseParameters(Request request) {
        // No parameters to parse
        return null;
    }

    @Override
    CompletableFuture<Void> processOperation(String deviceIdForOperations, String deviceIdForResponse, Void params) {
        // Returning null to notify operation is not supported
        return null;
    }

    @Override
    Output translateToOutput(Void result, String requestId, String deviceId) {
        // Never called
        return null;
    }
}
