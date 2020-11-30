package es.amplia.oda.operation.api;

import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface OperationRefreshInfo {
    enum Status {
        OK,
        NOT_FOUND,
        PROCESSING_ERROR
    }

    @Value
    class RefreshInfoValue {
        String datastreamId;
        Status status;
        long at;
        Object value; //null if status != OK
        String error; //null if status != PROCESSING_ERROR
    }

    @Value
    class Result {
        Map<String, List<RefreshInfoValue>> values;
    }


    /**
     * Search the system for all the registered datastreamsGetters and execute a get() operation in each of them.  
     * @param deviceId If empty string, the datastreamsGetters must be for ODA itself, otherwise, 
     * the datastreamsGetters must be for the device specified in this parameter. Not null.
     * @return A map with the result of (only) the successful get() of each datastreamsGetters found.
     */
    CompletableFuture<Result> refreshInfo(String deviceId);
}
