package es.amplia.oda.operation.api;

import lombok.Value;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CustomOperation {

    @Value
    class Result
    {
        private Status status;
        private String description;
        private Collection<Step> steps;

    }

    enum Status {
        SUCCESSFUL,
        ERROR_IN_PARAM,
        ERROR_PROCESSING
    }

    @Value
    class Step {
        private String name;
        private StepStatus result;
        private String description;
        private long timestamp;
    }

    enum StepStatus {
        SUCCESSFUL,
        ERROR,
        SKIPPED,
        NOT_EXECUTED
    }

    CompletableFuture<Result> execute(String deviceId, Map<String, Object> params);

    String getOperationSatisfied();
}
