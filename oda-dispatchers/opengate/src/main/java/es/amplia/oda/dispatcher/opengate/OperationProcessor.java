package es.amplia.oda.dispatcher.opengate;

import es.amplia.oda.dispatcher.opengate.domain.Output;
import es.amplia.oda.dispatcher.opengate.domain.interfaces.Request;

import java.util.concurrent.CompletableFuture;

public interface OperationProcessor {
    CompletableFuture<Output> process(String deviceIdForOperations, String deviceIdForResponse, Request request, int timeout);
}
