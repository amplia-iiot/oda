package es.amplia.oda.core.commons.interfaces;


import java.util.concurrent.CompletableFuture;

public interface ScadaDispatcher {

    enum ScadaOperationResult {
        SUCCESS,
        ERROR,
        NOT_SUPPORTED
    }

    enum ScadaOperation {
        SELECT,
        SELECT_BEFORE_OPERATE,
        DIRECT_OPERATE,
        DIRECT_OPERATE_NO_ACK
    }

    <T, S> CompletableFuture<ScadaOperationResult> process(ScadaOperation operation, int index, T value, S type);
}
