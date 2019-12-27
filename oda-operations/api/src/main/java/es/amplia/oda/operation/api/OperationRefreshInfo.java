package es.amplia.oda.operation.api;

import lombok.Value;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface OperationRefreshInfo {
    @Value
    class Result
    {
        Map<String, Object> obtained;
    }

    /**
     * Search the system for all the registered datastreamsGetters and execute a get() operation in each of them.  
     * @param deviceId If empty string, the datastreamsGetters must be for ODA itself, otherwise, 
     * the datastreamsGetters must be for the device specified in this parameter. Not null.
     * @return A map with the result of (only) the successful get() of each datastreamsGetters found.
     */
    CompletableFuture<Result> refreshInfo(String deviceId);
}
