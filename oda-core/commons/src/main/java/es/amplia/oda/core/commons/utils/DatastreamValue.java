package es.amplia.oda.core.commons.utils;

import lombok.Data;
import lombok.NonNull;

@Data
public class DatastreamValue {
    public enum Status {
        OK("Successful"),
        NOT_FOUND("Not found"),
        PROCESSING_ERROR("Error processing");

        private final String userFriendlyName;

        Status(String userFriendlyName) {
            this.userFriendlyName = userFriendlyName;
        }

        @Override
        public String toString() {
            return userFriendlyName;
        }
    }


    public DatastreamValue(String deviceId, String datastreamId, String feed, Long at, Object value, Status status,
                           String error, @NonNull Boolean sent, @NonNull Boolean processed) {
        this.deviceId = deviceId;
        this.datastreamId = datastreamId;
        this.feed = feed;
        this.at = at;
        this.value = value;
        this.status = status;
        this.error = error;
        this.sent = sent;
        this.processed = processed;
    }

    private final String deviceId;
    private final String datastreamId;
    private final String feed;
    private final Long at;
    private Long date = System.currentTimeMillis();
    private final Object value;
    private Status status;
    private String error;
    @NonNull
    private Boolean sent;
    @NonNull
    private Boolean processed;
}
