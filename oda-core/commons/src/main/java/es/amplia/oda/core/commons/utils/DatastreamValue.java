package es.amplia.oda.core.commons.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
//@AllArgsConstructor
@RequiredArgsConstructor
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

    private final String deviceId;
    private final String datastreamId;
    private final String feed;
    private final Long at;
    private Long date = System.currentTimeMillis();
    private final Object value;
    private final Status status;
    private final String error;
    @NonNull
    private Boolean sent;
    @NonNull
    private Boolean processed;
}
