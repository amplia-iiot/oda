package es.amplia.oda.core.commons.utils;

import lombok.Value;

@Value
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

    private String deviceId;
    private String datastreamId;
    private long at;
    private Object value;
    private Status status;
    private String error;
}
