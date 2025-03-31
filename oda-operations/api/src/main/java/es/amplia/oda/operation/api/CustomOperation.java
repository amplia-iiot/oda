package es.amplia.oda.operation.api;

import lombok.Value;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CustomOperation {

    @Value
    class Result
    {
        Status status;
        String description;
        Collection<Step> steps;

    }

    enum Status {
        SUCCESSFUL,
        ERROR_IN_PARAM,
        ERROR_PROCESSING
    }

    @Value
    class Step {
        String name;
        StepStatus result;
        String description;
        long timestamp;
        List<Object> responses;

        public Step(String name, StepStatus result, String description, long timestamp, List<Object> responses) {
            this.name = name;
            this.result = result;
            this.description = description;
            this.timestamp = timestamp;
            this.responses = responses;
        }

        public Step(String name, StepStatus result, String description, long timestamp) {
            this(name, result, description, timestamp, Collections.emptyList());
        }
    }

    enum StepStatus {
        SUCCESSFUL,
        ERROR,
        SKIPPED,
        NOT_EXECUTED
    }

    CompletableFuture<Result> execute(String deviceId, String operationId, Map<String, Object> params);

    String getOperationSatisfied();
}
