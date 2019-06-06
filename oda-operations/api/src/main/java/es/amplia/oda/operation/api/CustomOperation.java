package es.amplia.oda.operation.api;

import lombok.Value;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CustomOperation {

    @Value
    class Result
    {
        Status status;
        String description;
    }

    enum Status {
        SUCCESSFUL,
        ERROR_IN_PARAM,
        ERROR_PROCESSING
    }

    CompletableFuture<Result> execute(String deviceId, Map<String, Object> params);

    String getOperationSatisfied();
}
